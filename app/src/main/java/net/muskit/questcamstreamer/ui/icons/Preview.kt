// sources:
// https://composeicons.com/icons/material-symbols/outlined/preview
// https://composeicons.com/icons/material-symbols/outlined/preview_off
package net.muskit.questcamstreamer.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val PreviewOn: ImageVector
    get() {
        if (_PreviewOn != null) {
            return _PreviewOn!!
        }
        _PreviewOn = ImageVector.Builder(
            name = "Preview",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(200f, 840f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(120f, 760f)
                verticalLineToRelative(-560f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(200f, 120f)
                horizontalLineToRelative(560f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(840f, 200f)
                verticalLineToRelative(560f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(760f, 840f)
                close()
                moveToRelative(0f, -80f)
                horizontalLineToRelative(560f)
                verticalLineToRelative(-480f)
                horizontalLineTo(200f)
                close()
                moveToRelative(280f, -80f)
                quadToRelative(-82f, 0f, -146.5f, -44.5f)
                reflectiveQuadTo(240f, 520f)
                quadToRelative(29f, -71f, 93.5f, -115.5f)
                reflectiveQuadTo(480f, 360f)
                reflectiveQuadToRelative(146.5f, 44.5f)
                reflectiveQuadTo(720f, 520f)
                quadToRelative(-29f, 71f, -93.5f, 115.5f)
                reflectiveQuadTo(480f, 680f)
                moveToRelative(0f, -60f)
                quadToRelative(56f, 0f, 102f, -26.5f)
                reflectiveQuadToRelative(72f, -73.5f)
                quadToRelative(-26f, -47f, -72f, -73.5f)
                reflectiveQuadTo(480f, 420f)
                reflectiveQuadToRelative(-102f, 26.5f)
                reflectiveQuadToRelative(-72f, 73.5f)
                quadToRelative(26f, 47f, 72f, 73.5f)
                reflectiveQuadTo(480f, 620f)
                moveToRelative(0f, -40f)
                quadToRelative(25f, 0f, 42.5f, -17.5f)
                reflectiveQuadTo(540f, 520f)
                reflectiveQuadToRelative(-17.5f, -42.5f)
                reflectiveQuadTo(480f, 460f)
                reflectiveQuadToRelative(-42.5f, 17.5f)
                reflectiveQuadTo(420f, 520f)
                reflectiveQuadToRelative(17.5f, 42.5f)
                reflectiveQuadTo(480f, 580f)
            }
        }.build()
        return _PreviewOn!!
    }

public val PreviewOff: ImageVector
    get() {
        if (_PreviewOff != null) {
            return _PreviewOff!!
        }
        _PreviewOff = ImageVector.Builder(
            name = "Preview_off",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(480f, 680f)
                quadToRelative(-82f, 0f, -146.5f, -44.5f)
                reflectiveQuadTo(240f, 520f)
                quadToRelative(20f, -48f, 56f, -84f)
                reflectiveQuadToRelative(84f, -56f)
                lineToRelative(47f, 47f)
                quadToRelative(-39f, 11f, -70f, 34.5f)
                reflectiveQuadTo(306f, 520f)
                quadToRelative(26f, 47f, 72f, 73.5f)
                reflectiveQuadTo(480f, 620f)
                quadToRelative(30f, 0f, 58f, -8f)
                reflectiveQuadToRelative(51f, -23f)
                lineToRelative(43f, 43f)
                quadToRelative(-32f, 23f, -70.5f, 35.5f)
                reflectiveQuadTo(480f, 680f)
                moveToRelative(209f, -104f)
                lineToRelative(-43f, -43f)
                quadToRelative(2f, -3f, 4f, -6.5f)
                reflectiveQuadToRelative(4f, -6.5f)
                quadToRelative(-18f, -33f, -47f, -56.5f)
                reflectiveQuadTo(542f, 429f)
                lineToRelative(-69f, -69f)
                quadToRelative(82f, 0f, 150f, 44.5f)
                reflectiveQuadTo(720f, 520f)
                quadToRelative(-6f, 15f, -13.5f, 29f)
                reflectiveQuadTo(689f, 576f)
                moveTo(791f, 904f)
                lineToRelative(-64f, -64f)
                horizontalLineTo(200f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(120f, 760f)
                verticalLineToRelative(-527f)
                lineToRelative(-64f, -65f)
                lineToRelative(56f, -56f)
                lineToRelative(736f, 736f)
                close()
                moveTo(200f, 760f)
                horizontalLineToRelative(447f)
                lineTo(200f, 313f)
                close()
                moveToRelative(640f, -33f)
                lineToRelative(-80f, -80f)
                verticalLineToRelative(-327f)
                horizontalLineTo(433f)
                lineTo(233f, 120f)
                horizontalLineToRelative(527f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(840f, 200f)
                close()
            }
        }.build()
        return _PreviewOff!!
    }

private var _PreviewOn: ImageVector? = null
private var _PreviewOff: ImageVector? = null
