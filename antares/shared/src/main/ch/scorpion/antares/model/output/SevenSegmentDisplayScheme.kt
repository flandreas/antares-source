package ch.scorpion.antares.model.output

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPortImpl
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException

/**
 * Represents the supported [Port] schemes of [SevenSegmentDisplay]s.
 */
enum class SevenSegmentDisplayScheme(val customName: String) {

    SINGLE("single") {
        override fun createPorts(display: SevenSegmentDisplay) {
            display.addPort(DigitalPortImpl.createInput("a"))
            display.addPort(DigitalPortImpl.createInput("b"))
            display.addPort(DigitalPortImpl.createInput("c"))
            display.addPort(DigitalPortImpl.createInput("d"))
            display.addPort(DigitalPortImpl.createInput("e"))
            display.addPort(DigitalPortImpl.createInput("f"))
            display.addPort(DigitalPortImpl.createInput("g"))
            display.addPort(DigitalPortImpl.createInput("p"))
        }

        override fun inputValueOf(display: SevenSegmentDisplay, bitName: String): Boolean {
            val port = display.getInput<DigitalSignal>(bitName)
            return port.getIncomingSignal()!!.bitAt(0).isSet
        }
    },

    COMBINED("combined") {
        override fun createPorts(display: SevenSegmentDisplay) {
            display.addPort(DigitalPortImpl.createInput(Logic.POSITIVE, "s", BitWidth.BW_8))
            display.addPort(DigitalPortImpl.createInput("p"))
        }

        override fun inputValueOf(display: SevenSegmentDisplay, bitName: String): Boolean {
            if (bitName.equals("p")) {
                val port = display.getInput<DigitalSignal>(bitName)
                return port.getIncomingSignal()!!.bitAt(0).isSet
            }
            val port = display.getInput<DigitalSignal>("s")
            return port.getIncomingSignal()!!.bitAt(bitName[0].toInt() - 'a'.toInt()).isSet
        }
    };

    companion object {
        fun withName(customName: String): SevenSegmentDisplayScheme {
            for (s in SevenSegmentDisplayScheme.values()) {
                if (s.customName == customName) {
                    return s
                }
            }
            throw IllegalArgumentException("Unknown SevenSegmentDisplayScheme '$customName'")
        }
    }

    /** Creates the necessary [Port] for and adds them to the specified [SevenSegmentDisplay]. */
    abstract fun createPorts(display: SevenSegmentDisplay)

    /**
     * Returns the input value of the bit with the specified name.
     * @return the value of the bit with name `bitName`
     */
    abstract fun inputValueOf(display: SevenSegmentDisplay, bitName: String): Boolean

    override fun toString(): String {
        when (this) {
            SINGLE -> return Translations.getString("element.property.SevenSegmentDisplayScheme.single")
            COMBINED -> return Translations.getString("element.property.SevenSegmentDisplayScheme.combined")
        }
    }
}
