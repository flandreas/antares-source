package io.antarescircuit.jabbah.graph.library.dictionary

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.graph.library.Library
import io.antarescircuit.jabbah.io.ElectricXmlReader
import io.antarescircuit.jabbah.io.StoreXmlReader
import java.io.InputStream

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

	override val directoryExists: Boolean get() = true

	override fun ensureLibraryDirectory() { }

	private fun createInputStream(): InputStream =
		ResourceLibraryDictionaryPersistenceService::class.java.getResourceAsStream(path)!!
}