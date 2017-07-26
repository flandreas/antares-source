package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.exception.IllegalArgumentException

/**
 * Identifies a particular [InputPort] of an [AbstractDigitalGate].
 */
enum class InputPortNumber(val id: Int) {
    NONE(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8);

    companion object {

        fun of(value: Int): InputPortNumber {
            return InputPortNumber.values().first { it.id == value }
        }

        fun withId(id: Int): InputPortNumber {
            for (inportPortNumber in InputPortNumber.values()) {
                if (inportPortNumber.id == id) {
                    return inportPortNumber
                }
            }
            throw IllegalArgumentException("Unknown InputPortNumber $id")
        }
    }

    val customName: String get() = id.toString()

    override fun toString(): String {
        return when(id) {
            // TODO I18N
            0 -> "None"
            else -> id.toString()
        }
    }
}