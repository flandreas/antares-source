package ch.scorpion.jabbah.base.io

open class Separator(
	private val out: CodePrinter,
	private val sep: String
) {

	private var first = true

	fun check() {
		if (first) {
			first = false
		} else {
			printSeparator(out)
		}
	}

	open fun printSeparator(out: CodePrinter) {
		out.print(sep)
	}
}