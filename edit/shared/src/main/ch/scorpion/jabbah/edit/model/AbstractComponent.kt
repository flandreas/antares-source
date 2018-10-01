package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.draw.drawable.AbstractStyledDrawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.io.*

/**
 * Base implementation of [Component] to be used for subclassing concrete [Component] implementations.
 */
abstract class AbstractComponent(
        styleProvider: StyleProvider,
        styleType: StyleType
) : AbstractStyledDrawable(styleType, styleProvider), Component {

    constructor(styleProvider: StyleProvider): this(styleProvider, StyleType.FIGURE)
    constructor(): this(DrawStyleModule.styleProvider)

    /** ---- [Component] interface */

    override var id: Int = 0

    override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy? = null

    override var rotation: Rotation = Rotation.R0
        set(value) {
            if (!rotatable) {
                throw IllegalArgumentException("rotation not supported")
            }
            if (value != field) {
                invalidate()
                field = value
                rotationChanged(field)
                invalidate()
                update()
            }
        }

    override val fixStyleType: Boolean get() = false

    override val selectableComponent: Component get() = this

    /**
     * Since rotation behaviour must be implemented by concrete [Component]s, this implementation is
     * not rotatable by default. Subclasses that support (and implement) rotation can override this
     * property to return `true`.
     */
    override val rotatable: Boolean get() = false

    override val deletable: Boolean get() = true

    /** ---- [Snappable] interface */

    override val snappableX: Array<SnappableX> get() = arrayOf(SnappableXCoordinate(location.x))

    override val snappableY: Array<SnappableY> get() = arrayOf(SnappableYCoordinate(location.y))

    override val propertyOwner: Any get() = this

    /** ---- [Locatable] interface */

    override fun moveBy(dx: Double, dy: Double) {
        location = Point2D(location.x + dx, location.y + dy)
    }

    /** ---- [Storable] interface */

    override var storableId: Int = 0

    override fun write(writer: StoreWriter) {
        writer.writeInt("id", id)
        if (!fixStyleType) {
            writer.writeString("style", styleType.name)
        }
        if (customColor != null) {
            writer.writeString("color", customColor!!.name)
        }
        if (rotatable) {
            writer.writeString("rot", rotation.customName)
        }
        if (!filled) {
            writer.writeBoolean("filled", filled)
        }
        if (!stroked) {
            writer.writeBoolean("stroked", stroked)
        }
    }

    override fun read(reader: StoreReader) {
        if (reader.hasAttribute("id")) {
            id = reader.readInt("id")
        }
        if (rotatable && reader.hasAttribute("rot")) {
            rotation = Rotation.withName(reader.readString("rot"))
        }
        if (!fixStyleType && reader.hasAttribute("style")) {
            styleType = styleProvider.getStyleType(reader.readString("style"))
        }
        if (reader.hasAttribute("color")) {
            customColor = styleProvider.predefinedColorProvider.withIdName(reader.readString("color"))
        }
        if (reader.hasAttribute("filled")) {
            filled = reader.readBoolean("filled")
        }
        if (reader.hasAttribute("stroked")) {
            stroked = reader.readBoolean("stroked")
        }
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        // empty
    }


    override fun getStorableChildren(): Iterator<Storable> {
        return EmptyIterator()
    }

    /** ---- Focus management */

    /** [Component]s are by default not focusable. */
    override var isFocusable: Boolean = false

    override val isFocusOwner: Boolean get() = FocusManager.focusOwner == this

    override fun requestFocus() {
        FocusManager.requestFocus(this)
    }

    override fun focusGained() {
        invalidate()
        validate()
    }

    override fun focusLost() {
        invalidate()
        validate()
    }

    /** ---- [AbstractComponent] */

    /**
     * Called by this [AbstractComponent] after the rotation property has been changed. Subclasses that support
     * rotation should override this method in order to update their geometry.
     */
    @Suppress("UNUSED_PARAMETER")
    protected open fun rotationChanged(newRotation: Rotation) {
        // empty
    }

}