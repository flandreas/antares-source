package ch.scorpion.jabbah.graph.app

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

/**
 * Gets posted on [EventBus] when the current [ApplicationMode] is about to change.
 * The change is not guaranteed to happen, because entering the new mode might be aborted by
 * application logic.
 */
data class ApplicationModeBeginEvent(val applicationMode: ApplicationMode)

/** Gets posted on [EventBus] when the current [ApplicationMode] has changed.*/
data class ApplicationModeEvent(val applicationMode: ApplicationMode)

interface ApplicationModeHolder {

	val currentMode: ApplicationMode

	fun dispose()

	/**
	 * Toggles [ApplicationMode].
	 * @param after the code to be executed after the [ApplicationMode] has been toggled
	 */
	fun setMode(mode: ApplicationMode, after: () -> Unit = {})

	fun updateEditorEditability()
}

class ConstantApplicationModeHolder(private val applicationMode: ApplicationMode) : ApplicationModeHolder {

	override val currentMode: ApplicationMode = applicationMode

	override fun dispose() { }

	override fun setMode(mode: ApplicationMode, after: () -> Unit) {
		throw UnsupportedOperationException("not supported")
	}

	override fun updateEditorEditability() { }
}

class UndefinedApplicationModeHolder : ApplicationModeHolder {

	override val currentMode: ApplicationMode
		get() = throw UnsupportedOperationException("not implemented")

	override fun dispose() {
		throw UnsupportedOperationException("not implemented")
	}

	override fun setMode(mode: ApplicationMode, after: () -> Unit): Unit =
		throw UnsupportedOperationException("not implemented")

	override fun updateEditorEditability() {
		throw UnsupportedOperationException("not implemented")
	}
}