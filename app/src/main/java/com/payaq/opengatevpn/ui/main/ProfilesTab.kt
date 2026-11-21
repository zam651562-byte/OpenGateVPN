package com.payaq.opengatevpn.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payaq.opengatevpn.R
import com.payaq.opengatevpn.data.vpn.OpenVpnProfile
import com.payaq.opengatevpn.ui.theme.BorderHairline
import com.payaq.opengatevpn.ui.theme.CardBackground
import com.payaq.opengatevpn.ui.theme.Primary
import com.payaq.opengatevpn.ui.theme.StatusError
import com.payaq.opengatevpn.ui.theme.StatusSuccess
import com.payaq.opengatevpn.ui.theme.TechnicalData
import com.payaq.opengatevpn.ui.theme.TextMuted

@Composable
fun ProfilesTabContent(
    profiles: List<OpenVpnProfile>,
    activeProfileId: String?,
    profileMessage: String?,
    tunnelActive: Boolean,
    onImport: () -> Unit,
    onUseProfile: (String) -> Boolean,
    onSaveCredentials: (String, String, String) -> Unit,
    onExportProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.profiles_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Primary
        )
        Text(
            text = stringResource(R.string.profiles_description),
            style = TechnicalData,
            color = TextMuted
        )
        Button(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.profiles_import))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_language_title),
                style = TechnicalData,
                color = TextMuted,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = { onLanguageChange("ar") }) {
                Text(stringResource(R.string.language_arabic))
            }
            OutlinedButton(onClick = { onLanguageChange("en") }) {
                Text(stringResource(R.string.language_english))
            }
        }
        profileMessage?.let { message ->
            Text(
                text = message,
                style = TechnicalData,
                color = if (
                    message.startsWith("Profile import failed") ||
                    message.startsWith("Profile export failed") ||
                    message.startsWith("Could not") ||
                    message.startsWith("This profile")
                ) StatusError else StatusSuccess
            )
        }
        if (profiles.isEmpty()) {
            Text(
                text = stringResource(R.string.profiles_empty),
                style = TechnicalData,
                color = TextMuted,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(profiles, key = OpenVpnProfile::id) { profile ->
                    ProfileCard(
                        profile = profile,
                        isSelected = profile.id == activeProfileId,
                        tunnelActive = tunnelActive,
                        onUse = { onUseProfile(profile.id) },
                        onSaveCredentials = { username, password ->
                            onSaveCredentials(profile.id, username, password)
                        },
                        onExport = { onExportProfile(profile.id) },
                        onDelete = { onDeleteProfile(profile.id) }
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.profiles_authorization_note),
            style = TechnicalData.copy(fontSize = 10.sp),
            color = TextMuted,
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(10.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
                .padding(12.dp)
        )
    }
}

@Composable
private fun ProfileCard(
    profile: OpenVpnProfile,
    isSelected: Boolean,
    tunnelActive: Boolean,
    onUse: () -> Unit,
    onSaveCredentials: (String, String) -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    var username by remember(profile.id, profile.username) { mutableStateOf(profile.username) }
    var password by remember(profile.id, profile.password) { mutableStateOf(profile.password) }
    val dnsValue = if (profile.dnsServers.isEmpty()) {
        stringResource(R.string.profile_server_default)
    } else {
        profile.dnsServers.joinToString()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, if (isSelected) Primary.copy(alpha = 0.6f) else BorderHairline, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isSelected) Primary else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Text(stringResource(R.string.profile_selected), style = TechnicalData.copy(fontSize = 9.sp), color = StatusSuccess)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(R.string.profile_remote, profile.remote), style = TechnicalData, color = TextMuted)
        Text(
            text = stringResource(R.string.profile_dns, dnsValue),
            style = TechnicalData,
            color = TextMuted
        )
        Text(
            text = if (profile.fullTunnel) {
                stringResource(R.string.profile_full_tunnel, profile.routeCount)
            } else {
                stringResource(R.string.profile_split_tunnel, profile.routeCount)
            },
            style = TechnicalData,
            color = TextMuted
        )
        if (profile.requiresCredentials) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.profile_username)) },
                enabled = !tunnelActive,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.profile_password)) },
                enabled = !tunnelActive,
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onSaveCredentials(username, password) },
                enabled = !tunnelActive && username.isNotBlank() && password.isNotBlank()
            ) {
                Text(
                    stringResource(
                        if (profile.username.isNotBlank()) R.string.profile_update_credentials
                        else R.string.profile_save_credentials
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = onUse, enabled = !tunnelActive && !isSelected) {
                Text(stringResource(if (isSelected) R.string.profile_in_use else R.string.profile_use))
            }
            OutlinedButton(onClick = onExport) { Text(stringResource(R.string.profile_export)) }
            OutlinedButton(onClick = onDelete, enabled = !tunnelActive || !isSelected) {
                Text(stringResource(R.string.profile_delete))
            }
        }
    }
}
