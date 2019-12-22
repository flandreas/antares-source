package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.geom.Shape
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Tooltip

/**
 * An abstract base implementation of the [Drawable] interface.
 */
abstract class AbstractDrawable : Drawable {

	private val listeners: MutableList<DrawableListener> by lazy { mutableListOf<DrawableListener>() }

	/** ---- [Drawable] interface */

	/** The parent [DrawableContainer] that contains this [Drawable].*/
	private var _parent: DrawableContainer<*>? = null
	override val parent: DrawableContainer<*>? get() = _parent

	/**
	 * Is `false` by default. Subclasses that overwrite this property with `true` must implement
	 * [mirrorHorizontally] and [mirrorVertically].
	 */
	override val canMirror: Boolean = false

	override var visible: Boolean = true
		set(value) {
			if (value != visible) {
				invalidate()
				field = value
				validate()
			}
		}

	/** Empty implementation.*/
	override fun dispose() {
		// empty
	}

	override fun accept(visitor: HierarchyVisitor): Boolean {
		return visitor.visit(this)
	}

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		return InputEventHandlerAdapter.EMPTY_HANDLER
	}

	final override fun addDrawableListener(listener: DrawableListener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener)
		}
	}

	final override fun removeDrawableListener(listener: DrawableListener) {
		listeners.remove(listener)
	}

	override fun invalidate() {
		invalidate(boundingBox)
	}

	override fun invalidate(region: RectangularShape) {
		parent?.handleDrawableInvalidated(this, region)
		if (!listeners.isEmpty()) {
			val event = DrawableEvent(this, region)
			listeners.forEach { it.drawableInvalidated(event) }
		}
	}

	override fun validate() {
		requestRedraw()
	}

	override fun <T : Drawable> handleAdded(container: DrawableContainer<T>) {
		_parent = container
	}


	override fun <T : Drawable> handleRemoved(container: DrawableContainer<T>) {
		_parent = null
	}

	override fun mirrorHorizontally(x: Double) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun mirrorVertically(y: Double) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun getTooltip(x: Double, y: Double): Tooltip? = null

	override fun getExplanation(x: Double, y: Double): DrawableExplanation<RectangularDrawable>? = null

	/** ---- AbstractDrawable */

	/**
	 * Notifies all registered [DrawableListener]s and the parent [DrawableContainer] that the geometry
	 * of this [Drawable] has been updated.
	 */
	protected open fun update() {
		parent?.handleDrawableUpdated(this)
		if (!listeners.isEmpty()) {
			val event = DrawableEvent(this)
			listeners.forEach { it.drawableUpdated(event) }
		}
	}

	/**
	 * Notifies all registered [DrawableListener]s and the parent [DrawableContainer] that this [Drawable]
	 * should be redrawn.
	 */
	protected open fun requestRedraw() {
		parent?.handleDrawableRequestRedraw(this)
		if (!listeners.isEmpty()) {
			val event = DrawableEvent(this)
			listeners.forEach { it.drawableRequestRedraw(event) }
		}
	}

	/** Utility function for filling a [Shape] with the given fill [Color]. */
	protected fun drawFill(context: DrawContext, shape: Shape, fillColor: Color?) {
		if (fillColor != null) {
			context.g.color = fillColor
			context.g.fill(shape)
		}
	}

	/** Utility function for drawing the stroke of a [Shape] with the given stroke [Color]. */
	protected fun drawStroke(context: DrawContext, shape: Shape, strokeColor: Color?, stroke: Stroke?) {
		if (stroke != null && strokeColor != null) {
			context.g.color = strokeColor
			context.g.stroke = stroke
			context.g.draw(shape)
		}
	}

	protected fun buildToolTipText(title: String?, text: String?): String? {
		return System.buildToolTipText(title, text, true)
	}

	/**
	 * A wrapper class that listens to [DrawableEvent]s from an inner [AbstractDrawable] and that calls handling
	 * methods of an owner [AbstractDrawable].
	 */
	class DrawableOwner(val owner: AbstractDrawable, val inner: Drawable) : DrawableListener {

		init {
			inner.addDrawableListener(this)
		}

		fun dispose() {
			inner.removeDrawableListener(this)
		}

		/** ---- [DrawableListener] interface */

		override fun drawableInvalidated(event: DrawableEvent) {
			owner.invalidate()
		}

		override fun drawableRequestRedraw(event: DrawableEvent) {
			owner.requestRedraw()
		}

		override fun drawableUpdated(event: DrawableEvent) {
			owner.update()
		}
	}
}

