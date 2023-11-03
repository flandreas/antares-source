package ch.scorpion.antares.hdl

interface HDLRenaming {

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

	override fun checkName(name: String): String {
		var newName = checked[name]
		if (newName == null) {
			newName = parent.checkName(name)
			checked[name] = newName
		}
		return newName
	}
}