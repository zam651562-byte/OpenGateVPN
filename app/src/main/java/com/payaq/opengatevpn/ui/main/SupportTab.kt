package com.payaq.opengatevpn.ui.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Facebook
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import com.payaq.opengatevpn.ui.theme.BorderHairline
import com.payaq.opengatevpn.R
import com.payaq.opengatevpn.ui.theme.Primary
import com.payaq.opengatevpn.ui.theme.TextMuted

private data class SupportContact(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val uri: String? = null
)

@Composable
fun SupportTabContent() {
    val context = LocalContext.current
    val isArabic = LocalConfiguration.current.locales[0]?.language == "ar"
    val clipboard = LocalClipboardManager.current
    val contacts = listOf(
        SupportContact(
            title = context.getString(R.string.support_telegram_channel),
            value = "t.me/+SiOMr4JAU8thZDFk",
            icon = Icons.Rounded.Send,
            uri = "https://t.me/+SiOMr4JAU8thZDFk"
        ),
        SupportContact(
            title = context.getString(R.string.support_telegram_group),
            value = "t.me/+ZXt8aHvs5ok2ZjM0",
            icon = Icons.Rounded.Send,
            uri = "https://t.me/+ZXt8aHvs5ok2ZjM0"
        ),
        SupportContact(
            title = context.getString(R.string.support_facebook),
            value = "facebook.com/share/1DuqpyXuTa",
            icon = Icons.Rounded.Facebook,
            uri = "https://www.facebook.com/share/1DuqpyXuTa/"
        ),
        SupportContact(
            title = context.getString(R.string.support_email),
            value = "mwings740@gmail.com",
            icon = Icons.Rounded.Email,
            uri = "mailto:mwings740@gmail.com"
        ),
        SupportContact(
            title = "Sham Cash",
            value = "963793dbb5e910b81683981b2c8fd9f6",
            icon = Icons.Rounded.Link
        )
    )

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.support_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = Primary
            )
            Text(
                text = stringResource(R.string.support_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDirection = TextDirection.Content
                ),
                color = TextMuted
            )

            contacts.forEach { contact ->
                SupportContactCard(
                    contact = contact,
                    onOpen = {
                        contact.uri?.let { uri ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            try {
                                context.startActivity(intent)
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.support_open_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onCopy = {
                        clipboard.setText(AnnotatedString(contact.uri?.removePrefix("mailto:") ?: contact.value))
                        Toast.makeText(context, R.string.support_copied, Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.support_safety_note),
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun SupportContactCard(
    contact: SupportContact,
    onOpen: () -> Unit,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
            .background(Color(0xFF101010), RoundedCornerShape(12.dp))
            .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = contact.icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(22.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        ) {
            Text(
                text = contact.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Primary
            )
            Text(
                text = contact.value,
                style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Ltr),
                color = TextMuted
            )
        }
        if (contact.uri != null) {
            IconButton(onClick = onOpen) {
                Icon(
                    imageVector = Icons.Rounded.OpenInNew,
                    contentDescription = "فتح ${contact.title}",
                    tint = Primary
                )
            }
        }
        IconButton(onClick = onCopy) {
            Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = "نسخ ${contact.title}",
                tint = TextMuted
            )
        }
    }
}
