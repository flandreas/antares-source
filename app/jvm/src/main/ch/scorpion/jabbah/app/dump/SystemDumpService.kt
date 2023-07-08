package ch.scorpion.jabbah.app.dump

import ch.scorpion.jabbah.app.Workspace
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.module.AppModuleJvm
import ch.scorpion.jabbah.base.io.ZipUtil
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

	/** Creates a system dump and stores it in the local [destination].*/
	fun createDump(application: DesktopApplication, destination: Path) {
		val tempDir = Files.createTempDirectory(null)

		copyWorkspace(tempDir)
		copyLogfile(application, tempDir)

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

	private fun zipDirectory(dir: Path, destination: Path) {
		FileOutputStream(destination.toAbsolutePath().toString()).use { output ->
			ZipOutputStream(output).use {
				ZipUtil.zipFile(dir.toFile(), FilenameUtils.removeExtension(destination.last().name), it)
			}
		}
	}
}