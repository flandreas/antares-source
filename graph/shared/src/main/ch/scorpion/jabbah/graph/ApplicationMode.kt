package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.event.EventBus

/**
 * Represents the supported application mode of a [ch.scorpion.jabbah.graph] application.
 */
enum class ApplicationMode(val nameKey: String) {
	EDIT("application.mode.edit"),
	EXECUTE("application.mode.execution"),
	EXEC_USECASE("application.mode.exec_usecase");

	fun isEdit(): Boolean {
		return this === EDIT
	}

	fun isExecute(): Boolean {
		return this === EXECUTE || this === EXEC_USECASE
	}
}

/** Gets posted on [EventBus] when the current [ApplicationMode] has changed.*/
data class ApplicationModeEvent(val applicationMode: ApplicationMode)

interface ApplicationModeHolder {

	val currentMode: ApplicationMode

	/**
	 * Toggles [ApplicationMode].
	 * @param after the code to be executed after the [ApplicationMode] has been toggled
	 */
	fun setMode(mode: ApplicationMode, after: () -> Unit = {})
}

class UndefinedApplicationModeHolder : ApplicationModeHolder {

	override val currentMode: ApplicationMode
		get() = throw UnsupportedOperationException("not implemented")

	override fun setMode(mode: ApplicationMode, after: () -> Unit): Unit =
		throw UnsupportedOperationException("not implemented")
}