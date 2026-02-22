package com.foodsnap.presentation.screen.camera

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayCoordinateMapperTest {

    @Test
    fun `mapImageRectToOverlayRect center-crops using max scale`() {
        // Image is 4:3, overlay is 16:9 => expect vertical crop in image mapped to full width.
        val imageW = 400
        val imageH = 300
        val overlayW = 1600f
        val overlayH = 900f

        // A 100x100 square at image center
        val imageRect = Rect(150f, 100f, 250f, 200f)

        val mapped = mapImageRectToOverlayRect(
            imageRect = imageRect,
            imageWidth = imageW,
            imageHeight = imageH,
            overlayWidth = overlayW,
            overlayHeight = overlayH,
            isMirrored = false
        )

        // scale = max(1600/400=4, 900/300=3) = 4
        // dx = 0, dy = (900 - 300*4)/2 = (900 - 1200)/2 = -150
        assertEquals(600f, mapped.left, 0.01f)
        assertEquals(250f, mapped.top, 0.01f)
        assertEquals(1000f, mapped.right, 0.01f)
        assertEquals(650f, mapped.bottom, 0.01f)
    }

    @Test
    fun `mapImageRectToOverlayRect mirrors horizontally when requested`() {
        val mapped = mapImageRectToOverlayRect(
            imageRect = Rect(0f, 0f, 100f, 50f),
            imageWidth = 200,
            imageHeight = 100,
            overlayWidth = 1000f,
            overlayHeight = 500f,
            isMirrored = true
        )

        // scale = 5, dx=0, so unmirrored would be [0..500]
        // mirrored => [1000-500 .. 1000-0] = [500..1000]
        assertEquals(500f, mapped.left, 0.01f)
        assertEquals(0f, mapped.top, 0.01f)
        assertEquals(1000f, mapped.right, 0.01f)
        assertEquals(250f, mapped.bottom, 0.01f)
    }
}

