package ch.scorpion.jabbah.app.workspace

import ch.scorpion.jabbah.app.Workspace
import ch.scorpion.jabbah.app.module.AppModuleJvm
import java.nio.file.Files
import java.nio.file.Path

class WorkspaceService {

	fun openWorkspace(directory: Path) {
		ensureDirectory(directory)
		AppModuleJvm.workspaceHolder.workspace = Workspace(directory.toAbsolutePath().toString())
	}

	private fun ensureDirectory(directory: Path) {
		if (Files.notExists(directory)) {
			Files.createDirectories(directory)
		}
	}
}