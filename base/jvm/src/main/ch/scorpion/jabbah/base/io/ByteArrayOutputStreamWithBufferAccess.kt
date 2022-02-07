package ch.scorpion.jabbah.base.io

import java.io.ByteArrayOutputStream

/**
 * Extends [ByteArrayOutputStream] in order to access its protected buffer [ByteArray].
 * Used to implement in-memory cloning (write followed by read) without creating
 * an intermediate [String].
 */
class ByteArrayOutputStreamWithBufferAccess(
	size: Int = 32
) : ByteArrayOutputStream(size) {

	val buffer: ByteArray get() = buf
}