package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.select.SelectionToolFactory
import ch.scorpion.jabbah.edit.snap.ComponentSnapper
import ch.scorpion.jabbah.edit.snap.SnapManagerImpl
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Standard implementation of the [Editor] interface.
 */
open class EditorImpl(
    override val view: DrawingView<Drawing<Component>>,
    override val commandManager: CommandManager,
    selectionToolFactory: SelectionToolFactory
) : Editor{

    @Suppress("unused")
    constructor(view: DrawingView<Drawing<Component>>): this(view, EditModule.commandManager, EditSelectModule.selectionToolFactory)

    companion object {
        val DRAG_THRESHOLD = 15
    }

    private val changeSupport = PropertyChangeSupport<Any>(this)

    private val componentSnapper = ComponentSnapper(this)

    /** ---- [Editor] interface */

    override val snapManager: SnapManager = SnapManagerImpl(this)

    override var active: Boolean = false
        set(value) {
            if (value == field) {
                return
            }
            val oldValue = field
            field = value

            if (field) {
                view.addMouseListener(mouseEventDelegator)
                view.addMouseMotionListener(mouseEventDelegator)
                view.addKeyListener(keyEventDelegator)
            } else {
                view.removeMouseListener(mouseEventDelegator)
                view.removeMouseMotionListener(mouseEventDelegator)
                view.removeKeyListener(keyEventDelegator)
            }
            changeSupport.fire(Editor.PROP_ACTIVE, oldValue, field)
        }

    override var toolLock: Boolean = false
        set(value) {
            if (value == field) {
                return
            }
            val oldValue = field
            field = value
            changeSupport.fire(Editor.PROP_LOCK_TOOL, oldValue, field)
        }

    override var defaultTool: Tool? = null
        set(value) {
            if (value == field) {
                return
            }
            val oldValue = field
            field = value
            changeSupport.fire(Editor.PROP_DEFAULT_TOOL, oldValue, field)
        }

    override var currentTool: Tool = selectionToolFactory.create(this)
        set(value) {
            if (value == field) {
                return
            }
            val oldValue = field
            field = value
            field.activate()
            changeSupport.fire(Editor.PROP_CURRENT_TOOL, oldValue, field)
        }

    override var componentSnap: Boolean
        get() = componentSnapper.snapEnabled
        set(value) {
            if (value == componentSnap) {
                return
            }
            val oldValue = componentSnap
            componentSnapper.snapEnabled = value
            changeSupport.fire(Editor.PROP_COMPONENT_SNAP, oldValue, value)
        }

    override fun addPropertyChangeListener(l: PropertyChangeListener<Any>) {
        changeSupport.add(l)
    }

    override fun removePropertyChangeListener(l: PropertyChangeListener<Any>) {
        changeSupport.remove(l)
    }

    override fun toolDone() {
        if (!toolLock && defaultTool != null) {
            currentTool = defaultTool!!
        }
    }

    /** ---- [EditorImpl] */

    /**
     * Convenience method being automatically called by this [EditorImpl] whenever a [Component] has
     * been added to the current [Drawing].
     *
     * This implementation does nothing. Intended to be overridden by subclasses.
     * @param component the [Component] that has been added.
     */
    open protected fun handleComponentAdded(component: Component) {
        // empty
    }

    /**
     * Convenience method being automatically called by this [EditorImpl] whenever a [Component] has
     * been removed from the current [Drawing].
     *
     * This implementation does nothing. Intended to be overridden by subclasses.
     * @param component the [Component] that has been removed.
     */
    open protected fun handleComponentRemoved(component: Component) {
        // empty
    }

    /**
     * Listens for [MouseEvent]s from the [DrawingView], calculates the model coordinates by applying the
     * current zoom factor and pan origin, and delegates the events to the current [Tool].
     * Ensures that drag events are only forwarded if [DRAG_THRESHOLD] is exceeded.
     */
    // KT-14888 (fixed with Kotlin version 1.1-M04
    //private val mouseEventDelegator = object : MouseAdapter() {
    private val mouseEventDelegator = MouseEventDelegator()
    private inner class MouseEventDelegator : MouseAdapter() {

        private var isDragging = false

        private var pressedLocation = Point2D()

        override fun mouseMoved(e: MouseEvent) {
            currentTool.mouseMoved(e, view.viewToModelX(e.x.toDouble()), view.viewToModelY(e.y.toDouble()))
        }

        override fun mouseClicked(e: MouseEvent) {
            currentTool.mouseClicked(e, view.viewToModelX(e.x.toDouble()), view.viewToModelY(e.y.toDouble()))
        }

        override fun mousePressed(e: MouseEvent) {
            pressedLocation.setLocation(e.x.toDouble(), e.y.toDouble())
            currentTool.mousePressed(e, view.viewToModelX(e.x.toDouble()), view.viewToModelY(e.y.toDouble()))
        }

        override fun mouseDragged(e: MouseEvent) {
            if (!isDragging && pressedLocation.distance(e.x.toDouble(), e.y.toDouble()) > DRAG_THRESHOLD) {
               isDragging = true
            }
            if (isDragging) {
                currentTool.mouseDragged(e, view.viewToModelX(e.x.toDouble()), view.viewToModelY(e.y.toDouble()))
            }
        }

        override fun mouseReleased(e: MouseEvent) {
            currentTool.mouseReleased(e, view.viewToModelX(e.x.toDouble()), view.viewToModelY(e.y.toDouble()))
            isDragging = false
        }
    }

    /** Forwards [KeyEvent]s to the current [Tool]. */
    // KT-14888 (fixed with Kotlin version 1.1-M04
    //private val keyEventDelegator = object : KeyAdapter() {
    private val keyEventDelegator = KeyEventDelegator()
    private inner class KeyEventDelegator : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            currentTool.keyPressed(e)
        }

        override fun keyReleased(e: KeyEvent) {
            currentTool.keyReleased(e)
        }
    }

    /**
     * Listens for an exchange of the current [Drawing] in the [DrawingView] of this [Editor]
     * in order to register [drawingListener] in it.
     */
    // KT-14888 (fixed with Kotlin version 1.1-M04
    //private val drawingViewListener = object : PropertyChangeListener<Any> {
    private val drawingViewListener = DrawingViewListener()
    private inner class DrawingViewListener : PropertyChangeListener<Any> {
        override fun propertyChanged(e: PropertyChangeEvent<Any>) {
            if (e.name == DrawingView.PROP_DRAWING) {
                (e.oldValue as Drawing<Component>).addDrawableContainerListener(drawingListener)
                (e.newValue as Drawing<Component>).addDrawableContainerListener(drawingListener)
            }
        }
    }

    /**
     * Listens for added and removed [Component]s in the current [Drawing] in order to
     * call [handleComponentAdded] and [handleComponentRemoved].
     */
    // TODO KT-14888 (fixed with Kotlin version 1.1-M04
    //private val drawingListener = object : DrawableContainerAdapter<Component>() {
    private val drawingListener = DrawingListener()
    private inner class DrawingListener : DrawableContainerAdapter<Component>() {
        override fun drawableAdded(event: DrawableContainerEvent<Component>) {
            if (event.child is Component) {
                handleComponentAdded(event.child as Component)
            }
        }

        override fun drawableRemoved(event: DrawableContainerEvent<Component>) {
            if (event.child is Component) {
                handleComponentRemoved(event.child as Component)
            }
        }
    }

    init {
        defaultTool = currentTool
        snapManager.addSnapper(componentSnapper)
        snapManager.addSnapper(view.grid)

        view.drawing.addDrawableContainerListener(drawingListener)
        view.addPropertyChangeListener(drawingViewListener)

        active = true
    }
}