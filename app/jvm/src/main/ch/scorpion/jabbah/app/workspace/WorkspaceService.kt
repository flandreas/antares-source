package ch.scorpion.jabbah.app.workspace

import ch.scorpion.jabbah.app.Workspace
import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.Translations
import java.nio.file.Files
import java.nio.file.Path

class WorkspaceService {

	fun initializeWorkspace(directory: Path) {
		checkPath(directory)
		AppModuleJvm.workspaceHolder.initializeWorkspace(Workspace(directory.toAbsolutePath().toString()))
	}

	fun setWorkspace(directory: Path) {
		checkPath(directory)
		AppModuleJvm.workspaceHolder.setWorkspace(Workspace(directory.toAbsolutePath().toString()))
	}

	private fun checkPath(path: Path) {
		if (!Files.isDirectory(path)) {
			throw IllegalArgumentException(Translations.getString("application.openWorkspace.notDirectory.error"))
		}
		if (Files.notExists(path)) {
			throw IllegalArgumentException(Translations.getString("application.openWorkspace.doesNotExist.error"))
		}
		if (!Files.isReadable(path)) {
			throw IllegalArgumentException(Translations.getString("application.openWorkspace.notReadable.error"))
		}
		if (!Files.isWritable(path)) {
			throw IllegalArgumentException(Translations.getString("application.openWorkspace.notWritable.error"))
		}
	}
}