package com.payaq.opengatevpn.ui.theme

import androidx.compose.ui.graphics.Color

// ── Syria VPN — national color palette ─────────────────────────────────────────

// Canvas & Surfaces
val Void            = Color(0xFF000000)  // Pitch-black background
val Surface         = Color(0xFF131313)  // Primary surface
val SurfaceDim      = Color(0xFF131313)
val SurfaceContainer      = Color(0xFF1F1F1F)
val SurfaceContainerLow   = Color(0xFF1B1B1B)
val SurfaceContainerHigh  = Color(0xFF2A2A2A)
val SurfaceContainerHighest = Color(0xFF353535)
val SurfaceContainerLowest  = Color(0xFF0E0E0E)
val SurfaceBright   = Color(0xFF393939)
val CardBackground  = Color(0xFF0A0A0A)  // Card / elevated surface bg

// Borders
val BorderHairline  = Color(0xFF222222)  // 1px micro-border
val OutlineVariant  = Color(0xFF444748)
val Outline         = Color(0xFF8E9192)

// Text & Content
val OnSurface       = Color(0xFFE2E2E2)  // Primary text on dark surfaces
val OnSurfaceVariant = Color(0xFFC4C7C8)
val TextMuted       = Color(0xFF888888)  // Secondary / muted text

// Syria-inspired accents: green, white, black, and red.
val SyriaGreen      = Color(0xFF007A3D)
val SyriaRed        = Color(0xFFCE1126)
val SyriaWhite      = Color(0xFFFFFFFF)
val Primary         = SyriaGreen
val OnPrimary       = SyriaWhite
val PrimaryContainer = Color(0xFF0B3D26)

// Secondary
val Secondary       = SyriaWhite
val SecondaryContainer = Color(0xFF252525)

// Functional Status
val StatusSuccess   = SyriaGreen
val StatusError     = SyriaRed
val StatusPaused    = Color(0xFFFFBF00)  // Amber — Paused state

// Error (Material3 slots)
val ErrorColor      = Color(0xFFFFB4AB)
val OnError         = Color(0xFF690005)
val ErrorContainer  = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)