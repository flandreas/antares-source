package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.event.PropertyChangeSupport
import kotlin.properties.Delegates

interface Action {

	companion object {
		const val PROP_NAME = "name"
		const val PROP_DESCRIPTION = "description"
		const val PROP_ACCELERATOR = "accelerator"
		const val PROP_ENABLED = "enabled"
		const val PROP_SELECTED = "selected"
		const val PROP_IMAGE_PATH = "imagePath"
	}

	var name: String

	var description: String?

	var accelerator: String?

	var enabled: Boolean

	var selected: Boolean

	var imagePath: String?

	fun execute(event: ActionEvent)

	fun addPropertyChangeListener(l: PropertyChangeListener<Any>)

	fun removePropertyChangeListener(l: PropertyChangeListener<Any>)
}

abstract class AbstractAction(
	name: String,
	description: String?,
	accelerator: String?,
	enabled: Boolean = true,
	selected: Boolean = false,
	imagePath: String? = null
) : Action {

	companion object {
		protected fun translatedName(baseName: String): String = Translations.getString("$baseName.name")
		protected fun translatedDesc(baseName: String): String? = Translations.getOptionalString("$baseName.desc")
		protected fun translatedAccelerator(baseName: String): String? = Translations.getOptionalString(System.getActionAcceleratorKey(baseName))
	}

	constructor(baseName: String) : this(
		translatedName(baseName),
		translatedDesc(baseName),
		translatedAccelerator(baseName))

	protected fun setBaseName(baseName: String) {
		name = translatedName(baseName)
		description = translatedDesc(baseName)
		accelerator = translatedAccelerator(baseName)
	}

	override var name: String by Delegates.observable(name) { _, old, new -> changeSupport.fire(Action.PROP_NAME, old, new) }

	override var description: String? by Delegates.observable(description) { _, old, new -> changeSupport.fire(Action.PROP_DESCRIPTION, old, new) }

	override var accelerator: String? by Delegates.observable(accelerator) { _, old, new -> changeSupport.fire(Action.PROP_ACCELERATOR, old, new) }

	override var enabled: Boolean by Delegates.observable(enabled) { _, old, new -> changeSupport.fire(Action.PROP_ENABLED, old, new) }

	override var selected: Boolean by Delegates.observable(selected) { _, old, new -> changeSupport.fire(Action.PROP_SELECTED, old, new) }

	override var imagePath: String? by Delegates.observable(imagePath) { _, old, new -> changeSupport.fire(Action.PROP_IMAGE_PATH, old, new) }

	private val changeSupport = PropertyChangeSupport<Any>(this)

	override fun addPropertyChangeListener(l: PropertyChangeListener<Any>) {
		changeSupport.add(l)
	}

	override fun removePropertyChangeListener(l: PropertyChangeListener<Any>) {
		changeSupport.remove(l)
	}
}