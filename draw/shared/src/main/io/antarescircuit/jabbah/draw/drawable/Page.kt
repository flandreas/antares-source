package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.Dimension2D
import io.antarescircuit.jabbah.base.geom.Margin
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.DropShadow.PROP_OFFSET
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.style.DrawTheme
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.draw.style.Themes
import kotlin.math.max

/**
 * Represents a standardised printable page size.
 *
 * @property customName the translated, displayable name, e.g. "DIN A4"
 * @property dimension the width and height in millimeter
 */
data class PageSize(
    val customName: String,
    val dimension: Dimension2D
) {
    companion object {
        val A4 = PageSize("A4", Dimension2D(210, 297))
        val A3 = PageSize("A3", Dimension2D(297, 420))
        val A2 = PageSize("A2", Dimension2D(420, 594))

        val PREDEFINED = listOf(A4, A3, A2)

        fun predefinedWithName(customName: String): PageSize =
            PREDEFINED.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("Custom name $customName not found")
    }

    override fun toString(): String = customName

    /**
     * The width of this [PageSize] in pixel if rendered with the given [Resolution].
     */
    fun widthByResolution(resolution: Resolution): Int =
        resolution.millimeterToPixel(dimension.width.toInt())

    /**
     * The height of this [PageSize] in pixel if rendered with the given [Resolution].
     */
    fun heightByResolution(resolution: Resolution): Int =
        resolution.millimeterToPixel(dimension.height.toInt())
}

enum class PageOrientation(
    val customName: String,
    val nameKey: String
) {
    PORTRAIT("portrait", "draw.pageOrientation.portrait.name"),
    LANDSCAPE("landscape", "draw.pageOrientation.landscape.name");

    companion object {
        fun withName(customName: String): PageOrientation =
            entries.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("Custom name $customName not found")
    }

    override fun toString(): String = Translations.getString(nameKey)
}

data class Page(
    val size: PageSize,
    val orientation: PageOrientation,
    val margin: Margin = DEF_MARGIN
) {
    companion object {
        val DEF_MARGIN = Margin.allOf(15)
    }

    val width: Int = when(orientation) {
        PageOrientation.PORTRAIT -> size.dimension.widthInt
        PageOrientation.LANDSCAPE -> size.dimension.heightInt
    }

    val height: Int = when(orientation) {
        PageOrientation.PORTRAIT -> size.dimension.heightInt
        PageOrientation.LANDSCAPE -> size.dimension.widthInt
    }

    val shape: Rectangle2D = Rectangle2D(0, 0, width, height)

    val usableRectangle: Rectangle2D = margin.reduce(Rectangle2D(shape))
}

class PageView(
    private val page: Page,
    private val styleProvider: StyleProvider
) : AbstractRectangle(shape = Rectangle2D()) {

    companion object {
        private val SHADOW_DIST = BaseModule.properties.getInt(PROP_OFFSET)
        private val BORDER_STROKE = Stroke(0.5f)
        private val MARGIN_STROKE = Stroke(0.3f)
    }

    override val boundingBox: RectangularShape =
        Rectangle2D(0.0, 0.0, page.shape.width + SHADOW_DIST, page.shape.height + SHADOW_DIST)

    override val lineWidth: Double get() = max(BORDER_STROKE.width, MARGIN_STROKE.width).toDouble()

    init {
        setBounds(0.0, 0.0, page.shape.width, page.shape.height)
    }

    override fun draw(context: DrawContext) {
        val foregroundColor = styleProvider.getStyle(StyleType.FIGURE).color.foregroundColor
        context.g.color = styleProvider.getStyle(StyleType.FIGURE).color.foregroundColor
        drawProprietaryShadow(context)
        drawProprietaryShape(context, foregroundColor)
        drawMargin(context, foregroundColor)
    }

    private fun drawProprietaryShadow(context: DrawContext) {
        context.translated(SHADOW_DIST.toDouble(), SHADOW_DIST.toDouble()) { c ->
            drawFill(context, shape, Themes.get<DrawTheme>().shadow.foregroundColor)
        }
    }

    private fun drawProprietaryShape(context: DrawContext, foregroundColor: Color) {
        drawFill(context, shape, Themes.get<DrawTheme>().background.color.backgroundColor)
        drawStroke(context, shape, foregroundColor, BORDER_STROKE)
    }

    private fun drawMargin(context: DrawContext, foregroundColor: Color) {
        if (page.margin != Margin.NONE) {
            drawStroke(context, page.usableRectangle, foregroundColor.between(Themes.get<DrawTheme>().background.color.backgroundColor, 0.5f), MARGIN_STROKE)
        }
    }
}