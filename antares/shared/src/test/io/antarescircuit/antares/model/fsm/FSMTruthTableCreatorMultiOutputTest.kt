package io.antarescircuit.antares.model.fsm

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.antares.model.signal.Bit.Error
import io.antarescircuit.antares.model.signal.Bit.False
import io.antarescircuit.antares.model.signal.Bit.True
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.jabbah.edit.model.text.description.Name
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests creating a [TruthTable] from a [FSMDrawing] with multiple output signals
 */
class FSMTruthTableCreatorMultiOutputTest {

    private val service: FSMEditorService get() = AntaresModelModule.fsmEditorService

    init {
        AntaresTestRule.configure()
    }

    @Test
    fun shouldCreateTruthTable() {
        val fsm = createFSM()

        val tt = FSMTruthTableCreator(fsm).create()

        assertEquals(3, tt.inputColumnCount)
        assertEquals(4, tt.outputColumnCount)

        assertEquals("O0", tt.getColumnName(6))
        assertEquals("O1", tt.getColumnName(5))
        assertEquals("Z_0^(n+1)", tt.getColumnName(4))
        assertEquals("Z_1^(n+1)", tt.getColumnName(3))
        assertEquals("I", tt.getColumnName(2))
        assertEquals("Z_0^n", tt.getColumnName(1))
        assertEquals("Z_1^n", tt.getColumnName(0))

        assertEquals(listOf(False, True, False, False, False, False, Error, Error), tt.getColumnValues(6))
        assertEquals(listOf(False, False, False, True, False, True, Error, Error), tt.getColumnValues(5))
    }

    private fun createFSM(): FSMDrawing {
        val fsm = FSMDrawing()

        val state00 = service.createState(fsm).also {
            it.stateType = FSMStateType.Initial
            it.name = Name("00")
            it.stateNumber = 0
            it.location = Point2D(100, 0)
            fsm.add(it)
        }
        val state01 = service.createState(fsm).also {
            it.name = Name("00")
            it.stateNumber = 1
            it.location = Point2D(200, 0)
            fsm.add(it)
        }
        val state02 = service.createState(fsm).also {
            it.name = Name("00")
            it.stateNumber = 2
            it.location = Point2D(300, 0)
            fsm.add(it)
        }

        FSMTransition(state00.id, state01.id).also {
            it.condition = "1"
            it.output = "O0=1"
            fsm.add(it)
        }
        FSMTransition(state01.id, state00.id).also {
            it.condition = "0"
            fsm.add(it)
        }
        FSMTransition(state01.id, state02.id).also {
            it.condition = "1"
            it.output = "O1=1"
            fsm.add(it)
        }
        FSMTransition(state02.id, state02.id).also {
            it.condition = "1"
            it.output = "O1=1"
            fsm.add(it)
        }
        FSMTransition(state02.id, state00.id).also {
            it.condition = "0"
            fsm.add(it)
        }

        return fsm
    }
}