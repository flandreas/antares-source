package ch.scorpion.antares.filebased.library

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitWidth.Companion.BW_8
import ch.scorpion.antares.model.signal.DigitalSignalFactory.of
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class Incrementer8BitTest : AbstractSystemLibraryTest() {

    @BeforeTest
    fun setupCircuit() {
        configure()
        openCircuitWithElement(UUID("81e0ad9b-4678-4e79-aeef-8f59b153f2e3"))
    }

    @Test
    fun test() {
        execute(
            mapOf("D" to of(BW_8, 254L)),
            mapOf("O" to of(BW_8,255), "CO" to of(Bit.False))
        )
    }
}