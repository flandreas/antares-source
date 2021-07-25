package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.graph.model.InputPort
import ch.scorpion.antares.model.gate.AbstractDigitalGate

/**
 * Enumerates the possible number of [InputPort]s of an [AbstractDigitalGate].
 */
enum class InputCount(val count: Int) : EnumProperty<InputCount> {
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

	    const val BASE_KEY = "element.property.inputCount"

        fun of(value: Int): InputCount = values().first { it.count == value }

        fun withName(customName: String): InputCount {
            for (inputCount in values()) {
                if (inputCount.customName == customName) {
                    return inputCount
                }
            }
            throw IllegalArgumentException("Unknown InputCount $customName")
        }
    }

    override val customName: String get() = count.toString()

    override fun toString(): String = customName
}