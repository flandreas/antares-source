package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.Drawable
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
interface Component : Movable, Snappable, Storable, Stylable, Cloneable<Component>, Bean {

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

	/** Determines whether this [Component] uses its [rotation] property.*/
	val useRotation: Boolean

	/**
	 * Determines whether this [Component] can be interactively rotated by the user in terms of
	 * [rotateClockwise] and [rotateCounterClockwise], which doesn't necessarily require [useRotation] to be `true`.
	 */
	val rotatable: Boolean

	/**
	 * Holds the geometrical rotation property of this [Component]. This is automatically accounted for when the [Component]
	 * is drawn, or when its bounding box is calculated.
	 * @throws IllegalArgumentException when this property is set although [useRotation] is `false`
	 */
	var rotation: Rotation

	/**
	 * Returns the object whose properties are editable when the user edits this [Component].
	 * Most implementations will just return `this`. Wrapper classes might return the wrapped object.
	 */
	val propertyOwner: Any

	/**
	 * Returns the fully qualified class name to be used for instantiating the bean info object used
	 * for property sheets. If `null`, the class name is derived from the property owner's class
	 * be appending "BeanInfo" to its class name.
	 */
	val beanInfoClassName: String? get() = null

	/**
	 * Returns the preferred strategy of how this [Component] likes to render its selection state.
	 * Returns `null`if this [Component] supports multiple [SelectionDrawingStrategies][SelectionDrawingStrategy]
	 */
	var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?

	/** Controls whether this [Component] can receive the focus.*/
	var isFocusable: Boolean

	/** Determines whether this [Component] currently has the focus.*/
	val isFocusOwner: Boolean

	/** Requests the focus for this [Component].*/
	fun requestFocus()

	/**
	 * Informs this [Component] that is has gained the focus. Implementing classes should update their
	 * graphical representation. This method is typically only called by the [FocusManager].
	 */
	fun focusGained()

	/**
	 * Informs this [Component] that is has lost the focus. Implementing classes should update their
	 * graphical representation.  This method is typically only called by the [FocusManager].
	 */
	fun focusLost()

	/**
	 * Increases the current [Rotation] of this [Component] counterclockwise by 90 degrees.
	 * The default implementation of this method will be to adjust the [rotation] property, which
	 * will lead to an exception if [useRotation] is `false`. However, some [Component] implementation
	 * might have to adjust their geometry when being rotated, so they will implement a different behaviour
	 * of rotation, perhaps one that is based more on orientation [Direction] that on [Rotation] angle.
	 */
	fun rotateCounterClockwise()

	/**
	 * Increases the current [Rotation] of this [Component] clockwise by 90 degrees.
	 * @see [rotateCounterClockwise] for more information.
	 */
	fun rotateClockwise()
}