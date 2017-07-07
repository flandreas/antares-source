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
        val edge = DigitalNet()
        edge.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p1", BitWidth.BW_4))
        assertThat(edge.isError, `is`(false))
    }

    @Test
    fun shouldNotYieldErrorWithEqualBitWidthPorts() {
        val edge = DigitalNet()
        edge.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p1", BitWidth.BW_4))
        edge.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p2", BitWidth.BW_4))
        assertThat(edge.isError, `is`(false))
    }

    @Test
    fun shouldYieldErrorWithDifferentBitWidthPorts() {
        val edge = DigitalNet()
        edge.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p1", BitWidth.BW_4))
        edge.connect(DigitalPortImpl.createInput(Logic.POSITIVE, "p2", BitWidth.BW_8))
        assertThat(edge.isError, `is`(true))
    }
}