package ch.scorpion.jabbah.draw.container

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.DefaultDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.exception.IndexOutOfBoundsException
import ch.scorpion.jabbah.base.exception.NoSuchElementException
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.drawable.Locatable

/**
 * Standard implementation of the [DrawableContainer] interface.
 */
open class DrawableContainerImpl<T: Drawable>(
        override var location: Point2D = Point2D(),
        val useLocation: Boolean = false
) : AbstractDrawable(), DrawableContainer<T>, Locatable {

    override val drawablesCount: Int get() = children.size

    override val boundingBox: RectangularShape = Rectangle2D()

    /**
     * Holds the child [Drawable]s that this [DrawableContainer] contains. The topmost [Drawable] is stored
     * at the first position of the list.
     */
    private val children: MutableList<T> by lazy { mutableListOf<T>() }

    private val containerListeners: MutableList<DrawableContainerListener<T>>
            by lazy {mutableListOf<DrawableContainerListener<T>>() }

    private var drawableDrawer: DrawableDrawer<T> = DefaultDrawableDrawer()

    /**
     * Holds an [InputEventHandler] that dispatches input events to the [Drawable] whose
     * [contains] methods returns `true` for the events location.
     */
    private val inputEventHandler: InputEventHandler<InputEventContext> = createInputEventHandler()

    open protected fun createInputEventHandler(): InputEventHandler<InputEventContext> =
            DrawableContainerInputEventHandler(this)

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> = inputEventHandler

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
        if (useLocation) {
            context.g.translate(location.x, location.y)
        }
        drawablesInDrawingOrder().forEach {
            if (it.visible) {
                drawableDrawer.process(context, it)
            }
        }
        if (useLocation) {
            context.g.translate(-location.x, -location.y)
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

    override fun getToolTipText(x: Double, y: Double, width: Int?): String? {
        if (useLocation) {
            val l = Point2D(x, y).subtract(this.location)
            return getDrawableAt(x, y)?.getToolTipText(l.x, l.y, width) ?: super.getToolTipText(l.x, l.y, width)
        }
        return getDrawableAt(x, y)?.getToolTipText(x, y, width) ?: super.getToolTipText(x, y, width)
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

    override fun contains(drawable: T): Boolean = children.contains(drawable)

    override fun add(drawable: T): DrawableContainer<T> = add(drawable, 0)

    override fun add(drawable: T, index: Int): DrawableContainer<T> {
        if (children.contains(drawable)) {
            return this
        }
        children.add(index, drawable)
        drawable.handleAdded(this)

        val drawableBBox = drawable.boundingBox
        if (drawablesCount == 1) {
            boundingBox.setFrame(drawableBBox)
        } else {
            boundingBox.add(drawableBBox)
        }

        invalidate(drawableBBox)
        update()

        notifyDrawableAdded(drawable)
        return this
    }

    override fun remove(drawable: T): DrawableContainer<T> {
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

    override fun getDrawableAt(x: Double, y: Double): T? {
        if (useLocation) {
            return children.firstOrNull({ it.visible && it.contains(Point2D(x, y).subtract(location))})
        }
        return children.firstOrNull({ it.visible && it.contains(x, y) })
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
        if (!boundingBox.contains(drawable.boundingBox)) {
            update()
        }
    }

    override fun getStackingOrderPosition(drawable: T): Int {
        val position = children.indexOf(drawable)
        if (position < 0) {
            throw NoSuchElementException("drawable not contained")
        }
        return position
    }

    override fun setStackingOrderPosition(position: Int, drawable: T) {
        val currentPosition = children.indexOf(drawable)
		if (currentPosition < 0) {
			throw NoSuchElementException("drawable not contained")
		}
		if (position < 0 || position >= drawablesCount) {
			throw IndexOutOfBoundsException("position $position out of bounds")
		}
		if (position == currentPosition) {
			return
		}
		children.remove(drawable)
		children.add(position, drawable)
		drawable.invalidate()
    }

    override fun getStackingOrderPositions(drawables: Collection<T>): List<StackingOrderPosition<T>> {
        val positions = mutableListOf<StackingOrderPosition<T>>()
        drawables.forEach { drawable -> positions.add(StackingOrderPosition(getStackingOrderPosition(drawable), drawable)) }
        positions.sort()
        return positions
    }

    override fun toFront(drawables: Collection<T>) {
        for ((i, pos) in getStackingOrderPositions(drawables).withIndex()) {
            setStackingOrderPosition(i, pos.drawable)
        }
    }

    override fun toBack(drawables: Collection<T>) {
        for (pos in getStackingOrderPositions(drawables)) {
            setStackingOrderPosition(drawablesCount - 1, pos.drawable)
        }
    }

    /** ---- [DrawableContainerImpl] */

    private fun notifyDrawableAdded(drawable: T) {
        val event = DrawableContainerEvent(this, drawable)
        containerListeners.forEach { it.drawableAdded(event) }
    }

    private fun notifyDrawableRemoved(drawable: T) {
        val event = DrawableContainerEvent(this, drawable)
        containerListeners.forEach { it.drawableRemoved(event) }
    }

    /**
     * Implementation of removing a [Drawable] from this [DrawableContainer] that recalculates the new
     * bounding box only after the last removal.
     */
    private fun removeDrawableImpl(drawable: T, last: Boolean) {
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
    private fun updateBoundingBox() {
        if (!children.isEmpty()) {
            boundingBox.setFrame(children[0].boundingBox)
        }
        children.forEach { boundingBox.add(it.boundingBox) }
    }
}