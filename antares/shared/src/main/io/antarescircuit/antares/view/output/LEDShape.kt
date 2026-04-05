package io.antarescircuit.antares.view.output

import io.antarescircuit.jabbah.base.EnumProperty
import io.antarescircuit.jabbah.base.Translations

enum class LEDShape(
    override val customName: String,
    val oval: Boolean
) : EnumProperty<LEDShape> {

    Circle("circle", oval = true),
    Square("square", oval = false),
    Striped("stripe", oval = false);

    companion object {
        const val BASE_KEY = "element.property.ledShape"

        fun withName(customName: String): LEDShape =
            entries.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("unknown LEDShape '$customName'")
    }

    override fun toString(): String =
        when (this) {
            Circle -> Translations.getString("$BASE_KEY.circle")
            Square -> Translations.getString("$BASE_KEY.square")
            Striped -> Translations.getString("$BASE_KEY.stripe")
        }
}