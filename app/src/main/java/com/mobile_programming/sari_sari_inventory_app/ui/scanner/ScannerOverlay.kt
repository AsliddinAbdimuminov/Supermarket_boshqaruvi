package com.mobile_programming.sari_sari_inventory_app.ui.scanner

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import kotlin.math.abs

class ScannerOverlay(
    val size: Float,
    borderRadius: Float,
    val viewWidth: Float,
    val viewHeight: Float
) {
    private val quadrantOffset = (size / 4)

    private val canvasOffsetX = (viewWidth - size) / 2f
    private val canvasOffsetY = (viewHeight - size) / 2f

    val rect = Rect(
        Offset(canvasOffsetX, canvasOffsetY),
        Size(size, size)
    )

    private val startingPoints = listOf(
        PointF(rect.left, rect.centerLeft.y - quadrantOffset),
        PointF(rect.topCenter.x + quadrantOffset, rect.top),
        PointF(rect.right, rect.centerRight.y + quadrantOffset),
        PointF(rect.bottomCenter.x - quadrantOffset, rect.bottom)
    )

    val path = Path().apply {

        startingPoints.forEach { point ->
            val pointXToCenterX = abs(rect.center.x - point.x)
            val pointYToCenterY = abs(rect.center.y - point.y)

            // Modifier affects how to the path will move based on quadrant
            val modifierX =
                if(point.x > rect.center.x) 1
                else -1
            val modifierY =
                if(point.y > rect.center.y) 1
                else -1

            // X and Y of where the curve begins relative to starting point
            val curveStartX =
                if(pointXToCenterX < pointYToCenterY) {
                    point.x + (((size / 2) - quadrantOffset - borderRadius) * modifierX)
                } else {
                    point.x
                }
            val curveStartY =
                if(pointXToCenterX < pointYToCenterY) {
                    point.y
                } else {
                    point.y + (((size / 2) - quadrantOffset - borderRadius) * modifierY)
                }

            // X and Y of quadrant corner
            val cornerX =
                if(pointXToCenterX < pointYToCenterY) {
                    curveStartX + (borderRadius * modifierX)
                } else {
                    curveStartX
                }
            val cornerY =
                if(pointXToCenterX < pointYToCenterY) {
                    point.y
                } else {
                    curveStartY + (borderRadius * modifierY)
                }

            // X and Y of where the curve ends relative to starting point
            val curveEndX =
                if(pointXToCenterX < pointYToCenterY) {
                    cornerX
                } else {
                    cornerX - (borderRadius * modifierX)
                }
            val curveEndY =
                if(pointXToCenterX < pointYToCenterY) {
                    cornerY - (borderRadius * modifierY)
                } else {
                    cornerY
                }

            // X and Y of where the border ends
            val quadrantEndX =
                if(pointXToCenterX < pointYToCenterY) {
                    cornerX
                } else {
                    cornerX - (((size / 2) - quadrantOffset) * modifierX)
                }
            val quadrantEndY =
                if(pointXToCenterX < pointYToCenterY) {
                    cornerY + (((size / 2) - quadrantOffset) * modifierX)
                } else {
                    cornerY
                }

            moveTo(point.x, point.y)
            lineTo(curveStartX, curveStartY)
            cubicTo(
                curveStartX, curveStartY,
                cornerX, cornerY,
                curveEndX, curveEndY
            )
            lineTo(quadrantEndX, quadrantEndY)
        }

    }
}