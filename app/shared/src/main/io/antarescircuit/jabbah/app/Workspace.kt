package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.Settings
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule

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
	private val eventBus: EventBus = BaseModule.eventBus,
	private val settings: Settings = BaseModule.settings
) {

	companion object {
		private val LOG by logger(WorkspaceHolder::class)

		/** The name of the [String] property in [Settings] holding the path of the current [Workspace].*/
		const val PROP_WORKSPACE = "app.workspace"
	}

	val userDataDirectoryPath: String get() = workspace.userDataDirectoryPath

	// Initially invalid Workspace by intention. The application boot-strap process must install
	// a valid Workspace.
	private var _workspace: Workspace = Workspace("")

	val workspace: Workspace get() = _workspace

	init {
		storeSettings()
		writeLog()
	}

	fun initializeWorkspace(workspace: Workspace) {
		updateWorkspace(workspace)
	}

	fun setWorkspace(workspace: Workspace) {
		if (_workspace != workspace) {
			eventBus.postTwoPhase(
				CurrentWorkspaceEvent(workspace, isPrepare = true),
				CurrentWorkspaceEvent(workspace, isPrepare = false)
			) {
				updateWorkspace(workspace)
			}
		}
	}

	private fun updateWorkspace(workspace: Workspace) {
		_workspace = workspace
		writeLog()
		storeSettings()
	}

	private fun storeSettings() {
		settings.set(PROP_WORKSPACE, workspace.userDataDirectoryPath)
	}

	private fun writeLog() {
		if (userDataDirectoryPath.isNotBlank()) {
			LOG.userTrail("Using workspace $userDataDirectoryPath")
		}
	}
}

data class CurrentWorkspaceEvent(
	val workspace: Workspace,
	val isPrepare: Boolean
)