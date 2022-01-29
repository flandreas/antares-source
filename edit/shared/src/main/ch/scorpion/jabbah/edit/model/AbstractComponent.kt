package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractStyledDrawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.io.*

/**
 * Base implementation of [Component] to be used for subclassing concrete [Component] implementations.
 */
abstract class AbstractComponent(
	stylable: Stylable
) : AbstractStyledDrawable(stylable), Component {

	constructor(
		styleProvider: StyleProvider = DrawStyleModule.styleProvider,
		styleType: StyleType = StyleType.FIGURE
	) : this(StylableImpl(styleType = styleType, styleProvider = styleProvider))

	/** ---- [Cloneable] interface */

	override fun doClone(): Component = StorableCloner.clone(this)

	/** ---- [Component] interface */

	override var id: Int = 0

	override val typeDesc: String? get() = null

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy? = null

	override val fixStyleType: Boolean get() = false

	override val selectableComponent: Component get() = this

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

	override fun write(writer: StoreWriter) {
		writer.writeInt("id", id)
		if (!fixStyleType) {
			writer.writeString("style", styleType.name)
		}
		if (customColor != null) {
			writer.writeString("color", customColor!!.name)
		}
		if (customStroke != null) {
			writer.writeString("stroke", customStroke!!.identity.id)
		}
		if (useRotation) {
			writer.writeString("rot", rotation.customName)
		}
		writer.writeBoolean("filled", filled)
		writer.writeBoolean("stroked", stroked)
		if (customShadow != null) {
			writer.writeBoolean("shadow", customShadow!!)
		}
	}

	override fun read(reader: StoreReader) {
		if (reader.hasAttribute("id")) {
			id = reader.readInt("id")
		}
		if (useRotation && reader.hasAttribute("rot")) {
			rotation = Rotation.withName(reader.readString("rot"))
		}
		if (!fixStyleType && reader.hasAttribute("style")) {
			var storedStyle = reader.readString("style")
			if (storedStyle == "explanation") {
				// Backward compatibility: StyleType 'explanation' has been made a system StyleType.
				// Existing usages are replaced by 'text'.
				storedStyle = StyleType.TEXT.name
			}
			styleType = styleProvider.getStyleType(storedStyle)
		}
		if (reader.hasAttribute("color")) {
			customColor = styleProvider.predefinedColorProvider.withIdName(reader.readString("color"))
		}
		if (reader.hasAttribute("stroke")) {
			customStroke = styleProvider.predefinedStrokeProvider.withId(reader.readString("stroke"))
		}
		if (reader.hasAttribute("filled")) {
			// Legacy support: Older versions didn't store 'filled' if equal to default 'true'
			filled = reader.readBoolean("filled")
		}
		if (reader.hasAttribute("stroked")) {
			// Legacy support: Older versions didn't store 'stroked' if equal to default 'true'
			stroked = reader.readBoolean("stroked")
		}
		if (reader.hasAttribute("shadow")) {
			customShadow = reader.readBoolean("shadow")
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		// empty
	}

	/** ---- [Focusable] interface */

	/** [Component]s are by default not focusable. */
	override var isFocusable: Boolean = false

	override fun focusGained() {
		invalidate()
		validate()
	}

	override fun focusLost() {
		invalidate()
		validate()
	}

	/** ---- [Rotatable] interface */

	override var rotation: Rotation = Rotation.R0
		set(value) {
			if (!useRotation) {
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

	/**
	 * Since rotation behaviour must be implemented by concrete [Component]s, this implementation doesn't
	 * use the [rotation] property by default. Subclasses that support (and implement) rotation can override this
	 * property to return `true`.
	 */
	override val useRotation: Boolean get() = false

	/**
	 * Since rotation behaviour must be implemented by concrete [Component]s, this implementation returns the
	 * same value as [useRotation] by default. Subclasses that implement a custom, non [Rotation] property based rotation
	 * behaviour can override this method to return `true`.
	 */
	override val rotatable: Boolean get() = useRotation

	override fun rotate(direction: RotationDirection) {
		rotation = when (direction) {
			RotationDirection.Clockwise ->rotation.previous()
			RotationDirection.CounterClockwise -> rotation.next()
		}
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

	/**
	 * Drawing wrapper method that prepares a setup for location and rotation independent drawing of custom drawing code.
	 *
	 * This method translates the [Graphics2D] context to the location of this [Component] and also
	 * rotates it to the current [Rotation].
	 * @param context the [DrawContext] to be used for drawing
	 * @param drawer the code that effectively draws content within the prepared translation and rotation context.
	 */
	open fun draw(context: DrawContext, drawer: (DrawContext) -> Unit) {
		context.g.translate(location.x, location.y)
		context.g.rotate(rotation.angle)

		drawer.invoke(context)

		context.g.rotate(-rotation.angle)
		context.g.translate(-location.x, -location.y)

		DrawModule.drawLocatableDebugBoundingBox(this, context)
	}
}