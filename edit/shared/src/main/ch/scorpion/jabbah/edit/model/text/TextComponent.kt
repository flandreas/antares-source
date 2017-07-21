package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.geom.RoundRectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.TextRenderInfo
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.rectangle.RectangularComponent
import ch.scorpion.jabbah.edit.style.EditStyleType
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter


/**
 * A [Component] with text that can be interactively edited by the user.
 */
interface TextComponent : Component, RectangularShape {

    var text: String

    // Requested by the compiler due to multiple inheritance problem
    override fun contains(x: Double, y: Double): Boolean {
        throw UnsupportedOperationException("not implemented")
    }
}

interface TextComponentFactory {

    /**
     * Creates a new platform-specific [TextComponent] implementation.
     * @param text the text to be displayed
     * @param location the location of the text baseline point
     */
    fun create(
            text: String,
            location: Point2D,
            styleType: StyleType,
            styleProvider: StyleProvider
    ): TextComponent
}

open class TextComponentImpl(
        text: String = "",
        location: Point2D = Point2D(),
        styleType: StyleType = EditStyleType.MESSAGE,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        private val textRenderInfoFactory: (String, Font) -> TextRenderInfo = DrawModule.textRenderInfoFactory
) : RectangularComponent(
        styleType = styleType,
        styleProvider = styleProvider,
        bounds = Rectangle2D(location.x, location.y, 0.0, 0.0)
    ),
    Transparent, TextComponent
{
    private companion object {

        /** The horizontal inset between the bounding box and the text.  */
        private val INSET_X = 10

        /** The vertical inset between the bounding box and the text.  */
        private val INSET_Y = 10
    }

    private val transparent = TransparentImpl(this)

    override var transparency: Int
        get() = transparent.transparency
        set(value) {
            transparent.transparency = value
        }

    // TODO Use Styles to determine Colors!
    private var decorator: TextComponentDecorator = RectangularShapeTextComponentDecorator(
            shape = RoundRectangle2D(0.0, 0.0, 0.0, 0.0, 20.0, 20.0),
            backgroundColor = backgroundColor,
            foregroundColor = foregroundColor,
            stroke = stroke,
            transparent = transparent
    )

    init {
        adjustBounds()
    }

    /** ---- [TextComponent] interface */

    override var text: String = text
        set(value) {
            invalidate()
            field = value
            invalidate()
            validate()
        }

    /** ---- [Drawable] */

    override fun contains(x: Double, y: Double): Boolean {
        return super<RectangularComponent>.contains(x, y)
    }

    override fun draw(context: DrawContext) {
        val oldClip = context.g.getClipBounds()
        val b = bounds

        decorator.drawBackground(this, context)

        //setupTextPainter()
        //(context.g as Graphics2DJvm).g.setClip(b.x.toInt(), b.y.toInt(), b.width.toInt(), b.height.toInt())
        //context.g.translate(TextComponentJvm.TEXT_PAINTER.x.toDouble(), TextComponentJvm.TEXT_PAINTER.y.toDouble())
        //TextComponentJvm.TEXT_PAINTER.paint((context.g as Graphics2DJvm).g)
        //context.g.translate(-TextComponentJvm.TEXT_PAINTER.x.toDouble(), -TextComponentJvm.TEXT_PAINTER.y.toDouble())
        //(context.g as Graphics2DJvm).g.setClip(oldClip.x.toInt(), oldClip.y.toInt(), oldClip.width.toInt(), oldClip.height.toInt())



        decorator.drawForeground(this, context)
    }

    /** ---- [Stylable] */

    override var styleType: StyleType
        get() = super.styleType
        set(value) {
            super.styleType = value
            decorator.backgroundColor = backgroundColor
            decorator.foregroundColor = foregroundColor
            decorator.stroke = stroke
        }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("text", text)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        text = reader.readString("text")
    }

    /** ---- [TextComponentImpl] */

    /**
     * Adjusts the size of this [TextComponentImpl]'s bounds to the preferred size of the current text, expanded by
     * the constant horizontal and vertical insets.
     *
     * Currently, this method only changes the width and height of the bounds, but not its position. It should be
     * considered whether or not this method should also change the position of the bounds, depending on the current
     * alignment settings.
     */
    private fun adjustBounds() {
        // TODO change the position of the bounding box depending on alignment?
        // Note that TextEditTool uses its own adjustment strategy for inline editing,
        // which does change the position depending on alignment
        val b = bounds
        val tri = textRenderInfoFactory.invoke(text, font)
        setFrame(b.x, b.y, tri.textBounds.width + 2 * INSET_X, tri.textBounds.height + 2 * INSET_Y)
    }

}