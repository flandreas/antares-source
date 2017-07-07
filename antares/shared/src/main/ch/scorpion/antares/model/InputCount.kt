package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.exception.IllegalArgumentException

/**
 * Enumerates the possible number of [InputPort]s of an [AbstractDigitalGate].
 */
enum class InputCount(val count: Int) {
    ZERO(0),
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8);

    companion object {

        fun of(value: Int): InputCount {
            return values().first { it.count == value }
        }

        fun withName(customName: String): InputCount {
            for (inputCount in values()) {
                if (inputCount.customName == customName) {
                    return inputCount
                }
            }
            throw IllegalArgumentException("Unknown InputCount $customName")
        }
    }

    val customName: String get() = count.toString()

    override fun toString(): String {
        return customName
    }
}