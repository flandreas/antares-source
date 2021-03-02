package ch.scorpion.antares.model.net

import ch.scorpion.jabbah.base.Translations

enum class PullDirection(val customName: String) {
	LOW("low"),
	HIGH("high");

	companion object {
		fun withName(customName: String): PullDirection =
			values().firstOrNull { it.customName == customName } ?:
				throw IllegalArgumentException("Unknown PullDirection '$customName'")
	}

	override fun toString(): String {
		return when(this) {
			LOW -> Translations.getString("element.pullDirection.low")
			HIGH -> Translations.getString("element.pullDirection.high")
		}
	}
}