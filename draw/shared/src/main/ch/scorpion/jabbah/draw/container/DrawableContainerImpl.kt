package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.DefaultDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.module.DrawModule

/**
 * Standard implementation of the [DrawableContainer] interface.
 *
 * @param useViewCoordinates `true` if [Drawable]s in this [DrawableContainerImpl] use view coordinate space,
 * `false` if they use model coordinate space. Relevant for clipping when repainting dirty regions.
 */
open class DrawableContainerImpl<T : Drawable>(
	override var location: Point2D = Point2D.ZERO,
	override val useLocation: Boolean = false,
	visible: Boolean = true,
	private val useViewCoordinates: Boolean = false
) : AbstractDrawable(visible), DrawableContainer<T>, Locatable {

	/**
	 * Holds the child [Drawable]s that this [DrawableContainer] contains. The topmost [Drawable] is stored
	 * at the first position of the list.
	 */
	protected val children: MutableList<T> by lazy { mutableListOf() }

	private val containerListeners: MutableList<DrawableContainerListener<T>> by lazy { mutableListOf() }

	private var drawableDrawer: DrawableDrawer<T> = DefaultDrawableDrawer()

	/**
	 * Holds an [InputEventHandler] that dispatches input events to the [Drawable] whose
	 * [contains] methods returns `true` for the events location.
	 */
	private val inputEventHandler: DrawableBagInputEventHandler<T, InputEventContext> by lazy { provideInputEventHandler() }

	/** Used by [DrawableContainerImpl]s with [useViewCoordinates] `true` to fetch the clipping buffer. */
	private val viewCoordinatesClipBuffer = Rectangle2D()

	/** ---- [DrawableBag] interface */

	override val drawables: List<T> get() = children

	override var rotation: Rotation = Rotation.R0

	override fun add(drawable: T, index: Int): DrawableContainer<T> {
		if (children.contains(drawable)) {
			return this
		}
		children.add(index, drawable)
		drawable.handleAdded(this)

		if (drawable.visible) {
			val drawableBBox = childBoundingBox(drawable)
			if (drawables.size == 1) {
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

	/** ---- [Drawable] interface */

	override val boundingBox: RectangularShape = Rectangle2D()

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
		if (visible && children.isNotEmpty()) {
			var clip = getClip(context)

			if (useLocation) {
				context.g.translate(location.x, location.y)
				if (clip != null) {
					clip = Rectangle2D(clip.x - location.x, clip.y - location.y, clip.width, clip.height)
				}
			}

			drawablesInDrawingOrder().forEach {
				if (it.visible) {
					// Clipping
					if (clip == null || it.intersects(clip)) {
						drawableDrawer.process(context, it)
					}
				}
			}

			if (useLocation) {
				context.g.translate(-location.x, -location.y)
				DrawModule.drawDebugBoundingBox(this, context.g)
			} else {
				DrawModule.drawDebugBoundingBox(this, context.g)
			}

			DrawModule.drawDebugBoundingBoxLocation(location, context)
		}
	}

	private fun getClip(context: DrawContext): RectangularShape? =
		if (useViewCoordinates) {
			if (context.g.supportClipping) {
				context.g.getClipBounds(viewCoordinatesClipBuffer)
				viewCoordinatesClipBuffer
			} else {
				null
			}
		} else {
			context.modelClip
		}

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		inputEventHandler.useFor(this)
		return inputEventHandler
	}

	/** Returns the [Drawable]s in the order they should be drawn.*/
	protected open fun drawablesInDrawingOrder(): ImmutableList<T> = children.asReversed().toImmutableList()

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

	override fun handleDrawableInvalidated(drawable: Drawable, region: RectangularShape) {
		if (useLocation) {
			invalidate(region.moveBy(location))
		} else {
			invalidate(region)
		}
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

	private fun childBoundingBox(child: Drawable): RectangularShape {
		return if (useLocation) {
			Rectangle2D(child.boundingBox).moveBy(location)
		} else {
			child.boundingBox
		}
	}

	protected open fun provideInputEventHandler(): DrawableBagInputEventHandler<T, InputEventContext> =
		DrawableBagInputEventHandler()

	/**
	 * Updates this [DrawableContainer]'s bounding box by calculating the union of the bounding boxes of
	 * all contained [Drawable]'s.
	 */
	protected fun updateBoundingBox() {
		children.firstOrNull { it.visible }
			?.let { boundingBox.setFrame(childBoundingBox(it)) }
			?: boundingBox.setFrame(0.0, 0.0, 0.0, 0.0)
		children.filter { it.visible }.forEach { boundingBox.add(childBoundingBox(it)) }
	}

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
		if (children.contains(drawable)) {
			children.remove(drawable)
			drawable.handleRemoved(this)
			if (last) {
				updateBoundingBox()
			}
			invalidate(childBoundingBox(drawable))
			notifyDrawableRemoved(drawable)
		}
	}
}