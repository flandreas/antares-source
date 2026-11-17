package io.antarescircuit.jabbah.app.dump

import io.antarescircuit.jabbah.app.Workspace
import io.antarescircuit.jabbah.app.DesktopApplication
import io.antarescircuit.jabbah.app.module.AppModuleJvm
import io.antarescircuit.jabbah.base.io.ZipUtil
import io.antarescircuit.jabbah.io.StorableCloner
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipOutputStream
import kotlin.io.path.name

/**
 * Creates a dump file of the entire system state used for bug analysis by developers.
 * This might consist of the log file, the currently open [Workspace], and other
 * information that might be useful during analysis.
 */
class SystemDumpService {

	/**
	 * Creates a system dump and stores it in the local [destination]. [currentStorable] must be captured on
	 * the thread owning the model (see [serializeCurrentStorable]); the rest is plain file I/O.
	 */
	fun createDump(
		application: DesktopApplication,
		destination: Path,
		includeWorkspace: Boolean = true,
		currentStorable: String? = serializeCurrentStorable(application),
	) {
		val tempDir = Files.createTempDirectory(null)

		if (includeWorkspace) {
			copyWorkspace(tempDir)
		}
		copyLogfile(application, tempDir)
		currentStorable?.let {
			Files.writeString(Paths.get(tempDir.toAbsolutePath().toString(), "currentStorable.txt"), it)
		}

		zipDirectory(tempDir, destination)
	}

	private fun copyWorkspace(dir: Path) {
		FileUtils.copyDirectory(
			File(AppModuleJvm.workspaceHolder.workspace.userDataDirectoryPath),
			dir.toFile()
		)
	}

	private fun copyLogfile(application: DesktopApplication, dir: Path) {
		FileUtils.copyFileToDirectory(
			Paths.get(application.appDataDirectoryPath.toAbsolutePath().toString(), application.logFileName).toFile(),
			dir.toFile())
	}

	/** Serializes the currently edited state. Call on the EDT, since the model is not thread-safe. */
	fun serializeCurrentStorable(application: DesktopApplication): String? =
		application.controller.getUndoableState()?.let { StorableCloner.serialize(it) }

	private fun zipDirectory(dir: Path, destination: Path) {
		FileOutputStream(destination.toAbsolutePath().toString()).use { output ->
			ZipOutputStream(output).use {
				ZipUtil.zipFile(dir.toFile(), FilenameUtils.removeExtension(destination.last().name), it)
			}
		}
	}
}