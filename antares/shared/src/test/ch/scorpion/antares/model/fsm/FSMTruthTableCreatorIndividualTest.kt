package ch.scorpion.antares.model.fsm

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.signal.Bit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FSMTruthTableCreatorIndividualTest {

    private val service: FSMEditorService get() = AntaresModelModule.fsmEditorService

    init {
        AntaresTestRule.configure()
    }

    @Test
    fun shouldCatchInvalidValue() {
        val fsm = FSMDrawing()
        fsm.add(service.createState(fsm).also {
            it.stateType = FSMStateType.Initial
            it.output = "Hallo"
        })

        assertFailsWith<FSMException> {
             FSMTruthTableCreator(fsm).create()
        }
    }

    @Test
    fun shouldInterpretDecimalMultiBitStateOutput() {
        interpretDecimalMultiBitStateOutput("5")
    }

    @Test
    fun shouldInterpretHexadecimalMultiBitStateOutput() {
        interpretDecimalMultiBitStateOutput("0x5")
    }

    @Test
    fun shouldInterpretBinaryMultiBitStateOutput() {
        interpretDecimalMultiBitStateOutput("0b101")
    }

    private fun interpretDecimalMultiBitStateOutput(output: String) {
        val fsm = FSMDrawing()
        val s0 = service.createState(fsm).also {
            it.stateType = FSMStateType.Initial
            it.stateNumber = 0
            it.output = output
            fsm.add(it)
        }
        val s1 = service.createState(fsm).also {
            it.stateNumber = 1
            fsm.add(it)
        }
        FSMTransition(s0.id, s1.id).also {
            it.condition = "0"
            fsm.add(it)
        }

        val truthTable = FSMTruthTableCreator(fsm).create()

        assertEquals(6, truthTable.columnCount) // 1 (self) input, 1 state input, 1 state output, 3 output
        assertEquals(1 + 3, truthTable.outputColumnCount) // 1 for state, 3 for output with max. value 5

        assertEquals("O0", truthTable.getColumnName(5))
        assertEquals("O1", truthTable.getColumnName(4))
        assertEquals("O2", truthTable.getColumnName(3))

        assertEquals(Bit.True, truthTable.getValue(0, 5))
        assertEquals(Bit.False, truthTable.getValue(0, 4))
        assertEquals(Bit.True, truthTable.getValue(0, 3))
    }

    @Test
    fun shouldInterpretDecimalMultiBitTransitionOutput() {
        interpretMultiBitTransitionOutput("O=5")
        interpretMultiBitTransitionOutput("5")
    }

    private fun interpretMultiBitTransitionOutput(output: String) {
        val fsm = FSMDrawing()
        val s1 = service.createState(fsm).also {
            it.stateType = FSMStateType.Initial
            it.stateNumber = 0
            fsm.add(it)
        }
        val s2 = service.createState(fsm).also {
            it.stateNumber = 1
            fsm.add(it)
        }
        FSMTransition(s1.id, s2.id).also {
            it.condition = "I=0"
            it.output = output
            fsm.add(it)
        }

        val truthTable = FSMTruthTableCreator(fsm).create()

        assertEquals(6, truthTable.columnCount) // 1 (self) input, 1 state input, 1 state output, 3 output
        assertEquals(1 + 3, truthTable.outputColumnCount) // 1 for state, 3 for output with max. value 5

        assertEquals("O0", truthTable.getColumnName(5))
        assertEquals("O1", truthTable.getColumnName(4))
        assertEquals("O2", truthTable.getColumnName(3))
    }

    @Test
    fun shouldRejectMultiBitInputValue() {
        val fsm = FSMDrawing()
        val s1 = service.createState(fsm).also {
            it.stateType = FSMStateType.Initial
            it.stateNumber = 0
            it.output = "0"
            fsm.add(it)
        }
        val s2 = service.createState(fsm).also {
            it.stateNumber = 1
            fsm.add(it)
        }
        FSMTransition(s1.id, s2.id).also {
            it.condition = "I=5"
            fsm.add(it)
        }

        assertFailsWith<FSMException> { FSMTruthTableCreator(fsm).create() }
    }

    @Test
    fun shouldInterpretListOfDecimalStateOutputs() {
        interpretListOfStateOutputs("A=0, B=1", listOf("A", "B"))
    }

    @Test
    fun shouldInterpretListOfDecimalMultiBitOutputs() {
        interpretListOfStateOutputs("A=2, B=2", listOf("A1", "A0", "B1", "B0"))
    }

    @Test
    fun shouldInterpretListOfHexadecimalMultiBitOutputs() {
        interpretListOfStateOutputs("A=0x2, B=0x2", listOf("A1", "A0", "B1", "B0"))
    }

    private fun interpretListOfStateOutputs(output: String, outputNames: List<String>) {
        val fsm = FSMDrawing()
        val s0 = service.createState(fsm).also {
            it.stateType = FSMStateType.Initial
            it.stateNumber = 0
            it.output = output
            fsm.add(it)
        }
        val s1 = service.createState(fsm).also {
            it.stateNumber = 1
            fsm.add(it)
        }
        FSMTransition(s0.id, s1.id).also {
            it.condition = "0"
            fsm.add(it)
        }

        val truthTable = FSMTruthTableCreator(fsm).create()

        assertEquals(3 + outputNames.size, truthTable.columnCount) // 1 (self) input, 1 state input, 1 state output, n output
        assertEquals(1 + outputNames.size, truthTable.outputColumnCount) // 1 for state, n for outputs

        for (i in outputNames.indices) {
            assertEquals(outputNames[i], truthTable.getColumnName(truthTable.columnCount - outputNames.size + i))
        }
    }

    @Test
    fun shouldInterpretListOfDecimalTransitionOutputs() {
        interpretListOfTransitionOutputs("A=0, B=1", listOf("B", "A"))
    }

    @Test
    fun shouldInterpretListOfDecimalMultiBitTransitionOutputs() {
        interpretListOfTransitionOutputs("A=2, B=2", listOf("B1", "B0", "A1", "A0"))
    }

    @Test
    fun shouldInterpretListOfHexadecimalMultiBitTransitionOutputs() {
        interpretListOfTransitionOutputs("A=0x2, B=0x2", listOf("B1", "B0", "A1", "A0"))
    }

    private fun interpretListOfTransitionOutputs(output: String, outputNames: List<String>) {
        val fsm = FSMDrawing()
        val s1 = service.createState(fsm).also {
            it.stateType = FSMStateType.Initial
            it.stateNumber = 0
            fsm.add(it)
        }
        val s2 = service.createState(fsm).also {
            it.stateNumber = 1
            fsm.add(it)
        }
        FSMTransition(s1.id, s2.id).also {
            it.condition = "I=0"
            it.output = output
            fsm.add(it)
        }

        val truthTable = FSMTruthTableCreator(fsm).create()

        assertEquals(3 + outputNames.size, truthTable.columnCount) // 1 (self) input, 1 state input, 1 state output, n output
        assertEquals(1 + outputNames.size, truthTable.outputColumnCount) // 1 for state, n for outputs

        for (i in outputNames.indices) {
            assertEquals(outputNames[i], truthTable.getColumnName(truthTable.columnCount - outputNames.size + i))
        }
    }
}