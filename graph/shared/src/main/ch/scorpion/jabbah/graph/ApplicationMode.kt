package ch.scorpion.jabbah.graph

/**
 * Represents the supported application mode of a [graph] application.
 */
enum class ApplicationMode(val nameKey: String) {
    EDIT("application.mode.edit"),
    EXECUTE("application.mode.simulation");
}

/** Gets posted on [EventBus] when the current [ApplicationMode] has changed.*/
data class ApplicationModeEvent(val applicationMode: ApplicationMode)

interface ApplicationModeHolder {

	val currentMode: ApplicationMode

	/**
	 * Toggles [ApplicationMode].
	 * @param after the code to be executed after the [ApplicationMode] has been toggled
	 */
	fun toggleMode(after: () -> Unit = {})
}