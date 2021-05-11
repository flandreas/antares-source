package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.command.AbstractCommand

/**
 * Marker interface to be implemented by classes with an associated bean info class.
 * Used to prevent such classes from obfuscation when used on the JVM platform.
 */
interface Bean {
	// empty
}

/**
 * Used to set a [Bean]'s property if reflection is not possible.
 * @param V the type of the property
 */
typealias PropertySetter<V> = (Any, V?) -> Unit

/**
 * Used to get a [Bean]'s property if reflection is not possible.
 * @param V the type of the property
 */
typealias PropertyGetter<V> = (Any) -> V?

/**
 * Used by [Command]s to access a bean only by its ID. The provided [List] of IDs represents the chain
 * from the topmost object in the [Drawing] held by the [Editor], down to the bean that is to be provided,
 * thereby allowing to reference chains of composed objects.
 */
typealias BeanProvider = (Editor, List<Int>) -> Bean

/** Provides the [Component] of an [Editor]'s [Drawing] with a particular ID. */
val componentBeanProvider: BeanProvider = { e, ids -> e.drawing.getWithId(ids[0])!!.propertyOwner as Component }

/** Provides the current [Drawing] of an [Editor].*/
val drawingBeanProvider: BeanProvider = { e, _ -> e.drawing }

/**
 * A [Command] that reflects the undoable change of an object's property.
 *
 * [Command]s keep the ID of rather than a reference to the object they access, because
 * undoing [Command] can lead to replaying older [Command]s from a snapshot, which leads to different
 * object references.
 *
 * @param V the type of the property's value
 */
abstract class AbstractPropertyCommand<V>(
	editor: Editor,
	private val propertyBaseKey: String,
	private val beanProvider: BeanProvider,
	private val beanIds: List<Int>,
	private val newValue: V?,
) : AbstractCommand("edit.command.property", editor), Undoable {

	var oldValue: V? = null
		private set

	fun establishOldValue() {
		oldValue = getValue()
	}

	protected abstract fun getValue(): V?

	protected abstract fun setValue(value: V?)

	protected val bean get() = beanProvider(editor!!, beanIds)

	override fun getDescription(): String =
		Translations.getString("$propertyBaseKey.name")

	override fun execute() {
		if (oldValue == null) {
			oldValue = getValue()
		}
		setValue(newValue)
	}

	override fun undo() {
		setValue(oldValue)
	}
}
