package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.Component

/**
 * Models a three-level size property that can be used by [Component]s that support various sizes.
 */
enum class Size(val customName: String) {

    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large");

    companion object {

        private val LOG by logger(Size::class)

        fun withName(name: String): Size {
            for (size in values()) {
                if (size.customName == name) {
                    return size
                }
            }
            LOG.error("unknown Size '$name'")
            throw IllegalArgumentException("unknown Size '$name'")
        }
    }

    override fun toString(): String {
        return when (this) {
            SMALL -> Translations.getString("edit.property.size.small.name")
            MEDIUM -> Translations.getString("edit.property.size.medium.name")
            LARGE -> Translations.getString("edit.property.size.large.name")
        }
    }
}