package ch.scorpion.jabbah.base.preferences

/**
 * An object capable of showing and hiding validation messages in the context
 * of [Preferences][Preference].
 */
interface PreferencesMessageDisplay {

	/** Shows the specified message. */
	fun showMessage(message: String)

	/** Hides any previously shown message.*/
	fun hideMessage()
}