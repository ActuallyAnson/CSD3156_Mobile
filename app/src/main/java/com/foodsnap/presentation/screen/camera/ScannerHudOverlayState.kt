package com.foodsnap.presentation.screen.camera

import androidx.compose.ui.geometry.Rect
import com.foodsnap.presentation.navigation.CameraMode

data class ScannerHudOverlayState(
    val mode: CameraMode,
    val statusText: String,
    val labels: List<ScannerHudLabel>,
    val scanEnabled: Boolean,
    val boxes: List<ScannerHudBox>,
    val qualityHints: ScannerHudQualityHints = ScannerHudQualityHints()
)

data class ScannerHudLabel(
    val name: String,
    val confidence: Float
)

data class ScannerHudBox(
    val imageRect: Rect,
    val imageWidth: Int,
    val imageHeight: Int,
    val label: String? = null,
    val confidence: Float? = null,
    val isMirrored: Boolean = false
)

data class ScannerHudQualityHints(
    val lowConfidence: Boolean = false,
    val lowLight: Boolean = false
)

