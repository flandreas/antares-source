package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.io.*
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream

/**
 * An [ApplicationDataRepository] that stores [Savable]s in [File]s
 * @property fileExtension the file name extension to be used for data files handled by this [FileApplicationDataRepository]
 */
class FileApplicationDataRepository(
	private val fileExtension: String
) : ApplicationDataRepository<FileSavable> {

	companion object {
		private val LOG by logger(FileApplicationDataRepository::class)
	}

	override fun createUndefinedSavable(): FileSavable = FileSavable.undefined()

	override fun load(savable: FileSavable): Storable {
		if (savable.notDefined) {
			throw IllegalArgumentException("cannot load undefined Storable")
		}
		try {
			FileInputStream(savable.filePath!!).use {
				try {
					val storeReader = StoreXmlReader(ElectricXmlReader(it))
					return storeReader.readStorable()
					//data = ApplicationData(drawing, FileSavable.withPath(identification), eventBus)
				} catch (e: Throwable) {
					LOG.error("Error while opening '$${savable.filePath}': ${e.cause}")
					throw e
				}
			}
		} catch (e: FileNotFoundException) {
			throw IllegalArgumentException()
		}
	}

	override fun store(savable: FileSavable, storable: Storable) {
		if (savable.notDefined) {
			throw IllegalArgumentException("cannot store undefined Storable")
		}
		var filePath = savable.filePath
		if (!filePath!!.endsWith(fileExtension)) {
			filePath = "$filePath.$fileExtension"
		}
		FileOutputStream(filePath).use {
			try {
				val storeWriter = StoreXmlWriter(ElectricXmlWriter(it))
				storeWriter.writeStorable(storable)
			} catch (e: Throwable) {
				LOG.error("Error while saving '$filePath': ${e.message}")
				throw e
			}
		}
	}
}