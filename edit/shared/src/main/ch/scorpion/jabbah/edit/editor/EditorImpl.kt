package ch.scorpion.jabbah.edit.editor

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.*
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.edit.SelectionToolFactory
import ch.scorpion.jabbah.edit.snap.ComponentSnapper
import ch.scorpion.jabbah.edit.snap.SnapManagerImpl
import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Standard implementation of the [Editor] interface.
 */
open class EditorImpl(
    final override val view: DrawingView<Drawing<Component>>,
    final override val commandManager: CommandManager,
    selectionToolFactory: SelectionToolFactory
) : Editor{

    @Suppress("unused")
    constructor(view: DrawingView<Drawing<Component>>): this(view, EditModule.commandManager, EditSelectModule.selectionToolFactory)

    companion object {
        const val DRAG_THRESHOLD = 15
    }

    private val changeSupport = PropertyChangeSupport<Any>(this)

    private val componentSnapper = ComponentSnapper(this)

    /** ---- [Editor] interface */

    final override val snapManager: SnapManager = SnapManagerImpl(this)

    final override var active: Boolean = false
        set(value) {
            if (value == field) {
                return
            }
            val oldValue = field
            field = value

            if (field) {
	            currentTool.activate()
            } else {
	            currentTool.deactivate()
            }
            view.autoPanningEnabled = active
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

    final override var selectionTool = selectionToolFactory.create(this)
        set(value) {
            if (value == field) {
                return
            }
            val oldValue = field
            field = value
            changeSupport.fire(Editor.PROP_DEFAULT_TOOL, oldValue, field)
        }

    final override var currentTool: Tool = selectionTool
        set(value) {
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

	override var gridSnap: Boolean
		get() = view.grid.snapEnabled
		set(value) {
			if (value == gridSnap) {
				return
			}
			val oldValue = gridSnap
			view.grid.snapEnabled = value
			changeSupport.fire(Editor.PROP_GRID_SNAP, oldValue, value)
		}

    override fun addPropertyChangeListener(l: PropertyChangeListener<Any>) {
        changeSupport.add(l)
    }

    override fun removePropertyChangeListener(l: PropertyChangeListener<Any>) {
        changeSupport.remove(l)
    }

    override fun toolDone() {
        if (!toolLock) {
            currentTool = selectionTool
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
    protected open fun handleComponentAdded(component: Component) {
        // empty
    }

    /**
     * Convenience method being automatically called by this [EditorImpl] whenever a [Component] has
     * been removed from the current [Drawing].
     *
     * This implementation does nothing. Intended to be overridden by subclasses.
     * @param component the [Component] that has been removed.
     */
    protected open fun handleComponentRemoved(component: Component) {
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

        private var pressedLocation = Point2D.ZERO

        override fun mouseMoved(e: MouseEvent) {
            currentTool.mouseMoved(e, view.viewToModelX(e.x.toDouble()), view.viewToModelY(e.y.toDouble()))
        }

        override fun mouseClicked(e: MouseEvent) {
            currentTool.mouseClicked(e, view.viewToModelX(e.x.toDouble()), view.viewToModelY(e.y.toDouble()))
        }

        override fun mousePressed(e: MouseEvent) {
            pressedLocation = Point2D(e.x.toDouble(), e.y.toDouble())
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
        @Suppress("UNCHECKED_CAST")
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
    // TODO KT-14888 (promised to be fixed with Kotlin version 1.1-M04, but wasn't)
    //private val drawingListener = object : DrawableContainerAdapter<Component>() {
    private val drawingListener = DrawingListener()
    private inner class DrawingListener : DrawableContainerAdapter<Component>() {
        override fun drawableAdded(event: DrawableContainerEvent<Component>) {
            if (event.child is Component) {
	            // Due to Kotlin bug KT-15558, the gradle compiler issues warning "No cast needed"
                handleComponentAdded(event.child as Component)
            }
        }

        override fun drawableRemoved(event: DrawableContainerEvent<Component>) {
            if (event.child is Component) {
	            // Due to Kotlin bug KT-15558, the gradle compiler issues warning "No cast needed"
                handleComponentRemoved(event.child as Component)
            }
        }
    }

    init {
        snapManager.addSnapper(componentSnapper)
        snapManager.addSnapper(view.grid)

        view.drawing.addDrawableContainerListener(drawingListener)
        view.addPropertyChangeListener(drawingViewListener)

	    view.addMouseListener(mouseEventDelegator)
	    view.addMouseMotionListener(mouseEventDelegator)
	    view.addKeyListener(keyEventDelegator)

	    active = true

	    System.invokeLater {
		    // Invoked later when UI already exists and is able to set its state accordingly
		    currentTool = selectionTool
	    }
    }
}