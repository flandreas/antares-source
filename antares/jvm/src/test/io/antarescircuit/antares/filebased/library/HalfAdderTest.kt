package io.antarescircuit.antares.filebased.library

import io.antarescircuit.antares.model.signal.Bit.*
import io.antarescircuit.antares.model.signal.DigitalSignalFactory.of
import io.antarescircuit.jabbah.base.UUID
import kotlin.test.Test

class HalfAdderTest : AbstractSystemLibraryTest() {

    override fun setup() {
        super.setup()
        configure()
        openCircuitWithElement(UUID("52255dc4-c010-4f6f-8ea6-9c2c8f5f9a82"))
    }

    @Test
    fun test() {
        execute(listOf(
        Pair(
            mapOf("A" to of(False), "B" to of(False)),
            mapOf("S" to of(False), "C" to of(False))),
        Pair(
            mapOf("A" to of(True), "B" to of(False)),
            mapOf("S" to of(True), "C" to of(False))),
        Pair(
            mapOf("A" to of(False), "B" to of(True)),
            mapOf("S" to of(True), "C" to of(False))),
        Pair(
            mapOf("A" to of(True), "B" to of(True)),
            mapOf("S" to of(False), "C" to of(True))),
        Pair(
            mapOf("A" to of(Undefined), "B" to of(Undefined)),
            mapOf("S" to of(False), "C" to of(False)))
        ))
    }
}