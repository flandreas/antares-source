package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RoundRectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.TextRenderInfo
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.style.EditStyleType
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A simple, non-editable [TextComponent] that uses a [MultilineText] for text rendering.
 */
class SimpleTextComponent(
        text: String = "",
        location: Point2D = Point2D(),
        styleType: StyleType = EditStyleType.MESSAGE,
        styleProvider: StyleProvider = DrawStyleModule.styleProvider,
        private val textRenderInfoFactory: (String, Font) -> TextRenderInfo = DrawModule.textRenderInfoFactory
) : RectangleComponent(
        styleType = styleType,
        styleProvider = styleProvider,
        shape = Rectangle2D(location.x, location.y, 100.0, 50.0)
), TextComponent, Transparent {

    private companion object {

        /** The horizontal inset between the bounding box and the text.  */
        private val INSET_X = 10

        /** The vertical inset between the bounding box and the text.  */
        private val INSET_Y = 10
    }

    /** ---- [Transparent] interface */

    private val transparent = TransparentImpl(this)

    override var transparency: Int
        get() = transparent.transparency
        set(value) {
            transparent.transparency = value
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

    /** ---- [TextComponent] interface */

    override var text: String = text
        set(value) {
            invalidate()
            field = value
            updateMultilineText()
            invalidate()
            validate()
        }

    /** ---- [Drawable] */

    override fun contains(x: Double, y: Double): Boolean {
        return super<RectangleComponent>.contains(x, y)
    }

    override fun contains(p: Point2D): Boolean {
        return super<RectangleComponent>.contains(p)
    }

    override fun draw(context: DrawContext) {
        decorator.drawBackground(this, context)

        context.g.font = font
        context.g.color = textColor
        val b = shape

        // TODO: Implement clipping in JavaScript platform
        // val oldClip = context.g.getClipBounds()
        //(context.g as Graphics2DJvm).g.setClip(b.x.toInt(), b.y.toInt(), b.width.toInt(), b.height.toInt())
        multilineText.draw(context, x.toInt() + INSET_X, y.toInt() + INSET_Y)
        //(context.g as Graphics2DJvm).g.setClip(oldClip.x.toInt(), oldClip.y.toInt(), oldClip.width.toInt(), oldClip.height.toInt())

        decorator.drawForeground(this, context)
    }

    override fun update() {
        updateMultilineText()
        super.update()
    }

    /** ---- [SimpleTextComponent] */

    private var multilineText = MultilineText(text, font, width.toInt() - 2 * INSET_X, font.size)

    private var decorator: TextComponentDecorator = RectangularShapeTextComponentDecorator(
            shape = RoundRectangle2D(0.0, 0.0, 0.0, 0.0, 20.0, 20.0),
            stylable = this,
            transparent = transparent
    )

    init {
        updateMultilineText()
    }

    private fun updateMultilineText() {
        multilineText = MultilineText(text, font, width.toInt() - 2 * INSET_X, font.size)
    }
}