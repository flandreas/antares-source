package io.antarescircuit.antares.hdl

interface HDLRenaming {

	/**
	 * `true` if the target language distinguishes identifiers that differ only in case
	 * (e.g. Verilog), `false` otherwise (e.g. VHDL).
	 */
	val isCaseSensitive: Boolean

	/**
	 * Checks the specified [name] for validity. Returns [name] if it is valid,
	 * or an adjusted name otherwise.
	 */
	fun checkName(name: String): String
}

/**
 * Ensures that a particular name is renamed at most once.
 */
class RenameSingleCheck(private val parent: HDLRenaming): HDLRenaming {

	private val checked = mutableMapOf<String, String>()

	override val isCaseSensitive: Boolean get() = parent.isCaseSensitive

	override fun checkName(name: String): String {
		var newName = checked[name]
		if (newName == null) {
			newName = parent.checkName(name)
			checked[name] = newName
		}
		return newName
	}
}
