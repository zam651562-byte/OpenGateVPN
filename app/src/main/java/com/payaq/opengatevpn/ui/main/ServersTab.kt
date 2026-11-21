package com.payaq.opengatevpn.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payaq.opengatevpn.data.model.VpnServerDisplay
import com.payaq.opengatevpn.R
import com.payaq.opengatevpn.ui.theme.BorderHairline
import com.payaq.opengatevpn.ui.theme.LabelCaps
import com.payaq.opengatevpn.ui.theme.Primary
import com.payaq.opengatevpn.ui.theme.StatusSuccess
import com.payaq.opengatevpn.ui.theme.TechnicalData
import com.payaq.opengatevpn.ui.theme.TextMuted
import com.payaq.opengatevpn.ui.util.countryCodeToFlag
import com.payaq.opengatevpn.ui.util.formatScore
import com.payaq.opengatevpn.ui.util.formatSpeed


// ── Servers Tab Content ─────────────────────────────────────────────────────────

@Composable
fun ServersTabContent(
    viewModel: VpnViewModel,
    onServerSelected: (VpnServerDisplay) -> Unit
) {
    val listState = rememberLazyListState()

    // Automatically jump to the absolute top of the list when filter/sort/query changes
    // or when a fresh directory replaces the data (keeps post-refresh scrolling deterministic).
    LaunchedEffect(
        viewModel.activeSort,
        viewModel.selectedCountryFilter,
        viewModel.residentialOnly,
        viewModel.favoritesOnly,
        viewModel.searchQuery,
        viewModel.displayServers
    ) {
        listState.scrollToItem(0)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.server_explorer_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = Primary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StatusSuccess)
            )
            Text(
                text = stringResource(R.string.server_count, viewModel.displayServers.size),
                style = TechnicalData,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
                .background(Color(0xFF141414))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.server_search_hint),
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = viewModel.searchQuery,
                    onValueChange = {
                        viewModel.searchQuery = it
                        // Reset country filter when typing
                        if (it.isNotEmpty()) viewModel.selectedCountryFilter = null
                    },
                    textStyle = TechnicalData.copy(color = Primary, fontSize = 14.sp),
                    cursorBrush = SolidColor(Primary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (viewModel.searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(R.string.server_search_hint),
                                style = TechnicalData.copy(color = TextMuted, fontSize = 14.sp)
                            )
                        }
                        innerTextField()
                    }
                )
                if (viewModel.searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.server_clear_search),
                        tint = TextMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.searchQuery = "" }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Country Filter Chips
        if (viewModel.availableCountries.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        label = stringResource(R.string.server_filter_all, viewModel.displayServers.size),
                        isSelected = viewModel.selectedCountryFilter == null,
                        onClick = { viewModel.selectedCountryFilter = null }
                    )
                }
                items(
                    items = viewModel.availableCountries,
                    key = { it }
                ) { country ->
                    val count = viewModel.countryServerCounts[country] ?: 0
                    FilterChip(
                        label = "${country.uppercase()}  ($count)",
                        isSelected = viewModel.selectedCountryFilter == country,
                        onClick = { viewModel.selectedCountryFilter = country }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sort Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    label = stringResource(R.string.server_sort_overall),
                    isSelected = viewModel.activeSort == SortCriteria.OVERALL,
                    onClick = { viewModel.setSortCriteria(SortCriteria.OVERALL) }
                )
            }
            item {
                FilterChip(
                    label = stringResource(R.string.server_sort_score),
                    isSelected = viewModel.activeSort == SortCriteria.SCORE,
                    onClick = { viewModel.setSortCriteria(SortCriteria.SCORE) }
                )
            }
            item {
                FilterChip(
                    label = stringResource(R.string.server_sort_ping),
                    isSelected = viewModel.activeSort == SortCriteria.PING,
                    onClick = { viewModel.setSortCriteria(SortCriteria.PING) }
                )
            }
            item {
                FilterChip(
                    label = stringResource(R.string.server_sort_speed),
                    isSelected = viewModel.activeSort == SortCriteria.SPEED,
                    onClick = { viewModel.setSortCriteria(SortCriteria.SPEED) }
                )
            }
            item {
                FilterChip(
                    label = stringResource(R.string.server_sort_sessions),
                    isSelected = viewModel.activeSort == SortCriteria.SESSIONS,
                    onClick = { viewModel.setSortCriteria(SortCriteria.SESSIONS) }
                )
            }
            // Residential-only view: likely home connections first (they attract
            // fewer bot-checks than datacenter IPs), best-ranked among equals.
            item {
                FilterChip(
                    label = stringResource(R.string.server_filter_residential, viewModel.residentialCount),
                    isSelected = viewModel.residentialOnly,
                    onClick = { viewModel.residentialOnly = !viewModel.residentialOnly }
                )
            }
            item {
                FilterChip(
                    label = stringResource(R.string.server_filter_favorites, viewModel.favoriteIps.size),
                    isSelected = viewModel.favoritesOnly,
                    onClick = { viewModel.favoritesOnly = !viewModel.favoritesOnly }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Server List or Empty State
        if (viewModel.filteredServers.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.lastLoadError != null && viewModel.displayServers.isEmpty()) {
                    // The directory fetch itself failed — say so honestly and offer a retry.
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.server_fetch_failed),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = viewModel.lastLoadError ?: "Could not reach the server directory",
                            style = TechnicalData,
                            color = TextMuted.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FilterChip(
                            label = stringResource(R.string.server_retry),
                            isSelected = false,
                            onClick = { viewModel.loadServers() }
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.server_none_found),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (viewModel.residentialOnly)
                                "No likely-residential nodes match — try clearing other filters"
                            else
                                "Try a different search or country filter",
                            style = TechnicalData,
                            color = TextMuted.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = viewModel.filteredServers,
                    key = { it.ip },
                    contentType = { "server_node" }
                ) { srv ->
                    val isSelected = srv.ip == viewModel.selectedServer?.ip
                    AdvancedServerCard(
                        server = srv,
                        isSelected = isSelected,
                        isFavorite = viewModel.favoriteIps.contains(srv.ip),
                        onFavoriteClick = { viewModel.toggleFavorite(srv.ip) },
                        onClick = { onServerSelected(srv) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}


// ── Advanced Server Card (Optimized 120 FPS Rendering) ─────────────────────────

@Composable
private fun AdvancedServerCard(
    server: VpnServerDisplay,
    isSelected: Boolean,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    // Sub-15ms reports are physically implausible for an internet relay — treat
    // them as suspect data, not as a great latency.
    val pingColor = when {
        server.ping < 15 -> Color(0xFFFFB300)
        server.ping < 60 -> Color(0xFF00E676)
        server.ping < 120 -> Color(0xFF00E5FF)
        else -> Color(0xFFFFB300)
    }

    val qualityColor = when {
        server.qualityScore > 0.7f -> Color(0xFF00E676)
        server.qualityScore > 0.4f -> Color(0xFF00E5FF)
        else -> Color(0xFFFFB300)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                1.dp,
                if (isSelected) Primary else BorderHairline,
                RoundedCornerShape(10.dp)
            )
            .background(if (isSelected) Color(0xFF181818) else Color(0xFF101010))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flag Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E1E), CircleShape)
                    .border(1.dp, BorderHairline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = server.flagEmoji,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = server.countryLong,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (server.isResidential) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.server_residential_badge),
                            style = LabelCaps.copy(fontSize = 7.sp, fontWeight = FontWeight.Bold),
                            color = Color(0xFF00E5FF),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.10f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .border(1.dp, StatusSuccess, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.server_active_badge),
                                style = LabelCaps.copy(fontSize = 8.sp),
                                color = StatusSuccess
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = server.ip,
                    style = TechnicalData,
                    color = TextMuted
                )
            }

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                    contentDescription = if (isFavorite) "إزالة من المفضلة" else "إضافة إلى المفضلة",
                    tint = if (isFavorite) StatusSuccess else TextMuted
                )
            }

            // Ping badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = server.pingText,
                    style = TechnicalData.copy(fontWeight = FontWeight.SemiBold),
                    color = pingColor
                )
                Text(
                    text = server.formattedSpeed,
                    style = TechnicalData.copy(fontSize = 10.sp),
                    color = TextMuted
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.server_selected),
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quality bar + stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quality progress bar (Lightweight zero-allocation Box)
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.server_quality),
                        style = LabelCaps.copy(fontSize = 9.sp),
                        color = TextMuted
                    )
                    Text(
                        text = "${(server.qualityScore * 100).toInt()}%",
                        style = LabelCaps.copy(fontSize = 9.sp),
                        color = qualityColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF1E1E1E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(server.qualityScore.coerceIn(0.01f, 1f))
                            .fillMaxHeight()
                            .background(qualityColor)
                    )
                }
            }

            // Score metric
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.server_score),
                    style = LabelCaps.copy(fontSize = 9.sp),
                    color = TextMuted
                )
                Text(
                    text = server.formattedScore,
                    style = TechnicalData.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = Primary
                )
            }

            // Sessions count
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.server_sessions),
                    style = LabelCaps.copy(fontSize = 9.sp),
                    color = TextMuted
                )
                Text(
                    text = "${server.numVpnSessions}",
                    style = TechnicalData.copy(fontSize = 11.sp),
                    color = TextMuted
                )
            }
        }
    }
}



// ── Filter Chip ─────────────────────────────────────────────────────────────────

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .border(
                1.dp,
                if (isSelected) Primary else BorderHairline,
                RoundedCornerShape(6.dp)
            )
            .background(if (isSelected) Color(0xFF222222) else Color(0xFF141414))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = LabelCaps.copy(fontSize = 10.sp),
            color = if (isSelected) Primary else TextMuted
        )
    }
}
