package io.antarescircuit.jabbah.base.io

/**
 * Prints content to an [Appendable] (e.g. a [StringBuilder] or, on the JVM, a `Writer`)
 * while providing multi-level indentation. [close] closes the target if it is [AutoCloseable].
 */
open class CodePrinter(
	protected val out: Appendable,
	private val indentWidth: Int = 2,
) : AutoCloseable {

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

	override fun close() {
		(out as? AutoCloseable)?.close()
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
		out.append(c)
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
			out.append(' ')
		}
	}
}
