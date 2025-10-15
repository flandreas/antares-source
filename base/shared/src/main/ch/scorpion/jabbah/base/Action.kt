package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.event.PropertyChangeSupport
import kotlin.js.JsExport
import kotlin.properties.Delegates

/** Cannot be inside [Action] companion object due to [JsExport] limitations.*/
@JsExport
object ActionProperty {
	const val PROP_NAME = "name"
	const val PROP_DESCRIPTION = "description"
	const val PROP_ACCELERATOR = "accelerator"
	const val PROP_ENABLED = "enabled"
	const val PROP_SELECTED = "selected"
	const val PROP_IMAGE_PATH = "imagePath"
}

@JsExport
interface Action {

	var name: String

	var description: String?

	var accelerator: String?

	var enabled: Boolean

	var selected: Boolean

	var imagePath: String?

	/**
	 * Returns `true` if this [Action] opens a dialog, which is used to expand [name] with "..."
	 * in menu items or action tooltips for this [Action].
	 */
	val opensDialog: Boolean get() = false

	fun dispose()

	/** Asks this [Action] to update its "enabled" state. */
	fun updateEnabled()

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
	imagePath: String? = null,
	override val opensDialog: Boolean = false
) : Action {

	companion object {
		protected fun translatedName(baseName: String): String = Translations.getString("$baseName.name")
		protected fun translatedDesc(baseName: String): String? = Translations.getOptionalString("$baseName.desc")
		protected fun translatedAccelerator(baseName: String): String? = Translations.getOptionalString(System.getActionAcceleratorKey(baseName))
	}

	constructor(baseName: String, imagePath: String? = null, opensDialog: Boolean = false) : this(
		translatedName(baseName),
		translatedDesc(baseName),
		translatedAccelerator(baseName),
		imagePath = imagePath,
		opensDialog = opensDialog)

	protected fun setBaseName(baseName: String) {
		name = translatedName(baseName)
		description = translatedDesc(baseName)
		accelerator = translatedAccelerator(baseName)
	}

	/**
	 * Overwritten by subclasses to calculate whether this [AbstractAction] is currently enabled.
	 * Automatically called by [updateEnabled].
	 */
	protected open fun calculateEnabled(): Boolean = true

	override var name: String by Delegates.observable(name) { _, old, new -> changeSupport.fire(ActionProperty.PROP_NAME, old, new) }

	override var description: String? by Delegates.observable(description) { _, old, new -> changeSupport.fire(ActionProperty.PROP_DESCRIPTION, old, new) }

	override var accelerator: String? by Delegates.observable(accelerator) { _, old, new -> changeSupport.fire(ActionProperty.PROP_ACCELERATOR, old, new) }

	override var enabled: Boolean by Delegates.observable(enabled) { _, old, new -> changeSupport.fire(ActionProperty.PROP_ENABLED, old, new) }

	override var selected: Boolean by Delegates.observable(selected) { _, old, new -> changeSupport.fire(ActionProperty.PROP_SELECTED, old, new) }

	override var imagePath: String? by Delegates.observable(imagePath) { _, old, new -> changeSupport.fire(ActionProperty.PROP_IMAGE_PATH, old, new) }

	protected val changeSupport = PropertyChangeSupport<Any>(this)

	override fun dispose() { }

	override fun updateEnabled() {
		enabled = calculateEnabled()
	}

	override fun addPropertyChangeListener(l: PropertyChangeListener<Any>) {
		changeSupport.add(l)
	}

	override fun removePropertyChangeListener(l: PropertyChangeListener<Any>) {
		changeSupport.remove(l)
	}
}