package com.payaq.opengatevpn.data.vpn

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import de.blinkt.openvpn.core.ConfigParser
import java.io.File
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

data class OpenVpnProfile(
    val id: String,
    val name: String,
    val config: String,
    val remote: String,
    val dnsServers: List<String>,
    val routeCount: Int,
    val fullTunnel: Boolean,
    val requiresCredentials: Boolean,
    val username: String = "",
    val password: String = ""
)

class OpenVpnProfileStore(private val context: Context) {

    @Synchronized
    fun load(): List<OpenVpnProfile> {
        val file = profilesFile
        val backup = File(file.parentFile, "${file.name}.bak")
        if (!file.exists() && backup.exists()) {
            check(backup.renameTo(file)) { "Could not recover encrypted profile storage." }
        } else if (file.exists() && backup.exists()) {
            backup.delete()
        }
        if (!file.exists()) return emptyList()

        val json = encryptedFile(file).openFileInput().bufferedReader().use { it.readText() }
        val array = JSONArray(json)
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            OpenVpnProfile(
                id = item.getString("id"),
                name = item.getString("name"),
                config = item.getString("config"),
                remote = item.getString("remote"),
                dnsServers = item.getJSONArray("dns").toStringList(),
                routeCount = item.getInt("routes"),
                fullTunnel = item.getBoolean("fullTunnel"),
                requiresCredentials = item.getBoolean("requiresCredentials"),
                username = item.optString("username"),
                password = item.optString("password")
            )
        }
    }

    @Synchronized
    fun addProfile(name: String, config: String): List<OpenVpnProfile> {
        require(config.toByteArray(StandardCharsets.UTF_8).size <= MAX_PROFILE_BYTES) {
            "The OpenVPN profile exceeds the 1 MiB import limit."
        }
        val summary = summarize(config)
        val existing = load()
        require(existing.size < MAX_PROFILES) { "You can store up to $MAX_PROFILES profiles." }

        val profile = OpenVpnProfile(
            id = UUID.randomUUID().toString(),
            name = cleanName(name),
            config = config,
            remote = summary.remote,
            dnsServers = summary.dnsServers,
            routeCount = summary.routeCount,
            fullTunnel = summary.fullTunnel,
            requiresCredentials = summary.requiresCredentials
        )
        return (existing + profile).also(::save)
    }

    @Synchronized
    fun updateCredentials(id: String, username: String, password: String): List<OpenVpnProfile> {
        require(username.isNotBlank() && password.isNotBlank()) {
            "Enter both a username and password."
        }
        val updated = load().map { profile ->
            if (profile.id == id) profile.copy(username = username, password = password) else profile
        }
        require(updated.any { it.id == id }) { "The selected profile is no longer available." }
        save(updated)
        return updated
    }

    @Synchronized
    fun delete(id: String): List<OpenVpnProfile> =
        load().filterNot { it.id == id }.also(::save)

    private fun summarize(config: String): ConfigSummary {
        require(config.isNotBlank()) { "The selected file is empty." }
        val normalized = config.replace("\r\n", "\n")
        require(!UNSUPPORTED_SCRIPT_DIRECTIVE.containsMatchIn(normalized)) {
            "This profile contains a script, plugin, or management directive that this app does not support."
        }
        require(!EXTERNAL_FILE_DIRECTIVE.containsMatchIn(normalized)) {
            "This import supports self-contained profiles only; embed certificate and key data inside the .ovpn file."
        }

        val parser = ConfigParser()
        parser.parseConfig(StringReader(normalized))
        val profile = parser.convertProfile()
        val endpoints = profile.mConnections
            .filter { it.mEnabled && it.mServerName.isNotBlank() }
            .map { "${it.mServerName}:${it.mServerPort}" }
            .distinct()
        require(endpoints.isNotEmpty()) { "The profile must define at least one remote server." }

        val dns = DNS_DIRECTIVE.findAll(normalized)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
        val routeCount = ROUTE_DIRECTIVE.findAll(normalized).count()
        val fullTunnel = FULL_TUNNEL_DIRECTIVE.containsMatchIn(normalized)
        val requiresCredentials = AUTH_USER_PASS_DIRECTIVE.containsMatchIn(normalized)
        return ConfigSummary(endpoints.joinToString(", "), dns, routeCount, fullTunnel, requiresCredentials)
    }

    private fun save(profiles: List<OpenVpnProfile>) {
        val parent = profilesFile.parentFile ?: error("Profile storage directory is unavailable.")
        check(parent.exists() || parent.mkdirs()) { "Could not create the private profile directory." }
        val json = JSONArray().apply {
            profiles.forEach { profile ->
                put(
                    JSONObject()
                        .put("id", profile.id)
                        .put("name", profile.name)
                        .put("config", profile.config)
                        .put("remote", profile.remote)
                        .put("dns", JSONArray(profile.dnsServers))
                        .put("routes", profile.routeCount)
                        .put("fullTunnel", profile.fullTunnel)
                        .put("requiresCredentials", profile.requiresCredentials)
                        .put("username", profile.username)
                        .put("password", profile.password)
                )
            }
        }.toString()

        val target = profilesFile
        val temporary = File(parent, "${target.name}.tmp")
        val backup = File(parent, "${target.name}.bak")
        temporary.delete()
        backup.delete()

        try {
            encryptedFile(temporary).openFileOutput().bufferedWriter().use { it.write(json) }
            if (target.exists()) check(target.renameTo(backup)) { "Could not safely update stored profiles." }
            check(temporary.renameTo(target)) { "Could not safely save imported profiles." }
            backup.delete()
        } catch (error: Exception) {
            temporary.delete()
            if (backup.exists() && !target.exists()) backup.renameTo(target)
            throw error
        }
    }

    private fun encryptedFile(file: File): EncryptedFile {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    private val profilesFile: File
        get() = File(context.noBackupFilesDir, "openvpn-profiles.enc")

    private fun cleanName(input: String): String {
        val cleaned = input.substringAfterLast('/').substringBeforeLast('.')
            .replace(Regex("[\\p{Cntrl}/\\\\]"), "")
            .trim()
            .take(MAX_NAME_LENGTH)
        return cleaned.ifBlank { "Imported OpenVPN profile" }
    }

    private fun JSONArray.toStringList(): List<String> =
        List(length()) { index -> getString(index) }

    private data class ConfigSummary(
        val remote: String,
        val dnsServers: List<String>,
        val routeCount: Int,
        val fullTunnel: Boolean,
        val requiresCredentials: Boolean
    )

    companion object {
        private const val MAX_PROFILE_BYTES = 1024 * 1024
        private const val MAX_PROFILES = 12
        private const val MAX_NAME_LENGTH = 64
        private val DNS_DIRECTIVE = Regex("(?im)^\\s*dhcp-option\\s+DNS\\s+(\\S+)")
        private val ROUTE_DIRECTIVE = Regex("(?im)^\\s*route(?:-ipv6)?\\s+\\S+")
        private val FULL_TUNNEL_DIRECTIVE =
            Regex("(?im)^\\s*redirect-gateway(?:\\s|$)")
        private val AUTH_USER_PASS_DIRECTIVE = Regex("(?im)^\\s*auth-user-pass\\s*$")
        private val EXTERNAL_FILE_DIRECTIVE = Regex(
            "(?im)^\\s*(?:ca|cert|key|pkcs12|tls-auth|tls-crypt|askpass|crl-verify|auth-user-pass)\\s+[^<\\s][^\\s]*"
        )
        private val UNSUPPORTED_SCRIPT_DIRECTIVE = Regex(
            "(?im)^\\s*(?:up|down|route-up|route-pre-down|ipchange|client-connect|client-disconnect|learn-address|plugin|auth-user-pass-verify|tls-verify|management(?:-[\\w-]+)?)\\s+"
        )
    }
}

internal fun OpenVpnProfile.toBase64Config(): String =
    Base64.encodeToString(config.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
