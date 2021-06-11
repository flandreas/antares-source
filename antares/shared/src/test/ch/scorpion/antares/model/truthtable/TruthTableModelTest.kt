package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.signal.Bit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [TruthTableModel].
 */
class TruthTableModelTest {

	@Test
	fun shouldAssignDefaultColumnNames() {
		val model = TruthTableModel(3, 2)
		assertEquals("A", model.inputColumns[0].name)
		assertEquals("B", model.inputColumns[1].name)
		assertEquals("C", model.inputColumns[2].name)
		assertEquals("O1", model.outputColumnNames[0])
		assertEquals("O2", model.outputColumnNames[1])
	}

	@Test
	fun singleOutputShouldHaveUnnumberedColumnName() {
		val model = TruthTableModel(2, 1)
		assertEquals("A", model.inputColumns[0].name)
		assertEquals("B", model.inputColumns[1].name)
		assertEquals("O", model.outputColumnNames[0])
	}

	@Test
	fun shouldPredefineModel() {
		val model = TruthTableModel(2, 1)
		assertEquals(4, model.rows.size)
		assertTrue(arrayOf(Bit.False, Bit.False).contentEquals(model.rows[0].input))
		assertTrue(arrayOf(Bit.True, Bit.False).contentEquals(model.rows[1].input))
		assertEquals(Bit.False, model.outputOf(arrayOf(Bit.True, Bit.True))[0])
	}

	@Test
	fun shouldPredefineWithInts() {
		val model = TruthTableModel(2, 1)
		model.define(intArrayOf(0, 0), 0)
		model.define(intArrayOf(0, 1), 1)
		model.define(intArrayOf(1, 0), 1)
		model.define(intArrayOf(1, 1), 1)
		assertTrue(intArrayOf(0).contentEquals(model.outputOf(intArrayOf(0, 0))))
		assertTrue(intArrayOf(1).contentEquals(model.outputOf(intArrayOf(1, 1))))
	}

	@Test
	fun shouldGetDefinedOutputs() {
		val model = createOrGateModel()
		assertEquals(Bit.False, model.outputOf(arrayOf(Bit.False, Bit.False))[0])
		assertEquals(Bit.True, model.outputOf(arrayOf(Bit.True, Bit.False))[0])
	}

	private fun createOrGateModel(): TruthTableModel {
		val model = TruthTableModel(2, 1)
		model.define(arrayOf(Bit.False, Bit.False), Bit.False)
		model.define(arrayOf(Bit.False, Bit.True), Bit.True)
		model.define(arrayOf(Bit.True, Bit.False), Bit.True)
		model.define(arrayOf(Bit.True, Bit.True), Bit.False)
		return model
	}

}