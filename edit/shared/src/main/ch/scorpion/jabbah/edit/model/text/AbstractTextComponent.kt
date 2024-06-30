package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.geom.RoundRectangle2D
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.drawable.TransparentImpl
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.rectangle.AbstractRectangularComponent
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.jvm.JvmStatic

abstract class AbstractTextComponent(
    location: Point2D = Point2D.ZERO,
    shape: RectangularShape = Rectangle2D(location.x, location.y, 0.0, 0.0),
    styleType: StyleType = StyleType.TEXT,
    styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangularComponent(
    styleType = styleType,
    styleProvider = styleProvider,
    shape = shape
), TextComponent, Transparent {

    companion object {

        protected val TYPE = Translations.getString("edit.component.text")

        /** The horizontal inset between the bounding box and the text.  */
        @JvmStatic
        protected val INSET_X = 10

        /** The vertical inset between the bounding box and the text.  */
        @JvmStatic
        protected val INSET_Y = 10
    }

    /** ---- [Drawable] */

    override fun contains(x: Double, y: Double): Boolean = super<AbstractRectangularComponent>.contains(x, y)

    override fun contains(p: Point2D): Boolean = super<AbstractRectangularComponent>.contains(p)

    override fun intersects(rect: RectangularShape): Boolean = super<AbstractRectangularComponent>.intersects(rect)

    /** ---- [Transparent] */

    protected val transparent = TransparentImpl(this)

    override var transparency: Int
        get() = transparent.transparency
        set(value) {
            transparent.transparency = value
        }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        if (!text.isEmpty) {
            writer.writeStorables("text", text.allTranslations())
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("text")) {
            // Backward compatibility
            text = TranslatableText(reader.readString("text"))
        }
        if (reader.hasElement("text")) {
            text = TranslatableText(reader.readStorables("text"))
        }
    }

    /** ---- [Component] interface */

    override val type: String get() = TYPE

    /** ---- [TextComponent] interface */

    override var horizontalAlignment: HorizontalAlignment = HorizontalAlignment.LEFT
        set(value) {
            if (field != value) {
                invalidate()
                field = value
                update()
            }
        }

    /** ---- [AbstractTextComponent] interface */

    protected var decorator: TextComponentDecorator = RectangularShapeTextComponentDecorator(
        shape = RoundRectangle2D(0.0, 0.0, 0.0, 0.0, 20.0, 20.0),
        stylable = this,
        transparent = transparent
    )
        private set
}