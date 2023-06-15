package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Settings
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Represents the file system directory where the user's data is stored.
 * Can be switched during the runtime of an [Application].
 */
data class Workspace(
	val userDataDirectoryPath: String
)

/**
 * Holds the one and only current [Workspace].
 * Posts [CurrentWorkspaceEvent] on the system [EventBus] when changed.
 */
class WorkspaceHolder(
	workspace: Workspace,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val settings: Settings = BaseModule.settings
) {

	companion object {
		private val LOG by logger(WorkspaceHolder::class)

		/** The name of the [String] property in [Settings] holding the path of the current [Workspace].*/
		const val PROP_WORKSPACE = "app.workspace"
	}

	val userDataDirectoryPath: String get() = workspace.userDataDirectoryPath

	var workspace: Workspace = workspace
		set(value) {
			if (field != value) {
				field = value
				writeLog()
				eventBus.post(CurrentWorkspaceEvent(field))
				settings.set(PROP_WORKSPACE, field.userDataDirectoryPath)
			}
		}

	init {
		writeLog()
	}

	private fun writeLog() {
		LOG.userTrail("Using workspace $userDataDirectoryPath")
	}
}

data class CurrentWorkspaceEvent(val workspace: Workspace)