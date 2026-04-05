package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.Focusable
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.drawable.Movable
import io.antarescircuit.jabbah.draw.drawable.Rotatable
import io.antarescircuit.jabbah.draw.style.Stylable
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.io.Storable

/**
 * A [Component] represents a graphical, editable part of a [Drawing].
 *
 * The essential properties and capabilities of a [Component] are the following:
 * - [Component]s can be rendered as [Drawable]s
 * - [Component]s are a part of a [Drawing]
 * - [Component]s can be serialized (e.g. stored) into external formats, and vice versa
 * - [Component]s can be selected
 * - [Component]s can be manipulated by an [Editor]
 */
interface Component : Movable, Rotatable, Snappable, Storable, Stylable, Focusable, Cloneable<Component>, Bean {

	companion object {
		const val BASE_KEY_ID = "edit.property.id"
		const val BASE_KEY_ORIENTATION = "edit.property.Component.orientation"
	}

	/** Holds an identification being unique within the [ComponentContainer] that contains this [Component].*/
	override var id: Int

	/**
	 * Holds a short translated description of the type of this [Component].
	 *
	 * The type of a [Component] describes the "kind" or the nature of a [Component]. This is in contrast to
	 * the name of a [Component], which is often provided by the user and can serve to distinguish two
	 * [Component]s of the same type. Typically, the type is not persistent, but provided by concrete
	 * implementation of the [Component] interface. Note that this type description should be internationalized.
	 *
	 * Example: "Rectangle"
	 */
	val type: String

	val typeDesc: String?

	/**
	 * Determines whether this [Component]'s [StyleType] is determined by the implementing class and cannot be
	 * changed in the [styleType] property.
	 */
	val fixStyleType: Boolean

	/**
	 * Returns the [Component] to be graphically selected when the user selects this [Component].
	 *
	 * Standard implementations just return `this`. Wrappers will return the wrapped [Component].
	 * This is primarily relevant for determination of the displayed [SelectionModel] by the
	 * [SelectionManagerFactory]; the logically selected [Component] is always `this`.
	 */
	val selectableComponent: Component

	/** Determines whether this [Component] can be manually deleted from its [ComponentContainer] interactively by the user.*/
	val deletable: Boolean

	/** Determines whether this [Component] can be manually copied to the clipboard. */
	val copyable: Boolean get() = true

	/**
	 * Returns the object whose properties are editable when the user edits this [Component].
	 * Most implementations will just return `this`. Wrapper classes might return the wrapped object.
	 */
	val propertyOwner: Component

	/**
	 * Returns the preferred strategy of how this [Component] likes to render its selection state.
	 * Returns `null`if this [Component] supports multiple [SelectionDrawingStrategies][SelectionDrawingStrategy]
	 */
	var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?

	/**
	 * Returns `true` if the [InputEventHandler] of this [Component] handles dragging by itself,
	 * i.e. if this [Component] is itself a "drag manager".
	 * Returns `false`if dragging should be done by the [DragManager] controlled by the [SelectionTool].
	 */
	val isDragManager: Boolean get() = false

	/**
	 * Returns the [Component]s to be deleted as well when this [Component] is deleted
	 * from [drawing].
	 */
	fun getDeleteBuddies(drawing: Drawing<*>): List<Component> = emptyList()

	/**
	 * Collects the [Component]s to be selected in addition to this [Component] when selected by mouse
	 * during "Selection expansion mode". Does nothing  by default.
	 * @param buddies the [MutableSet] to which buddies are to be added. Also used for checking
	 * whether a [Component] has already been visited in order to avoid infinite recursion.
	 */
	fun collectSelectBuddies(drawing: Drawing<Component>, buddies: MutableSet<Component>) {}

	/**
	 * Called by the copy/paste system after this [Component] was created as a result of a paste
	 * operation, but before it is added to the destination [drawing]. This gives this [Component]
	 * a chance to adjust any of its properties, e.g. changing its name "Hello" to "Hello (2)".
	 */
	fun beforePaste(drawing: Drawing<Component>) {}

	/**
	 * Notifies this [Component] that the editability of its context (e.g. the view in which it is displayed)
	 * has changed. Most [Component] won't be interested in this, because editing tools are usually managed by an editor
	 * and not by [Components][Component]. However, there are cases like the oscilloscope in the Antares project
	 * that displays [Components][Component] representing buttons that a user can only click if the context is editable.
	 */
	fun notifyEditable(editable: Boolean) {}
}