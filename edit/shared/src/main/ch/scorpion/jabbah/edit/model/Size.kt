package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.Component

/**
 * Models a three-level size property that can be used by [Component]s that support various sizes.
 */
enum class Size(val customName: String, val factor: Float) {

    SMALL("small", 0.5f),
    MEDIUM("medium", 0.7f),
    LARGE("large", 1.0f);

    companion object {

	    const val BASE_KEY_SIZE = "edit.property.size"

        fun withName(name: String): Size =
            entries.firstOrNull { it.customName == name }
                ?: throw IllegalArgumentException("unknown Size '$name'")
    }

    override fun toString(): String {
        return when (this) {
            SMALL -> Translations.getString("edit.property.size.small.name")
            MEDIUM -> Translations.getString("edit.property.size.medium.name")
            LARGE -> Translations.getString("edit.property.size.large.name")
        }
    }
}