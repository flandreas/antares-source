package ch.scorpion.antares.model.truthtable

import ch.scorpion.antares.model.signal.Bit
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [TruthTableModel].
 */
class TruthTableModelTest {

    @Test
    fun shouldPredefineModel() {
        val model = TruthTableModel(2, 1)
        assertThat(model.rows.size, `is`(4))
        assertThat(model.rows[0].input, `is`(arrayOf(Bit.False, Bit.False)))
        assertThat(model.rows[1].input, `is`(arrayOf(Bit.True, Bit.False)))
        assertThat(model.outputOf(arrayOf(Bit.True, Bit.True))[0], `is`(Bit.False))
    }

    @Test
    fun shouldPredefineWithInts() {
        val model = TruthTableModel(2, 1)
        model.define(intArrayOf(0, 0), 0)
        model.define(intArrayOf(0, 1), 1)
        model.define(intArrayOf(1, 0), 1)
        model.define(intArrayOf(1, 1), 1)
        assertThat(model.outputOf(intArrayOf(0, 0)), `is`(intArrayOf(0)))
        assertThat(model.outputOf(intArrayOf(1, 1)), `is`(intArrayOf(1)))
    }

    @Test
    fun shouldGetDefinedOutputs() {
        val model = createOrGateModel()
        assertThat(model.outputOf(arrayOf(Bit.False, Bit.False))[0], `is`(Bit.False))
        assertThat(model.outputOf(arrayOf(Bit.True, Bit.False))[0], `is`(Bit.True))
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