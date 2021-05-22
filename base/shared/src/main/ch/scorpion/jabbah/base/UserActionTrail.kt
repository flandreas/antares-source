package ch.scorpion.jabbah.base

/**
 * Keeps the last n user actions logged with [LogLevel.Debug] and provides
 * a textual representation of them to be used for reporting issues.
 */
object UserActionTrail {

	private const val SIZE = 20

	private val entries = mutableListOf<String>()

	fun add(entry: String) {
		if (entries.size > SIZE - 1) {
			entries.removeAt(0)
		}
		entries.add(entry)
	}

	override fun toString(): String {
		val builder = StringBuilder()
		entries.forEach { builder.append(it).append('\n') }
		return builder.toString()
	}
}