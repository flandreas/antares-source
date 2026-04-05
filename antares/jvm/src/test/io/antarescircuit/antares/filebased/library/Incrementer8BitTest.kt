package io.antarescircuit.antares.filebased.library

import io.antarescircuit.antares.model.signal.BitWidth.Companion.BW_8
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory.of
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class Incrementer8BitTest : AbstractSystemLibraryTest() {

    @BeforeTest
    override fun setup() {
        super.setup()
        configure()
        openCircuitWithElement(UUID("81e0ad9b-4678-4e79-aeef-8f59b153f2e3"))
    }

    /** Test every possible value to find the largest propagation delay.*/
    @Test
    fun test() {
        val pairs = mutableListOf<Pair<Map<String, DigitalSignal>, Map<String, DigitalSignal>>>()
        for (i in 0L until 256L) {
            pairs.add(
                Pair(
                    mapOf("D" to of(BW_8, i)),
                    mapOf(
                        "O" to of(BW_8, i + 1),
                        "CO" to of(i >= 255)
                    ),
                ))
        }
        execute(pairs)
    }
}