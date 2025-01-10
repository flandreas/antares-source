package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.ApplicationTooOldException
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.io.ZipUtil
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.graphics.Image
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.GraphQuota
import ch.scorpion.jabbah.graph.GraphQuotaException
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream

/**
 * Base class for implementing classes that import [Libraries][Library] from ZIP files.
 */
abstract class AbstractLibraryImporter(
	private val libraryFileName: String = AbstractFileLibraryPersistenceService.DEF_LIBRARY_FILE_NAME
) {

	companion object {
		private val LOG by logger(AbstractLibraryImporter::class)
	}

	/**
	 * Importing a [Library] by reading its data from [inputStream], replacing it if
	 * it already exists if [replaceExisting] is `true`.
	 *
	 * @throws IllegalArgumentException if an error occurred while reading from [inputStream]
	 * @throws LibraryImportConflictException if a [Library] with the [UUID] of the imported [Library] already exists
	 * and [replaceExisting] is false
	 * @throws GraphQuotaException if the user's [GraphQuota] are not sufficient to import the [Library]
	 * @return the imported [Library]
	 */
	suspend fun import(
		inputStream: InputStream,
		replaceExisting: Boolean,
		currentLibraryCount: Int,
		quota: GraphQuota = GraphQuota.UNLIMITED
	): Library {

		val incubationDirPath = incubate(inputStream)

		val libraryDir = getLibraryDirectory(incubationDirPath)

		val library = loadIncubatedLibrary(incubationDirPath, libraryDir.name)

		val exists = checkIfUuidAlreadyExists(library.identification, replaceExisting)

		checkQuota(library, quota, currentLibraryCount, exists)

		save(library, libraryDir, exists)

		return library
	}

	/**
	 * Checks if a [Library] with the given [LibraryIdentification] already exists.
	 * @return `true` if a [Library] with [identification] already exists
	 * @throws LibraryImportConflictException if [Library] already exists and [replaceExisting] is `false`
	 */
	protected abstract suspend fun checkIfUuidAlreadyExists(identification: LibraryIdentification, replaceExisting: Boolean): Boolean

	/**
	 * Saves the [Library] and the content ([MetaGraphs][MetaGraph], [Images][Image] etc.) contained in the
	 * incubation files at [sourceLibraryDir] into the proper storage system, which depends on the implementation.
	 */
	protected abstract suspend fun save(library: Library, sourceLibraryDir: File, exists: Boolean)

	/**
	 * Import and unzip file to incubation directory.
	 * @return the [Path] of the incubation directory
	 */
	protected fun incubate(inputStream: InputStream): Path {
		val incubationDirPath = Files.createTempDirectory(null)
		inputStream.use { input ->
			ZipInputStream(input).use {
				ZipUtil.unzipFile(incubationDirPath, it)
			}
		}
		return incubationDirPath
	}

	/**
	 * Check the incubated directory structure and returns the incubation directory of the imported [Library].
	 * @throws IllegalArgumentException if the structure is invalid
	 */
	protected fun getLibraryDirectory(incubationDirPath: Path): File {
		val incubationDir = incubationDirPath.toFile()
		val incubationFiles = incubationDir.listFiles()
		if (incubationFiles == null || incubationFiles.size != 1) {
			val msg = "Expected 1 file in zip file, but found ${incubationFiles?.size}"
			LOG.trace(msg)
			throw IllegalArgumentException(msg)
		}
		return incubationFiles[0]
	}

	/**
	 * Loads the [Library] from the library file in the incubation directory.
	 * @throws IllegalArgumentException if the structure is invalid
	 */
	protected fun loadIncubatedLibrary(incubationDirPath: Path, libraryDirName: String): Library {
		val libraryFilePath = FileLibraryPersistenceService.buildLibraryFilePath(incubationDirPath.toAbsolutePath().toString(), libraryDirName, libraryFileName)
		val library = createLibraryFileInputStream(libraryFilePath).use {
			try {
				AbstractFileLibraryPersistenceService.loadLibrary(it)
			} catch (e: ApplicationTooOldException) {
				// Pass on to higher layers for interpretation
				throw e
			} catch (e: Exception) {
				LOG.trace("Could not read library file, possibly not an Graph library")
				throw IllegalArgumentException("Could not read library file", e)
			}
		}
		return library
	}

	/**
	 * Checks if [GraphQuota] are sufficient to import the [Library].
	 * @throws GraphQuotaException if [GraphQuota] are not sufficient
	 */
	protected fun checkQuota(library: Library, quota: GraphQuota, currentLibraryCount: Int, exists: Boolean) {
		if (library.metaGraphCount > quota.maxGraphPerLibrary) {
			val msg = "Only ${quota.maxGraphPerLibrary} graphs per library allowed"
			LOG.trace(msg)
			throw GraphQuotaException(msg, Translations.getString("library.quota.maxGraphPerLibrary.text", quota.maxGraphPerLibrary))
		}

		if (!exists && currentLibraryCount + 1 > quota.maxLibraries) {
			val msg = "Only ${quota.maxLibraries} libraries allowed"
			LOG.trace(msg)
			throw GraphQuotaException(msg, Translations.getString("library.quota.maxLibraries.text", quota.maxLibraries))
		}
	}

	protected fun createLibraryFileInputStream(path: String): InputStream {
		try {
			return FileInputStream(path)
		} catch (e: FileNotFoundException) {
			LOG.error("Library file $path not found")
			throw e
		}
	}
}