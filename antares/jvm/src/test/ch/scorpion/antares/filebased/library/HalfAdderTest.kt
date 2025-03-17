package ch.scorpion.antares.filebased.library

import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.DigitalSignalFactory.of
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class HalfAdderTest : AbstractSystemLibraryTest() {

    @BeforeTest
    fun setupCircuit() {
        configure()
        openCircuitWithElement(UUID("52255dc4-c010-4f6f-8ea6-9c2c8f5f9a82"))
    }

    @Test
    fun test() {
        execute(
            mapOf("A" to of(False), "B" to of(False)),
            mapOf("S" to of(False), "C" to of(False)))
        execute(
            mapOf("A" to of(True), "B" to of(False)),
            mapOf("S" to of(True), "C" to of(False)))
        execute(
            mapOf("A" to of(False), "B" to of(True)),
            mapOf("S" to of(True), "C" to of(False)))
        execute(
            mapOf("A" to of(True), "B" to of(True)),
            mapOf("S" to of(False), "C" to of(True)))
        execute(
            mapOf("A" to of(Undefined), "B" to of(Undefined)),
            mapOf("S" to of(False), "C" to of(False)))
    }
}