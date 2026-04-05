package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.geom.*
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.DrawableAdapter
import io.antarescircuit.jabbah.draw.DrawableEvent
import io.antarescircuit.jabbah.draw.drawable.Locatable
import io.antarescircuit.jabbah.draw.drawable.RectangularDrawable
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.AbstractComponent
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.VerticeView
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewConnectionGeometry
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.graph.view.style.GraphTheme
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A rectangular shaped [VerticeView] implementation that adds the bounding boxes of all [PortView]s to its
 * own bounding box.
 *
 * [AbstractRectangularVerticeView] distinguishes between the location in terms of [Locatable] and the
 * location of the rectangle that defined the basic geometry. The upper left corner of the rectangle is
 * defined relative to the [location] of the [Locatable], which allows to implements concrete [VerticeView]s
 * whose location is at the connection point of one of the [PortView]s and **not** at the upper-left
 * corner of a rectangular box.
 *
 * @param T the type of the model [Vertice] that this [AbstractRectangularVerticeView] displays
 */
abstract class AbstractRectangularVerticeView<T : Vertice>(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: T,
	rectangle: MutableRectangularShape
) : AbstractVerticeView<T>(styleProvider, model), RectangularDrawable {

	/**
	 * @param x the x-coordinate of the upper-left rectangle corner, relative to [location]
	 * @param y the y-coordinate of the upper-left rectangle corner, relative to [location]
	 * @param w the width of the rectangle
	 * @param h the height of the rectangle
	 */
	constructor(
		styleProvider: StyleProvider = DrawStyleModule.styleProvider,
		model: T,
		x: Double = 0.0,
		y: Double = 0.0,
		w: Double = 0.0,
		h: Double = 0.0
	) : this(styleProvider, model, Rectangle2D(x, y, w, h))

	/** Contains the position relative to [location] and the size of the rectangle.*/
	protected val rectangle: MutableRectangularShape = rectangle

	/** Contains the actual and absolute bounding box including all [PortView] bounding boxes. */
	private val _boundingBox = Rectangle2D()

	/** Contains the rectangular area that responds to [contains] method calls. Always kept in sync with the geometry.*/
	private val containsBox = Rectangle2D()

	/** Listens for geometry updates on [PortView]s and initiates bounding box recalculation.*/
	private val portViewUpdateListener = PortViewUpdateListener()

	private inner class PortViewUpdateListener : DrawableAdapter() {
		override fun drawableUpdated(event: DrawableEvent) {
			updateBoxes()
		}
	}

	/** ---- [Locatable] interface  */

	/** Holds the absolute location of this [AbstractRectangularVerticeView], which must not necessarily be the upper-left corner. */
	override var location: Point2D = Point2D.ZERO
		set(value) {
			invalidate()
			field = value
			updateBoxes()
			invalidate()
			if (!isResolving) {
				update()
			}
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeDouble("x", location.x)
		writer.writeDouble("y", location.y)
		if (storeSize) {
			writer.writeDouble("w", width)
			writer.writeDouble("h", height)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		location = Point2D(reader.readDouble("x"), reader.readDouble("y"))
		if (storeSize) {
			setDimension(reader.readDouble("w"), reader.readDouble("h"))
		}
	}

	/** ---- [Drawable] interface */

	override fun contains(x: Double, y: Double): Boolean {
		return rotate(containsBox).contains(x, y)
	}

	/** ---- [AbstractComponent] */

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		updateBoxes()
	}

	/** ---- [AbstractVerticeView] */

	override fun getBoundingBoxImpl(): Rectangle2D {
		updateBoxes()
		return _boundingBox
	}

	override fun addPortView(portView: PortView<*>) {
		super.addPortView(portView)
		portView.addDrawableListener(portViewUpdateListener)
		updateBoxes()
	}

	override fun removePortView(portView: PortView<*>) {
		super.removePortView(portView)
		portView.removeDrawableListener(portViewUpdateListener)
		updateBoxes()
	}

	override fun <G : Any> handleConnect(edgeView: EdgeView<G>, port: Port<G>?, geometry: EdgeViewConnectionGeometry) {
		super.handleConnect(edgeView, port, geometry)
		updateBoxes()
	}

	override fun <G : Any> handleUnconnect(edgeView: EdgeView<G>, port: Port<G>?, lockEndpoint: Boolean) {
		super.handleUnconnect(edgeView, port, lockEndpoint)
		updateBoxes()
	}

	/** ---- [RectangularDrawable] */

	override val bounds: MutableRectangularShape get() = rectangle

	override fun setBounds(x: Double, y: Double, w: Double, h: Double) {
		if (this.x == x && this.y == y && this.width == w && this.height == h) {
			return
		}
		invalidate()
		rectangle.setFrame(x, y, w, h)
		updateBoxes()
		invalidate()
		if (!isResolving) {
			update()
		}
	}

	override var width: Double
		get() = rectangle.width
		set(value) {
			setDimension(value, height)
		}

	override var height: Double
		get() = rectangle.height
		set(value) {
			setDimension(width, value)
		}

	override val lineWidth: Double
		get() = stroke.width.toDouble()

	override fun contains(x: Double, y: Double, w: Double, h: Double): Boolean {
		return rectangle.contains(x, y, w, h)
	}

	/** ---- [AbstractRectangularVerticeView] */

	open fun drawSelected(context: DrawContext) {
		draw(context) { c ->
			super.drawImpl(c)
			context.g.color = Themes.get<GraphTheme>().selection.color.foregroundColor
			context.g.stroke = stroke
			context.g.draw(bounds)
		}
	}

	/**
	 * Determines whether the width and height attributes should be stored by this [Storable], or whether
	 * subclasses calculate the size by themselves. The default ist `false`
	 * @return `true` if the width and height should be stored by this [Storable].
	 */
	protected open val storeSize: Boolean = false

	/** Returns the number of model units to be added at the corresponding side of the rectangle when calculating the bounding box.*/
	protected open val outsetLeft: Int get() = 0
	protected open val outsetRight: Int get() = 0
	protected open val outsetTop: Int get() = 0
	protected open val outsetBottom: Int get() = 0

	protected fun setDimension(w: Double, h: Double) {
		invalidate()
		rectangle.setFrame(rectangle.x, rectangle.y, w, h)
		updateBoxes()
		invalidate()
		if (!isResolving) {
			update()
		}
	}

	protected fun updateBoxes() {
		_boundingBox.setFrame(
			location.x + x - lineWidth - outsetLeft,
			location.y + y - lineWidth - outsetTop,
			width + 2 * lineWidth + outsetLeft + outsetRight,
			height + 2 * lineWidth + outsetTop + outsetBottom
		)
		if (shadow) {
			DropShadow.expand(_boundingBox, rotation)
		}
		addPortViewsTo(_boundingBox, containsBox)
	}
}
