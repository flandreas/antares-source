package io.antarescircuit.jabbah.graph.container

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.drawable.Locatable
import io.antarescircuit.jabbah.draw.drawable.RotationDirection
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.model.AbstractComponent
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.port.PortViewFactory
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A [Component] that wraps a [PortView] in order to allow the user to manipulate it.
 *
 * Cloning a subclass instance of [PortViewComponent] always creates the type of instance  that is returned by
 * [PortViewFactory].
 */
open class PortViewComponent(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	portView: PortView<*>? = null
) : AbstractComponent(styleProvider), SnappableX, SnappableY {

	companion object {
		private val TYPE = Translations.getString("graph.component.port")
	}

	val port: Port<*> get() = portView!!.port

	var portView: PortView<*>? = portView
		private set

	private var drawableOwner: DrawableOwner? = null

	init {
		preferredSelectionDrawingStrategy = SelectionDrawingStrategy.REPLACE
		if (portView != null) {
			drawableOwner = DrawableOwner(this, portView)
		}
	}

	/** ---- Manually delegated properties */

	var direction: Direction
		get() = portView!!.direction
		set(value) {
			portView!!.direction = value
		}

	/** ---- [Cloneable] */

	override fun doClone(): Component =
		GraphViewModule.portViewFactory.createPortViewComponent(portView!!.doClone())

	/** ---- [Component] */

	override val type: String get() = TYPE

	override val copyable: Boolean get() = false

	override fun isRotatableWith(selection: Collection<*>): Boolean = true

	override fun rotate(direction: RotationDirection, pivot: Point2D?) {
		this.direction = when (direction) {
			RotationDirection.Clockwise -> Direction.of(this.direction.rotation.previous())
			RotationDirection.CounterClockwise -> Direction.of(this.direction.rotation.next())
		}
		pivot?.let {
			location = direction.rotation.rotatePointAround(it, location)
		}
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeStorable("portView", portView!!)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (drawableOwner != null) {
			drawableOwner!!.dispose()
		}
		portView = reader.readStorable("portView") as PortView<*>
		drawableOwner = DrawableOwner(this, portView!!)
	}

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape
		get() = portView!!.boundingBox

	override fun draw(context: DrawContext) {
		portView!!.draw(context)
	}

	override fun contains(x: Double, y: Double): Boolean {
		return portView!!.contains(x, y)
	}

	fun drawAboveOwner(context: DrawContext) {
		portView?.drawAboveOwner(context)
	}

	fun drawBelowOwner(context: DrawContext) {
		portView?.drawBelowOwner(context)
	}

	/** ---- [Locatable] */

	override var location: Point2D
		get() = portView!!.location
		set(value) {
			portView!!.location = value
		}

	override val snappableX: Array<SnappableX> get() = arrayOf(this)

	override val snappableY: Array<SnappableY> get() = arrayOf(this)

	/** ---- [SnappableX] */

	override val x: Double get() = location.x

	override fun accept(other: SnappableX): Boolean = other is PortViewContainer

	override val y: Double get() = location.y

	/** ---- [SnappableX] */

	override fun accept(other: SnappableY): Boolean = other is PortViewContainer
}