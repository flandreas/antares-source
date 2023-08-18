package ch.scorpion.jabbah.base.io

import java.io.*
import java.nio.charset.Charset

/**
 * Prints content to an [OutputStream] while providing multi-level indentation.
 */
open class CodePrinter(
	protected val out: OutputStream,
	private val indentWidth: Int = 2,
	private val charset: Charset = Charsets.ISO_8859_1
) : Closeable {

	constructor(file: File): this(FileOutputStream(file))

	/**
	 * The number of indentation levels on new lines. Is incremented by 1.
	 * Is multiplied by [indentWidth] to get the number of blanks used for indentation.
	 */
	private var indent: Int = 0

	/**
	 * `true` if a line break has just been added, and indentation is to be applied before
	 * writing the next content.
	 */
	private var newLine = false

	private val writer = BufferedWriter(OutputStreamWriter(out, charset))

	override fun close() {
		writer.close()
	}

	fun print(s: String): CodePrinter {
		s.forEach { print(it) }
		return this
	}

	fun println(s: String): CodePrinter {
		print(s)
		println()
		return this
	}

	fun println(): CodePrinter {
		print('\n')
		return this
	}

	fun print(i: Int): CodePrinter {
		print(i.toString())
		return this
	}

	fun print(l: Long): CodePrinter {
		print(l.toString())
		return this
	}

	fun print(c: Char): CodePrinter {
		if (newLine && c != '\n') {
			indent()
			newLine = false
		}
		writer.write(c.code)
		if (c == '\n') {
			newLine = true
		}
		return this
	}

	fun inc(): CodePrinter {
		indent++
		return this
	}

	fun dec(): CodePrinter {
		if (indent > 0) {
			indent--
		}
		return this
	}

	private fun indent() {
		for (i in 0 until (indent * indentWidth)) {
			writer.write(' '.code)
		}
	}
}