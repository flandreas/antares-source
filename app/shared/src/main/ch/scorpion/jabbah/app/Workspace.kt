package ch.scorpion.jabbah.app

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

class WorkspaceHolder(
	workspace: Workspace,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	companion object {
		private val LOG by logger(WorkspaceHolder::class)
	}

	val userDataDirectoryPath: String get() = workspace.userDataDirectoryPath.toString()

	var workspace: Workspace = workspace
		set(value) {
			if (field != value) {
				field = value
				writeLog()
				eventBus.post(CurrentWorkspaceEvent(field))
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