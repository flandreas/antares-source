package io.antarescircuit.jabbah.draw.container

import io.antarescircuit.jabbah.base.HierarchyVisitor
import io.antarescircuit.jabbah.base.Tooltip
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.base.geom.Rotation
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.drawable.DefaultDrawableDrawer
import io.antarescircuit.jabbah.draw.drawable.DrawableDrawer
import io.antarescircuit.jabbah.draw.drawable.Locatable
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.draw.style.Stylable

/**
 * Standard implementation of the [DrawableContainer] interface.
 *
 * @param useViewCoordinates `true` if [Drawable]s in this [DrawableContainerImpl] use view coordinate space,
 * `false` if they use model coordinate space. Relevant for clipping when repainting dirty regions.
 */
open class DrawableContainerImpl<T : Drawable>(
	location: Point2D = Point2D.ZERO,
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

	/** ---- [Locatable] interface */

	override var location: Point2D = location
		set(value) {
			if (field != value) {
				field = value
				updateBoundingBox()
			}
		}

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
				_boundingBox.setFrame(drawableBBox)
			} else {
				_boundingBox.add(drawableBBox)
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

	private val _boundingBox = Rectangle2D()
	override val boundingBox: RectangularShape get() = _boundingBox

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
		drawImpl(context, drawableDrawer)
	}

	protected fun drawImpl(context: DrawContext, drawer: DrawableDrawer<T>) {
		if (visible && children.isNotEmpty()) {
			var clip = getClip(context)
			val oldModelClip = context.modelClip

			if (useLocation) {
				context.g.translate(location.x, location.y)
				if (clip != null) {
					clip = Rectangle2D(clip.x - location.x, clip.y - location.y, clip.width, clip.height)
					if (!useViewCoordinates && context.modelClip != null) {
						context.modelClip = clip
					}
				}
			}

			drawablesInDrawingOrder().forEach {
				if (it.visible) {
					// Clipping
					if (clip == null || it.intersects(clip)) {
						drawer.process(context, it)
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

			context.modelClip = oldModelClip
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
	protected open fun drawablesInDrawingOrder(): List<T> = children.asReversed()

	override fun <T : InputEventContext> getTooltip(context: T): Tooltip? {
		if (useLocation) {
			val localContext = context.withXY(context.location.subtract(this.location))
			return getDrawableAt(context.location)?.getTooltip(localContext) ?: super.getTooltip(localContext)
		}
		return getDrawableAt(context.location)?.getTooltip(context) ?: super.getTooltip(context)
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
			invalidate(Rectangle2D(region).moveBy(location))
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
			?.let { _boundingBox.setFrame(childBoundingBox(it)) }
			?: _boundingBox.setFrame(0.0, 0.0, 0.0, 0.0)
		children.filter { it.visible }.forEach { _boundingBox.add(childBoundingBox(it)) }
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

	override fun drawStandalone(context: DrawContext) {
		val drawer = DrawableContainerDrawer<T>()
		drawBackdrop(context)
		drawImpl(context, drawer)
	}

	private fun drawBackdrop(context: DrawContext) {
		getDrawables { it is Stylable && (it as Stylable).styleType.isBackdrop }
			.forEach { it.draw(context) }
	}
}