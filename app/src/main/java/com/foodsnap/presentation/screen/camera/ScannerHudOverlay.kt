package com.foodsnap.presentation.screen.camera

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.foodsnap.presentation.navigation.CameraMode
import kotlin.math.max
import kotlin.math.min

@Composable
fun ScannerHudOverlay(
    state: ScannerHudOverlayState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        CanvasShapesOverlay(
            state = state,
            modifier = Modifier.fillMaxSize()
        )
        OverlayTextLayer(
            state = state,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CanvasShapesOverlay(
    state: ScannerHudOverlayState,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "scanLine")
    val scanT by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanT"
    )

    Canvas(modifier = modifier) {
        val barcodeFramePaddingPx = 12.dp.toPx()
        val frameRect = computeEffectiveFrameRect(
            overlaySize = size,
            mode = state.mode,
            boxes = state.boxes,
            barcodeFramePaddingPx = barcodeFramePaddingPx
        )

        val frameStroke = Stroke(width = 2.dp.toPx())
        val cornerRadius = 18.dp.toPx()
        val frameColor = Color.White.copy(alpha = 0.9f)
        val drawFullFrameOutline = state.mode != CameraMode.BARCODE
        val drawCornerTicks = state.mode == CameraMode.BARCODE
        if (drawFullFrameOutline) {
            drawRoundRect(
                color = frameColor,
                topLeft = frameRect.topLeft,
                size = frameRect.size,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = frameStroke
            )
        }

        if (drawCornerTicks) {
            val tickLen = 22.dp.toPx()
            val tickStroke = 3.dp.toPx()
            val inset = 2.dp.toPx()
            fun line(start: Offset, end: Offset) {
                drawLine(
                    color = frameColor,
                    start = start,
                    end = end,
                    strokeWidth = tickStroke,
                    cap = StrokeCap.Round
                )
            }

            val tl = frameRect.topLeft + Offset(inset, inset)
            val tr = frameRect.topRight + Offset(-inset, inset)
            val bl = frameRect.bottomLeft + Offset(inset, -inset)
            val br = frameRect.bottomRight + Offset(-inset, -inset)

            line(tl, tl + Offset(tickLen, 0f))
            line(tl, tl + Offset(0f, tickLen))

            line(tr, tr + Offset(-tickLen, 0f))
            line(tr, tr + Offset(0f, tickLen))

            line(bl, bl + Offset(tickLen, 0f))
            line(bl, bl + Offset(0f, -tickLen))

            line(br, br + Offset(-tickLen, 0f))
            line(br, br + Offset(0f, -tickLen))
        }

        val lineInset = 10.dp.toPx()
        val lineY = lerp(frameRect.top + lineInset, frameRect.bottom - lineInset, scanT)
        val glowColor = Color(0xFF00E5FF).copy(alpha = 0.25f)
        val scanColor = Color(0xFF00E5FF).copy(alpha = 0.85f)
        drawLine(
            color = glowColor,
            start = Offset(frameRect.left + lineInset, lineY),
            end = Offset(frameRect.right - lineInset, lineY),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = scanColor,
            start = Offset(frameRect.left + lineInset, lineY),
            end = Offset(frameRect.right - lineInset, lineY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        if (!(state.mode == CameraMode.BARCODE && state.boxes.isNotEmpty())) {
            clipRect {
                state.boxes.forEach { box ->
                    if (box.imageWidth <= 0 || box.imageHeight <= 0) return@forEach
                    val mapped = mapImageRectToOverlayRect(
                        imageRect = box.imageRect,
                        imageWidth = box.imageWidth,
                        imageHeight = box.imageHeight,
                        overlayWidth = size.width,
                        overlayHeight = size.height,
                        isMirrored = box.isMirrored
                    )

                    val boxColor = Color(0xFF7CFF6B).copy(alpha = 0.9f)
                    drawRoundRect(
                        color = boxColor,
                        topLeft = mapped.topLeft,
                        size = mapped.size,
                        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    val tagW = min(mapped.width, 120.dp.toPx())
                    val tagH = 18.dp.toPx()
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.55f),
                        topLeft = Offset(mapped.left, max(0f, mapped.top - tagH - 4.dp.toPx())),
                        size = Size(tagW, tagH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlayTextLayer(
    state: ScannerHudOverlayState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (state.mode) {
                    CameraMode.BARCODE -> "Barcode"
                    CameraMode.INGREDIENT -> "Ingredient"
                    CameraMode.DISH -> "Dish"
                },
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.95f)
            )
            Text(
                text = state.statusText,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.95f)
            )
            if (state.qualityHints.lowConfidence) {
                Text(
                    text = "Low confidence",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFD54F).copy(alpha = 0.95f)
                )
            }
        }

        if (state.labels.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = Color.Black.copy(alpha = 0.55f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.labels.take(3).forEach { label ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "${(label.confidence.coerceIn(0f, 1f) * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            ) {
                                val barRadius = 6.dp.toPx()
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.18f),
                                    cornerRadius = CornerRadius(barRadius, barRadius)
                                )
                                drawRoundRect(
                                    color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                                    size = Size(
                                        width = size.width * label.confidence.coerceIn(0f, 1f),
                                        height = size.height
                                    ),
                                    cornerRadius = CornerRadius(barRadius, barRadius)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun computeScanFrameRect(overlaySize: Size, mode: CameraMode): Rect {
    val frameWidth: Float
    val frameHeight: Float
    when (mode) {
        CameraMode.BARCODE -> {
            frameWidth = overlaySize.width * 0.82f
            frameHeight = frameWidth * 0.45f
        }

        CameraMode.INGREDIENT,
        CameraMode.DISH -> {
            val side = min(overlaySize.width * 0.72f, overlaySize.height * 0.45f)
            frameWidth = side
            frameHeight = side
        }
    }

    val minSideMargin = overlaySize.width * 0.08f
    val left = ((overlaySize.width - frameWidth) / 2f).coerceAtLeast(minSideMargin)
    val right = (left + frameWidth).coerceAtMost(overlaySize.width - minSideMargin)

    val rawTop = (overlaySize.height - frameHeight) / 2f
    val minTop = overlaySize.height * 0.18f
    val maxTop = (overlaySize.height - frameHeight - overlaySize.height * 0.18f).coerceAtLeast(minTop)
    val top = rawTop.coerceIn(minTop, maxTop)
    val bottom = top + frameHeight

    return Rect(left, top, right, bottom)
}

private fun computeEffectiveFrameRect(
    overlaySize: Size,
    mode: CameraMode,
    boxes: List<ScannerHudBox>,
    barcodeFramePaddingPx: Float
): Rect {
    if (mode == CameraMode.BARCODE && boxes.isNotEmpty()) {
        val box = boxes.first()
        if (box.imageWidth > 0 && box.imageHeight > 0) {
            val mapped = mapImageRectToOverlayRect(
                imageRect = box.imageRect,
                imageWidth = box.imageWidth,
                imageHeight = box.imageHeight,
                overlayWidth = overlaySize.width,
                overlayHeight = overlaySize.height,
                isMirrored = box.isMirrored
            )

            val expanded = Rect(
                left = mapped.left - barcodeFramePaddingPx,
                top = mapped.top - barcodeFramePaddingPx,
                right = mapped.right + barcodeFramePaddingPx,
                bottom = mapped.bottom + barcodeFramePaddingPx
            )

            val minW = overlaySize.width * 0.35f
            val minH = minW * 0.28f
            val w = max(expanded.width, minW)
            val h = max(expanded.height, minH)
            val cx = expanded.center.x
            val cy = expanded.center.y
            val resized = Rect(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f)
            val clamped = clampRect(resized, overlaySize)
            if (clamped != Rect.Zero) return clamped
        }
    }

    return computeScanFrameRect(overlaySize, mode)
}

private fun clampRect(rect: Rect, overlaySize: Size): Rect {
    val left = rect.left.coerceIn(0f, overlaySize.width)
    val top = rect.top.coerceIn(0f, overlaySize.height)
    val right = rect.right.coerceIn(0f, overlaySize.width)
    val bottom = rect.bottom.coerceIn(0f, overlaySize.height)
    return if (right <= left || bottom <= top) Rect.Zero else Rect(left, top, right, bottom)
}

internal fun mapImageRectToOverlayRect(
    imageRect: Rect,
    imageWidth: Int,
    imageHeight: Int,
    overlayWidth: Float,
    overlayHeight: Float,
    isMirrored: Boolean
): Rect {
    val iw = imageWidth.toFloat()
    val ih = imageHeight.toFloat()
    if (iw <= 0f || ih <= 0f || overlayWidth <= 0f || overlayHeight <= 0f) return Rect.Zero

    val scale = max(overlayWidth / iw, overlayHeight / ih)
    val dx = (overlayWidth - iw * scale) / 2f
    val dy = (overlayHeight - ih * scale) / 2f

    val left = imageRect.left * scale + dx
    val top = imageRect.top * scale + dy
    val right = imageRect.right * scale + dx
    val bottom = imageRect.bottom * scale + dy

    val mapped = Rect(left, top, right, bottom)
    if (!isMirrored) return mapped

    return Rect(
        left = overlayWidth - mapped.right,
        top = mapped.top,
        right = overlayWidth - mapped.left,
        bottom = mapped.bottom
    )
}

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t.coerceIn(0f, 1f)
