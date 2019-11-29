package ch.scorpion.jabbah.graph.library.dictionary

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.io.ElectricXmlReader
import ch.scorpion.jabbah.io.ElectricXmlWriter
import ch.scorpion.jabbah.io.StoreXmlReader
import ch.scorpion.jabbah.io.StoreXmlWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.FileSystems

/**
 * Stores a [LibraryDictionary] as a file in the local file system.
 *
 * @param directoryPath the path to the file system directory where the file is stored
 * @param dictionaryFileName the name of the file
 */
class FileLibraryDictionaryPersistenceService(
	directoryPath: String = "",
	dictionaryFileName: String = "library.dictionary"
) : LibraryDictionaryPersistenceService {

	companion object {
		private val LOG by logger(FileLibraryDictionaryPersistenceService::class)
	}

	private val path: String = FileSystems.getDefault().getPath(directoryPath, dictionaryFileName).toString()

	override fun load(): LibraryDictionary {
		try {
			FileInputStream(path).use {
				try {
					LOG.debug("loading entries")
					val storeReader = StoreXmlReader(ElectricXmlReader(it))
					return storeReader.readStorable() as LibraryDictionary
				} catch (e: Throwable) {
					LOG.error("error while loading dictionary file $path: ${e.message}")
					throw e
				}
			}
		} catch (e: Throwable) {
			LOG.error("error while accessing dictionary file $path: ${e.message}")
			throw e
		}
	}

	override fun store(dictionary: LibraryDictionary) {
		try {
			FileOutputStream(path).use {
				try {
					LOG.debug("storing entries")
					val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
					storeWriter.writeStorable(dictionary)
				} catch (e: Throwable) {
					LOG.error("error while storing dictionary file $path: ${e.message}")
					throw e
				}
			}
		} catch (e: Throwable) {
			LOG.error("error while accessing dictionary file $path: ${e.message}")
		}
	}
}