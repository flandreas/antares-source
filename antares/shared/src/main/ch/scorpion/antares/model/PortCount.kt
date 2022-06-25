package ch.scorpion.antares.model

import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.Vertice

/**
 * Enumerates the possible number of [Port]s of a [Vertice].
 */
enum class PortCount(val count: Int) : EnumProperty<PortCount> {
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

	    const val INPUT_COUNT_BASE_KEY = "element.property.inputCount"
	    const val OUTPUT_COUNT_BASE_KEY = "element.property.outputCount"

        fun of(value: Int): PortCount = values().first { it.count == value }

        fun withName(customName: String): PortCount {
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