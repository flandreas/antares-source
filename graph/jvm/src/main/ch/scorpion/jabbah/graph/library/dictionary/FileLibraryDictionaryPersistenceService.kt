package ch.scorpion.jabbah.graph.library.dictionary

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.library.dictionary.AbstractFileLibraryDictionaryPersistenceService.Companion.DEF_DICTIONARY_FILE_NAME
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths


abstract class AbstractFileLibraryDictionaryPersistenceService : LibraryDictionaryPersistenceService {

	companion object {
		private val LOG by logger(AbstractFileLibraryDictionaryPersistenceService::class)
		const val DEF_DICTIONARY_FILE_NAME = "dictionary.xml"
	}

	protected abstract fun createInputStream(): InputStream

	override fun load(): LibraryDictionary {
		try {
			createInputStream().use {
				LOG.debug("loading entries")
				val storeReader = StoreXmlReader(ElectricXmlReader(it))
				return storeReader.readStorable() as LibraryDictionary
			}
		} catch (e: Throwable) {
			LOG.error("error while accessing dictionary file: ${e.message}")
			throw e
		}
	}
}

/**
 * Stores a [LibraryDictionary] as a file in the local file system.
 *
 * @param directoryPath the path to the file system directory where the file is stored
 * @param dictionaryFileName the name of the file
 */
class FileLibraryDictionaryPersistenceService(
	private val directoryPath: String,
	dictionaryFileName: String = DEF_DICTIONARY_FILE_NAME
) : AbstractFileLibraryDictionaryPersistenceService() {

	companion object {
		private val LOG by logger(FileLibraryDictionaryPersistenceService::class)
	}

	private val filePath: String = FileSystems.getDefault().getPath(directoryPath, dictionaryFileName).toString()

	override fun createInputStream(): InputStream {
		try {
			return FileInputStream(filePath)
		} catch (e: Throwable) {
			LOG.error("error while loading dictionary file $filePath: ${e.message}")
			throw e
		}
	}

	override fun load(): LibraryDictionary {
		if (!Files.exists(Paths.get(directoryPath))) {
			return LibraryDictionary()
		}
		return super.load()
	}

	override fun store(dictionary: LibraryDictionary) {
		try {
			ensureLibraryDirectory()
			FileOutputStream(filePath).use {
				try {
					LOG.debug("storing entries")
					val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
					storeWriter.writeStorable(dictionary)
				} catch (e: Throwable) {
					LOG.error("error while storing dictionary file $filePath: ${e.message}")
					throw e
				}
			}
		} catch (e: Throwable) {
			LOG.error("error while accessing dictionary file $filePath: ${e.message}")
		}
	}

	private fun ensureLibraryDirectory() {
		val path = Paths.get(directoryPath)
		if (!Files.exists(path)) {
			Files.createDirectory(path)
		}
	}
}

/**
 * Reads a [LibraryDictionary] from the code's resource directory. Used for system [Libraries][Library].
 */
class ResourceLibraryDictionaryPersistenceService(
	dictionaryFileName: String = DEF_DICTIONARY_FILE_NAME
) : LibraryDictionaryPersistenceService {

	companion object {
		private val LOG by logger((ResourceLibraryDictionaryPersistenceService::class))
		private const val RESOURCE_DIR_PATH = "/libraries"
	}

	private val path: String = "$RESOURCE_DIR_PATH/$dictionaryFileName"

	override fun load(): LibraryDictionary {
		createInputStream().use {
			try {
				val storeReader = StoreXmlReader(ElectricXmlReader(it))
				return storeReader.readStorable() as LibraryDictionary
			} catch (e: Throwable) {
				LOG.error("error while loading dictionary file $path: ${e.message}")
				throw e
			}
		}
	}

	override fun store(dictionary: LibraryDictionary) {
		throw UnsupportedOperationException("Storing system libraries as resources is not supported.")
	}

	private fun createInputStream(): InputStream =
		ResourceLibraryDictionaryPersistenceService::class.java.getResourceAsStream(path)
}