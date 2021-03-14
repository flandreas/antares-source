package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.DefaultDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Standard implementation of the [DrawableContainer] interface.
 */
open class DrawableContainerImpl<T : Drawable>(
	override var location: Point2D = Point2D.ZERO,
	val useLocation: Boolean = false
) : AbstractDrawable(), DrawableContainer<T>, Locatable {

	override val drawablesCount: Int get() = children.size

	override val boundingBox: RectangularShape = Rectangle2D()

	/**
	 * Holds the child [Drawable]s that this [DrawableContainer] contains. The topmost [Drawable] is stored
	 * at the first position of the list.
	 */
	protected val children: MutableList<T> by lazy { mutableListOf() }

	private val containerListeners: MutableList<DrawableContainerListener<T>>
		by lazy { mutableListOf() }

	private var drawableDrawer: DrawableDrawer<T> = DefaultDrawableDrawer()

	/**
	 * Holds an [InputEventHandler] that dispatches input events to the [Drawable] whose
	 * [contains] methods returns `true` for the events location.
	 */
	private val inputEventHandler: DrawableContainerInputEventHandler<T, InputEventContext> by lazy { provideInputEventHandler() }

	protected open fun provideInputEventHandler(): DrawableContainerInputEventHandler<T, InputEventContext> =
		DrawableContainerInputEventHandler()

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		inputEventHandler.useFor(this)
		return inputEventHandler
	}

	/** ---- [Drawable] interface */

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			for (d in backToFrontIterator()) {
				if (!d.accept(visitor)) {
					break
				}
			}
		}
		return visitor.visitLeave(this)
	}

	override fun draw(context: DrawContext) {
		if (children.isNotEmpty()) {
			if (useLocation) {
				context.g.translate(location.x, location.y)
			}
			drawablesInDrawingOrder().forEach {
				if (it.visible) {
					drawableDrawer.process(context, it)
				}
			}
			DrawModule.drawDebugBoundingBox(this, context.g)
			if (useLocation) {
				context.g.translate(-location.x, -location.y)
			}
			DrawModule.drawDebugBoundingBoxLocation(location, context)
		}
	}

	/** Returns the [Drawable]s in the order they should be drawn.*/
	protected open fun drawablesInDrawingOrder(): ImmutableList<T> = children.asReversed().toImmutableList()

	override fun contains(x: Double, y: Double): Boolean {
		if (useLocation) {
			val location = Point2D(x, y).subtract(this.location)
			return children.any { it.visible && it.contains(location) }
		}
		return children.any { it.visible && it.contains(x, y) }
	}

	override fun getTooltip(x: Double, y: Double): Tooltip? {
		if (useLocation) {
			val l = Point2D(x, y).subtract(this.location)
			return getDrawableAt(x, y)?.getTooltip(l.x, l.y) ?: super.getTooltip(l.x, l.y)
		}
		return getDrawableAt(x, y)?.getTooltip(x, y) ?: super.getTooltip(x, y)
	}

	/** ---- [DrawableContainer] interface */

	override fun setDrawableDrawer(drawableDrawer: DrawableDrawer<T>) {
		this.drawableDrawer = drawableDrawer
	}

	override fun addDrawableDrawer(drawableDrawer: DrawableDrawer<T>) {
		drawableDrawer.successor = this.drawableDrawer
		this.drawableDrawer = drawableDrawer
	}

	override fun addDrawableContainerListener(listener: DrawableContainerListener<T>) {
		if (!containerListeners.contains(listener)) {
			containerListeners.add(listener)
		}
	}

	override fun removeDrawableContainerListener(listener: DrawableContainerListener<T>) {
		containerListeners.remove(listener)
	}

	override fun get(index: Int): T = children[index]

	override fun contains(drawable: Drawable): Boolean = children.contains(drawable)

	override fun add(drawable: T): DrawableContainer<T> = add(drawable, 0)

	override fun add(drawable: T, index: Int): DrawableContainer<T> {
		if (children.contains(drawable)) {
			return this
		}
		children.add(index, drawable)
		drawable.handleAdded(this)

		if (drawable.visible) {
			val drawableBBox = drawable.boundingBox
			if (drawablesCount == 1) {
				boundingBox.setFrame(drawableBBox)
			} else {
				boundingBox.add(drawableBBox)
			}

			invalidate(drawableBBox)
			update()

		}
		notifyDrawableAdded(drawable)
		return this
	}

	override fun remove(drawable: Drawable): DrawableContainer<T> {
		removeDrawableImpl(drawable, true)
		return this
	}

	override fun clear(): DrawableContainer<T> {
		while (children.size > 0) {
			removeDrawableImpl(children[0], children.size == 1)
		}
		return this
	}

	override fun frontToBackIterator(): Iterator<T> = children.iterator()

	override fun backToFrontIterator(): Iterator<T> {
		val iter = children.listIterator(children.size)
		return object : Iterator<T> {
			override fun hasNext(): Boolean = iter.hasPrevious()
			override fun next(): T = iter.previous()
		}
	}

	override fun getDrawableAt(x: Double, y: Double, predicate: (T) -> Boolean): T? {
		if (useLocation) {
			return children.firstOrNull { it.visible && predicate.invoke(it) && it.contains(Point2D(x, y).subtract(location)) }
		}
		return children.firstOrNull { it.visible && it.contains(x, y) }
	}

	override fun getDrawables(): ImmutableList<T> = children.toImmutableList()

	override fun getDrawables(predicate: (T) -> Boolean): ImmutableList<T> =
		children.filter(predicate).toImmutableList()

	override fun getDrawable(predicate: (T) -> Boolean): T? = children.firstOrNull(predicate)

	override fun handleDrawableInvalidated(drawable: Drawable, region: RectangularShape) {
		invalidate(region)
	}

	override fun handleDrawableRequestRedraw(drawable: Drawable) {
		requestRedraw()
	}

	override fun handleDrawableUpdated(drawable: Drawable) {
		updateBoundingBox()
		if (!boundingBox.contains(drawable.boundingBox)) {
			update()
		}
	}

	/** ---- [DrawableContainerImpl] */

	private fun notifyDrawableAdded(drawable: T) {
		val event = DrawableContainerEvent(this, drawable)
		containerListeners.forEach { it.drawableAdded(event) }
	}

	private fun notifyDrawableRemoved(drawable: Drawable) {
		val event = DrawableContainerEvent(this, drawable)
		containerListeners.forEach { it.drawableRemoved(event) }
	}

	/**
	 * Implementation of removing a [Drawable] from this [DrawableContainer] that recalculates the new
	 * bounding box only after the last removal.
	 */
	private fun removeDrawableImpl(drawable: Drawable, last: Boolean) {
		children.remove(drawable)
		drawable.handleRemoved(this)
		if (last) {
			updateBoundingBox()
		}
		invalidate(drawable.boundingBox)
		notifyDrawableRemoved(drawable)
	}

	/**
	 * Updates this [DrawableContainer]'s bounding box by calculating the union of the bounding boxes of
	 * all contained [Drawable]'s.
	 */
	protected fun updateBoundingBox() {
		children.firstOrNull { it.visible }
			?.let { boundingBox.setFrame(it.boundingBox) }
			?: boundingBox.setFrame(0.0, 0.0, 0.0, 0.0)
		children.filter { it.visible }.forEach { boundingBox.add(it.boundingBox) }
	}
}