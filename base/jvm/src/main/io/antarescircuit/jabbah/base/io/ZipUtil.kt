package io.antarescircuit.jabbah.base.io

import io.antarescircuit.jabbah.base.logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
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
		LOG.trace("zipping $fileName")
		if (file.isDirectory) {
			if (fileName.endsWith("/")) {
				zipOut.putNextEntry(ZipEntry(fileName))
				zipOut.closeEntry()
			} else {
				zipOut.putNextEntry(ZipEntry("$fileName/"))
				zipOut.closeEntry()
			}
			file.listFiles()?.forEach { zipFile(it, "$fileName/${it.name}", zipOut) }
		} else {
			FileInputStream(file).use {
				val zipEntry = ZipEntry(fileName)
				zipOut.putNextEntry(zipEntry)
				val buffer = ByteArray(1024) { 0 }
				var length: Int
				do {
					length = it.read(buffer)
					if (length >= 0) {
						zipOut.write(buffer, 0, length)
					}
				} while (length >= 0)
			}
		}
	}

	fun unzipFile(destDir: Path, zipIn: ZipInputStream) {
		val buffer = ByteArray(1024) { 0 }
		var zipEntry = zipIn.nextEntry
		while (zipEntry != null) {
			val destPath = resolveEntry(destDir, zipEntry)
			if (zipEntry.isDirectory) {
				LOG.trace("creating directory $destPath")
				Files.createDirectories(destPath)
			} else {
				LOG.trace("unzipping to $destPath")
				Files.createDirectories(destPath.parent)
				FileOutputStream(Files.createFile(destPath).toFile()).use {
					var length: Int
					do {
						length = zipIn.read(buffer)
						if (length > 0) {
							it.write(buffer, 0, length)
						}

					} while (length > 0)
				}
			}
			zipEntry = zipIn.nextEntry
		}
	}

	/**
	 * Returns the destination [Path] of the specified [ZipEntry] after checking that it is located
	 * within [destDir], hereby guarding against the "Zip Slip" vulnerability.
	 * @throws IOException if the entry would be located outside of [destDir]
	 */
	private fun resolveEntry(destDir: Path, zipEntry: ZipEntry): Path {
		val root = destDir.toAbsolutePath().normalize()
		val destPath = root.resolve(zipEntry.name).normalize()
		if (!destPath.startsWith(root)) {
			throw IOException("Entry is outside of the target dir: ${zipEntry.name}")
		}
		return destPath
	}
}