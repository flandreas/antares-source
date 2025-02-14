package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.draw.View


/**
 * Posted on the system's [EventBus] when an [Editor] has become the "current" one.
 * This is used in system with many [Editors][Editor] in different switchable [Views][View],
 * while some UI elements are always displayed and show information related with the current [Editor],
 * such as a central property panel.
 *
 * The concept "current editor" is not the same as [Editor.active]: The current [Editor] can still be inactive.
 */
data class CurrentEditorEvent(val editor: Editor)

/**
 * Represents an editor for interactively editing a [Drawing] within a [DrawingView].
 */
interface Editor {

    companion object {

        /** The minimum distance (in view space) of a mouse movement necessary to start a drag operation.*/
        const val DRAG_THRESHOLD = 15

        /** The name of the 'tool lock' property in [PropertyChangeEvent]s.*/
        const val PROP_LOCK_TOOL = "lockTool"

        /** The name of the default [Tool] property in [PropertyChangeEvent]s.*/
        const val PROP_DEFAULT_TOOL = "defaultTool"

        /** The name of the current [Tool] property in [PropertyChangeEvent]s.*/
        const val PROP_CURRENT_TOOL = "currentTool"

        /** The name of the 'active' property in [PropertyChangeEvent]s.*/
        const val PROP_ACTIVE = "active"

        /** The name of the [PropertyChangeEvent] property that determines whether component snap is active.*/
        const val PROP_COMPONENT_SNAP = "componentSnap"

	    /** The name of the [PropertyChangeEvent] property that determines whether grid snap is active.*/
	    const val PROP_GRID_SNAP = "gridSnap"
    }

    /** Holds the current [Drawing] being edited by this [Editor].*/
    val drawing: Drawing<Component> get() = view.drawing

    /** Holds the [DrawingView] used by the user to edit.*/
    val view: DrawingView<Drawing<Component>>

    /**
     * Determines whether this [Editor] is active or not. This affect primarily whether events are forwarded
     * to the current [Tool] or not. An [Editor] is set 'inactive' if interaction with the [Drawing] is completely
     * controlled by other logic, such as when the [Drawing] is being simulated.
     *
     * An [Editor] must not necessarily be deactivated if the [Drawing] is not editable, for example because
     * the user is not authorized to edit it. It might still be useful to keep the [SelectionTool] enabled
     * even if the user is only allowed to select [Component]s, for example to inspect their properties.
     * It is the responsibility of [Action] implementations (and of other logic) to ensure that the user
     * can't change the [Drawing] if he is not allowed to do so.
     */
    var active: Boolean

    /** Determines whether 'tool lock' is active.*/
    var toolLock: Boolean

    /** Determines whether [Component] snapping is enabled.*/
    var componentSnap: Boolean

	/** Determines whether [Grid] snapping is enabled.*/
	var gridSnap: Boolean

    /** Holds the current [Tool].*/
    var currentTool: Tool

    /**
     * Holds the [SelectionTool] of this [Editor]
     * This is also the [Tool] that will be activated when the current [Tool] is done and the [toolLock] property is not set.
     */
    var selectionTool: SelectionTool

    /** Holds the [SnapManager] that controls snapping on behalf of this [Editor].*/
    val snapManager: SnapManager

    /** Holds the [DragManager] that controls dragging of [Component]s on behalf of this [Editor].*/
    val dragManager: DragManager

    /** Holds the [CommandManager] that manages the [Command]s created by this [Editor].*/
    val commandManager: CommandManager

	/** Determines if the current [drawing] has changed in terms of undo/redo operations. */
	val dataChanged: Boolean get() = commandManager.canUndo()

	/** Called by the owner upon destruction. */
	fun dispose()

    fun addPropertyChangeListener(l: PropertyChangeListener<Any>)

	fun addPropertyChangeListener(l: (PropertyChangeEvent<Any>) -> Unit): PropertyChangeListener<Any>

	fun removePropertyChangeListener(l: PropertyChangeListener<Any>)

    /**
     * This method should be called by the current [Tool] when it has finished its activity, for that this
     * [Editor] gets a chance to switch to the default [Tool], if defined and desired by the value of the
     * [toolLock] property.
     */
    fun toolDone()

}