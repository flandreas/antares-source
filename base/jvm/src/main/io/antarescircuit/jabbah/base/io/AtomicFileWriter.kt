package io.antarescircuit.jabbah.base.io

import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

object AtomicFileWriter {

	/**
	 * Writes [target] by passing a stream on a temporary file in the same directory to [write] and then
	 * moving the temporary file over [target], so that a failure never leaves [target] truncated.
	 * The move is atomic if supported by the file system.
	 */
	fun write(target: Path, write: (OutputStream) -> Unit) {
		val temp = Files.createTempFile(target.toAbsolutePath().parent, ".${target.fileName}", ".tmp")
		try {
			FileOutputStream(temp.toFile()).use {
				write(it)
				it.flush()
				it.fd.sync()
			}
			try {
				Files.move(temp, target, ATOMIC_MOVE, REPLACE_EXISTING)
			} catch (_: AtomicMoveNotSupportedException) {
				Files.move(temp, target, REPLACE_EXISTING)
			}
		} finally {
			Files.deleteIfExists(temp)
		}
	}
}
