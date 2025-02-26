package ch.scorpion.antares.model.fsm

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.signal.Bit.*
import kotlin.test.Test
import kotlin.test.assertEquals

class FSMTruthTableCreatorTest {

    companion object {
        init {
            AntaresTestRule.configure()
        }
    }

    private val service: FSMService get() = AntaresModelModule.fsmService

    @Test
    fun shouldCreateTruthTable() {
        val fsm = createFSMDrawing()

        val truthTable = FSMTruthTableCreator(fsm).create()

        assertEquals(3, truthTable.inputColumnCount)
        assertEquals(3, truthTable.outputColumnCount)

        assertEquals("O", truthTable.getColumnName(5))
        assertEquals("Z_0^(n+1)", truthTable.getColumnName(4))
        assertEquals("Z_1^(n+1)", truthTable.getColumnName(3))
        assertEquals("I", truthTable.getColumnName(2))
        assertEquals("Z_0^n", truthTable.getColumnName(1))
        assertEquals("Z_1^n", truthTable.getColumnName(0))

        assertEquals(listOf(False, False, False, True, False, True, Error, Error), truthTable.getColumnValues(5))
        assertEquals(listOf(False, True, False, False, False, False, Error, Error), truthTable.getColumnValues(4))
        assertEquals(listOf(False, False, False, True, False, True, Error, Error), truthTable.getColumnValues(3))
        assertEquals(listOf(False, True, False, True, False, True, False, True), truthTable.getColumnValues(2))
        assertEquals(listOf(False, False, True, True, False, False, True, True), truthTable.getColumnValues(1))
        assertEquals(listOf(False, False, False, False, True, True, True, True), truthTable.getColumnValues(0))
    }

    private fun createFSMDrawing(): FSMDrawing {
        val fsm = FSMDrawing()

        val sx = service.createState(fsm).also {
            it.stateType = FSMStateType.Initial
            it.name = Name("SX")
            it.stateNumber = 0
            it.output = "O=0"
            it.location = Point2D(100, 100)
            fsm.add(it)
        }
        val s1 = service.createState(fsm).also {
            it.name = Name("S1")
            it.stateNumber = 1
            it.output = "O=0"
            it.location = Point2D(200, 200)
            fsm.add(it)
        }
        val s11 = service.createState(fsm).also {
            it.name = Name("S11")
            it.stateNumber = 2
            it.output = "O=1"
            it.location = Point2D(200, 100)
            fsm.add(it)
        }

        FSMTransition(sx.id, sx.id).also {
            it.condition = "I=0"
            fsm.add(it)
        }
        FSMTransition(sx.id, s1.id).also {
            it.condition = "I=1"
            fsm.add(it)
        }
        FSMTransition(s1.id, sx.id).also {
            it.condition = "I=0"
            fsm.add(it)
        }
        FSMTransition(s1.id, s11.id).also {
            it.condition = "I=1"
            fsm.add(it)
        }
        FSMTransition(s11.id, s11.id).also {
            it.condition = "I=1"
            fsm.add(it)
        }
        FSMTransition(s11.id, sx.id).also {
            it.condition = "I=0"
            fsm.add(it)
        }

        return fsm
    }
}