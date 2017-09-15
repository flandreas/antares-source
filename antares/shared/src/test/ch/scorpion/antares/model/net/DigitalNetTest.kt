package ch.scorpion.antares.model.net

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.TestTranslationsBuilder
import org.junit.ClassRule
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.port.DigitalPortImpl
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test


/**
 * Unit tests for [DigitalNet].
 */
class DigitalNetTest {

    companion object {
        @ClassRule @JvmField
        val rule = AntaresTestRule()
    }

    @Before
    fun setup() {
        TestTranslationsBuilder()
                .withResource("digitalnet.designError.text")
    }

    @Test
    fun shouldNotYieldErrorWithSinglePorts() {
        val net = DigitalNet()
        net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p1", BitWidth.BW_4))
        assertThat(net.isError, `is`(false))
    }

    @Test
    fun shouldNotYieldErrorWithEqualBitWidthPorts() {
        val net = DigitalNet()
        net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p1", BitWidth.BW_4))
        net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p2", BitWidth.BW_4))
        assertThat(net.isError, `is`(false))
    }

    @Test
    fun shouldYieldErrorWithDifferentBitWidthPorts() {
        val net = DigitalNet()
        net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p1", BitWidth.BW_4))
        net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p2", BitWidth.BW_8))
        assertThat(net.isError, `is`(true))
    }

    @Test
    fun shouldRecognizeAdaptivePort() {
        val net = DigitalNet()
        val adaptivePort = DigitalPortImpl.createInput(Logic.POSITIVE, "p1", BitWidth.BW_1)
        adaptivePort.isAdaptive = true
        net.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p2", BitWidth.BW_2))
        net.connect(adaptivePort)
        assertThat(net.isError, `is`(false))
    }
}