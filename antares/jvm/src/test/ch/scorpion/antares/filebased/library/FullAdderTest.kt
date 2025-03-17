package ch.scorpion.antares.filebased.library

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.Bit.*
import ch.scorpion.antares.model.signal.DigitalSignalFactory.of
import ch.scorpion.jabbah.base.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test

class FullAdderTest : AbstractSystemLibraryTest() {

    @BeforeTest
    fun setupCircuit() {
        configure()
        openCircuitWithElement(UUID("08aba425-96c2-4c43-b10b-2e0c72ce8300"))
    }

    @Test
    fun test() {
        execute(
            mapOf("A" to of(False), "B" to of(False), "CI" to of(False)),
            mapOf("S" to of(False), "CO" to of(False)))
        execute(
            mapOf("A" to of(False), "B" to of(False), "CI" to of(True)),
            mapOf("S" to of(True), "CO" to of(False)))
        execute(
            mapOf("A" to of(False), "B" to of(True), "CI" to of(False)),
            mapOf("S" to of(True), "CO" to of(False)))
        execute(
            mapOf("A" to of(False), "B" to of(True), "CI" to of(True)),
            mapOf("S" to of(False), "CO" to of(True)))
        execute(
            mapOf("A" to of(True), "B" to of(False), "CI" to of(False)),
            mapOf("S" to of(True), "CO" to of(False)))
        execute(
            mapOf("A" to of(True), "B" to of(False), "CI" to of(True)),
            mapOf("S" to of(False), "CO" to of(True)))
        execute(
            mapOf("A" to of(True), "B" to of(True), "CI" to of(False)),
            mapOf("S" to of(False), "CO" to of(True)))
        execute(
            mapOf("A" to of(True), "B" to of(True), "CI" to of(True)),
            mapOf("S" to of(True), "CO" to of(True)))
    }
}