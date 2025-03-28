package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Translations

enum class FSMStateType(override val customName: String) : EnumProperty<FSMStateType> {
    Initial("initial"),
    Normal("normal"),
    Final("final");

    companion object {
        const val BASE_KEY = "antares.fsm.state.type"

        fun withName(customName: String): FSMStateType =
            entries.firstOrNull { it.customName == customName }
                ?: throw IllegalArgumentException("Unknown FSMStateType $customName")
    }

    override fun toString(): String =
        when (this) {
            Initial -> Translations.getString("$BASE_KEY.initial")
            Normal -> Translations.getString("$BASE_KEY.normal")
            Final -> Translations.getString("$BASE_KEY.final")
        }
}