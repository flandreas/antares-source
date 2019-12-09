package ch.scorpion.jabbah.base.io

import ch.scorpion.jabbah.base.logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtil {

	private val LOG by logger(ZipUtil::class)

	fun zipFile(file: File, fileName: String, zipOut: ZipOutputStream) {
		if (file.isHidden) {
			return
		}
		LOG.debug(".. zipping $fileName")
		if (file.isDirectory) {
			file.listFiles()?.forEach { zipFile(it, "$fileName/${it.name}", zipOut) }
		} else {
			FileInputStream(file).use {
				val zipEntry = ZipEntry(fileName)
				zipOut.putNextEntry(zipEntry)
				val buffer = ByteArray(1024) { 0 }
				var length: Int
				do {
					length = it.read(buffer)
					if (length > 0) {
						zipOut.write(buffer, 0, length)
					}
				} while (length > 0)
			}
		}
	}

	fun unzipFile(destDir: Path, zipIn: ZipInputStream) {
		val buffer = ByteArray(1024) { 0 }
		var zipEntry = zipIn.nextEntry
		while (zipEntry != null) {
			val newFile = newFile(destDir.toFile(), zipEntry)
			FileOutputStream(newFile).use {
				var length: Int
				do {
					length = zipIn.read(buffer)
					if (length > 0) {
						it.write(buffer, 0, length)
					}

				} while (length > 0)
			}
			zipEntry = zipIn.nextEntry
		}
	}

	/**
	 * Returns the destination [File] of the specified [ZipEntry] by checking that it is a
	 * subdirectory of the overall directory, hereby guarding against the "Zip Slip" vulnerability.
	 * See https://www.baeldung.com/java-compress-and-uncompress.
	 */
	private fun newFile(destDir: File, zipEntry: ZipEntry): File {
		val destFile = File(destDir, zipEntry.name)
		val destDirPath = destDir.canonicalPath
		val destFilePath = destFile.canonicalPath

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw IOException("Entry is outside of the target dir: ${zipEntry.name}")
		}

		return destFile
	}
}