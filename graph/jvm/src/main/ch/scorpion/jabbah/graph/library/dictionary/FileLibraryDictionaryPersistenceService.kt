package ch.scorpion.jabbah.graph.library.dictionary

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

const val DEF_DICTIONARY_FILE_NAME = "dictionary.xml"

/**
 * Stores a [LibraryDictionary] as a file in the local file system.
 *
 * @param baseDirectoryProvider provides the path to the data base directory
 * @param directoryName the name of the directory in [baseDirectoryProvider] containing [dictionaryFileName]
 * @param dictionaryFileName the name of the file
 */
class FileLibraryDictionaryPersistenceService(
	private val baseDirectoryProvider: () -> String,
	private val directoryName: String,
	private val dictionaryFileName: String = DEF_DICTIONARY_FILE_NAME
) : LibraryDictionaryPersistenceService {

	companion object {
		private val LOG by logger(FileLibraryDictionaryPersistenceService::class)
	}

	private val directoryPath: String get() = "${baseDirectoryProvider()}${FileSystems.getDefault().separator}$directoryName"

	private val filePath: String get() = FileSystems.getDefault().getPath(directoryPath, dictionaryFileName).toString()

	override fun load(): LibraryDictionary {
		if (!Files.exists(Paths.get(directoryPath))) {
			return LibraryDictionary()
		}

		try {
			createInputStream().use {
				LOG.trace("loading entries")
				val storeReader = StoreXmlReader(ElectricXmlReader(it))
				return storeReader.readStorable() as LibraryDictionary
			}
		} catch (e: Throwable) {
			LOG.error("error while accessing dictionary file: ${e.message}")
			throw e
		}
	}

	override fun store(dictionary: LibraryDictionary) {
		try {
			ensureLibraryDirectory()
			val tempFile = File.createTempFile("dict", null)
			// First write into a temp file so that in case of an exception, the original file doesn't get emptied
			FileOutputStream(tempFile).use {
				try {
					LOG.trace("storing entries")
					val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
					storeWriter.writeStorable(dictionary)
					it.flush()
					FileUtils.copyFile(tempFile, File(filePath))
				} catch (e: Throwable) {
					LOG.error("error while storing dictionary file $filePath: ${e.message}")
					throw e
				}
			}
		} catch (e: Throwable) {
			LOG.error("error while accessing dictionary file $filePath: ${e.message}")
		}
	}

	override val directoryExists: Boolean get() = Files.exists(Paths.get(directoryPath))

	private fun createInputStream(): InputStream {
		try {
			return FileInputStream(filePath)
		} catch (e: Throwable) {
			LOG.error("error while loading dictionary file $filePath: ${e.message}")
			throw e
		}
	}

	override fun ensureLibraryDirectory() {
		if (!directoryExists) {
			Files.createDirectory(Paths.get(directoryPath))
		}
	}
}