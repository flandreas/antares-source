package ch.scorpion.jabbah.base.io

class Separator(
	private val out: CodePrinter,
	private val sep: String
) {

	private var first = true

	fun check() {
		if (first) {
			first = false
		} else {
			out.print(sep)
		}
	}
}