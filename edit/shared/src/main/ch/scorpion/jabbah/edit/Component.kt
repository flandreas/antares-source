package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.Focusable
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.drawable.Movable
import ch.scorpion.jabbah.draw.drawable.Rotatable
import ch.scorpion.jabbah.draw.style.Stylable
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.io.Storable

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
	 * Determines whether the [InputEventHandler] of this [Component] handles dragging by itself,
	 * rather than letting it be done by the [DragManager] controlled by the [SelectionTool].
	 */
	val isDragManager: Boolean get() = false

	/**
	 * Returns the [Component]s to be deleted as well when this [Component] is deleted
	 * from [drawing].
	 */
	fun getDeleteBuddies(drawing: Drawing<Component>): List<Component> = emptyList()

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
}