package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.command.AbstractCommand

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
 * Used by [Command]s to access a bean only by its ID.
 * [Bean] IDs are typically [Int], but the [String] type allows special [BeanProvider] implementations
 * to support other usage scenarios, such as modelling an inner-to-outer object path
 * represented as e.g. "outerID.innerID".
 */
typealias BeanProvider = (Editor, Collection<String>) -> Collection<Bean>

/**
 * Provides the IDs of the collection of selected beans by featuring [String]
 * to also support concatenated paths of beans.
 */
typealias BeanIdProvider = (Int) -> Collection<String>


/** Provides the [Component] of an [Editor]'s [Drawing] with a particular ID. */
val componentBeanProvider: BeanProvider = { e, ids ->
	e.drawing.getWidthIds(
		ids.map {
			it.toInt()
		}
	).map {
		it.propertyOwner
	}
}

/** Provides the current [Drawing] of an [Editor].*/
val drawingBeanProvider: BeanProvider = { e, _ -> listOf(e.drawing) }

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
	private val baseKeyParams: Array<Any> = emptyArray(),
	private val beanProvider: BeanProvider,
	private val beanIds: Collection<String>,
	private val newValue: V?,
) : AbstractCommand("edit.command.property", editor), Undoable {

	/** Maps a [Bean] ID to the old property value */
	var oldValues: Map<Int, V?>? = null
		private set

	val valueChanged: Boolean get() = oldValues?.values?.any { it != newValue } == true

	fun establishOldValues() {
		oldValues = mutableMapOf<Int, V?>().also { map ->
			beans.forEach { bean ->
				if (bean is Component) {
					map[bean.id] = getValue(bean)
				} else {
					// Non-Component Beans don't support multi-property editing
					map[0] = getValue(bean)
				}
			}
		}
	}

	/** Gets the property value of the specified [Bean]. */
	protected abstract fun getValue(bean: Bean): V?

	/** Sets [value] on the property of the specified [Bean] with identifications [beanIds]. */
	protected abstract fun setValue(bean: Bean, value: V?)

	protected val beans get() = beanProvider(editor!!, beanIds)

	override fun getDescription(): String = Translations.getString("$propertyBaseKey.name", *baseKeyParams)

	override fun getDetailedDescription(): String =
		if (beanIds.size == 1) {
			val id = beanIds.first()
			val bean = beans.first()
			"${super.getDetailedDescription()} ${bean::class.simpleName} $id"
		} else {
			"${super.getDetailedDescription()} ${beanIds.size} components"
		}

	override fun execute() {
		if (oldValues == null) {
			establishOldValues()
		}
		beans.forEach { setValue(it, newValue) }
	}

	override fun undo() {
		beans.forEach {
			if (it is Component) {
				setValue(it, oldValues!![it.id])
			} else {
				// Non-Component Beans don't support multi-property editing
				setValue(it, oldValues!![0])
			}
		}
	}
}
