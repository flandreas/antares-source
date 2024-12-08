package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.drawable.Rotatable
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.graph.model.element.AbstractGraphElement
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Wraps a [Component] in a [GraphElementView].
 *
 * Note that we can't use Kotlin delegation for delegating the [Component] interface to the wrapped [Component],
 * because the wrapped [Component] is changed during the lifetime of this wrapper (due to deserialization),
 * which is not yet supported by Kotlin's built-in delegation.
 */
class GraphElementViewWrapper(
	component: Component? = null,
	styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractGraphElementView<GraphElementWrapper>(styleProvider, GraphStyleType.VERTICE, GraphElementWrapper(component)) {

	val component: Component? get() = _component

	private var drawableOwner: DrawableOwner? = null

	private var _component: Component? = null
		set(value) {
			if (drawableOwner != null) {
				drawableOwner?.dispose()
			}
			field = value
			if (value != null) {
				drawableOwner = DrawableOwner(this, value)
			}
		}

	init {
		_component = component
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		writer.writeInt(STORABLE_MODEL_ID, writer.provideIdentity(model))
		writer.writeInt("id", id)
		writer.writeStorable("component", _component!!)
	}

	override fun read(reader: StoreReader) {
		// don't call super.read() in order not to interfere with the wrapped Component's style
		readModelId(reader)
		_component = reader.readStorable("component") as Component
		if (reader.hasAttribute("id")) {
			id = reader.readInt("id")
		}
	}

	/** ---- [Locatable] interface */

	override var location: Point2D
		get() = _component!!.location
		set(value) {
			_component!!.location = value
		}

	/** ---- [Snappable] interface */

	override val snappableX: Array<SnappableX> get() = _component!!.snappableX

	override val snappableY: Array<SnappableY> get() = _component!!.snappableY

	/** ---- [Stylable] interface */

	override var styleType: StyleType
		get() = _component!!.styleType
		set(value) {
			_component?.let { it.styleType = value }
		}

	/** ---- [Rotatable] interface */

	override fun isRotatableWith(selection: Collection<*>): Boolean =
		_component!!.isRotatableWith(selection)

	override val useRotation: Boolean get() = _component!!.useRotation

	override fun rotate(direction: RotationDirection, pivot: Point2D?) {
		_component!!.rotate(direction, pivot)
	}

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape get() = _component!!.boundingBox

	override val type: String get() = _component!!.type

	override val typeDesc: String? get() = _component!!.typeDesc

	override var visible: Boolean
		get() = _component!!.visible
		set(value) {
			_component!!.visible = value
		}

	override fun draw(context: DrawContext) {
		_component!!.draw(context)
	}

	override fun contains(x: Double, y: Double): Boolean = _component!!.contains(x, y)

	override fun <T: InputEventContext> getTooltip(context: T): Tooltip? = _component!!.getTooltip(context)

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		_component!!.getInputEventHandler(context)

	override fun <T : Drawable> handleAdded(container: DrawableContainer<T>) {
		_component!!.handleAdded(container)
	}

	override fun <T : Drawable> handleRemoved(container: DrawableContainer<T>) {
		_component!!.handleRemoved(container)
	}

	/** ---- [Component] interface */

	/** Forwards id to Component in order to appear as bean property.*/
	override var id: Int
		get() = super.id
		set(value) {
			super.id = value
			component!!.id = value
		}

	override val selectableComponent: Component get() = _component!!

	override val propertyOwner: Component get() = _component!!.propertyOwner

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = _component!!.preferredSelectionDrawingStrategy
		set(value) {
			super.preferredSelectionDrawingStrategy = value
		}

	/** ---- [GraphElementView] interface */

	override val isFullyConnected: Boolean get() = true
}

class GraphElementWrapper(private val component: Component? = null) : AbstractGraphElement() {

	override val type: String get() = component?.type ?: "unknown"
	override val typeDesc: String?
		get() = component?.typeDesc ?: "unknown"
}