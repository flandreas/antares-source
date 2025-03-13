package ch.scorpion.antares.model.fsm

import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.StringUtils.isBlank
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.Memory
import ch.scorpion.jabbah.base.logger
import kotlin.math.max

class FSMException(message: String) : Exception(message)

/**
 * Creates a [TruthTable] representing the logic of an [FSMDrawing].
 */
class FSMTruthTableCreator(
    private val fsm: FSMDrawing,
    private val stateVar: String = DEF_STATE_VAR,
    private val service: FSMEditorService = AntaresModelModule.fsmEditorService
) {

    companion object {
        private val LOG by logger(FSMTruthTableCreator::class)

        const val DEF_STATE_VAR = "Z"
        const val DEF_INPUT_NAME = "I"
        const val DEF_OUTPUT_NAME = "O"
    }

    private val memory = Memory()

    private val states: Collection<FSMState> = fsm.states

    private val transitions: Collection<FSMTransition> = fsm.transitions

    /** The number of bits required to address all [FSMState]s. */
    private val stateBits: Int = calculateStateVariableBits(states)

    private val initState: FSMState = getInitState(states)

    private val parsedTransitions: MutableMap<FSMTransition, ParsedTransition> =
        parseTransitions()

    private val parsedStateOutputs: MutableMap<FSMState, ParsedStateOutput> =
        parseStateOutputs()

    private val inputSignalNames = mutableListOf<String>()

    private val outputSignalNames = mutableListOf<String>()

    private val inputColumnNames = mutableListOf<String>()

    private val outputColumnNames = mutableListOf<String>()

    /**
     * Creates a [TruthTable] representing the logic of [fsm].
     * @throws FSMException if validation of [fsm] failed
     */
    fun create(): TruthTable {
        inputSignalNames.addAll(
            parsedTransitions.values
                .flatMap { it.inputNames }
                .toSet())
        if (inputSignalNames.isEmpty()) {
            inputSignalNames.add(DEF_INPUT_NAME)
        }
        inputColumnNames.addAll(getStateInputColumnNames(stateBits))
        inputColumnNames.addAll(inputSignalNames)

        outputSignalNames.addAll(
            parsedTransitions.values
                .filter { it.outputName != null }
                .map { it.outputName as String }
                .toSet())
        outputSignalNames.addAll(
            parsedStateOutputs.values
                .filter { it.outputName != null && !outputSignalNames.contains(it.outputName) }
                .map { it.outputName as String }
                .toSet())
        if (outputSignalNames.isEmpty()) {
            outputSignalNames.add(DEF_OUTPUT_NAME)
        }
        outputColumnNames.addAll(getStateVariableOutputColumns(stateBits))
        outputColumnNames.addAll(outputSignalNames)

        val truthTable = TruthTable(
            fsm.name.getTranslation(),
            inputColumnNames,
            outputColumnNames,
            stateBits)

        // Set "Don't care" on all output column values
        for (i in 0 until outputColumnNames.size) {
            // TruthTable uses Bit.Error (X) for "Any value"
            truthTable.setColumnValue(inputColumnNames.size + i, Bit.Error)
        }

        fillLogic(truthTable)

        return truthTable
    }

    private fun calculateStateVariableBits(states: Collection<FSMState>): Int {
        val numbers = mutableSetOf<Int>()
        var maxNumber = 0
        for (state in states) {
            maxNumber = max(maxNumber, state.stateNumber)
            if (numbers.contains(state.stateNumber)) {
                exception("antares.fsm.duplicateStateNumber.error", state.stateNumber)
            }
            numbers.add(state.stateNumber)
        }
        return BitOperation.bitCount(maxNumber.toULong())
    }

    private fun getInitState(states: Collection<FSMState>): FSMState {
        val initStates = states.filter { it.stateType == FSMStateType.Initial }
        if (initStates.size != 1) {
            exception("antares.fsm.notOneInitState.error", initStates.size)
        }
        if (initStates.first().stateNumber != 0) {
            exception("antares.fsm.initStateMustHaveNumber0.error")
        }
        return initStates.first()
    }

    private fun getStateInputColumnNames(stateBits: Int): List<String> {
        val result = mutableListOf<String>()
        for (i in stateBits - 1 downTo 0) {
            result.add("${stateVar}_$i^n")
        }
        return result
    }

    private fun getStateVariableOutputColumns(stateBits: Int): List<String> {
        val result = mutableListOf<String>()
        for (i in stateBits - 1 downTo 0) {
            outputColumnNames.add("${stateVar}_$i^(n+1)")
        }
        return result
    }

    private fun parseTransitions(): MutableMap<FSMTransition, ParsedTransition> {
        val parsedTransitions = mutableMapOf<FSMTransition, ParsedTransition>()
        for (t in transitions) {
            try {

                val inputNames: Set<String>
                val interpreter: FSMTransitionConditionInterpreter

                // ---- Conditions

                if (isBlank(t.condition)) {
                    exception("antares.fsm.emptyTransitionCondition.error", getState(t.origStateId).stateNumber)
                }

                val result: FSMTransitionConditionParseResult

                try {
                    result = AntaresModelModule.fsmTransitionService.parseTransitionCondition(t.condition)
                } catch (e: Throwable) {
                    exception("antares.fsm.invalidTransitionCondition.error", getState(t.origStateId).stateNumber, e.message ?: "")
                }

                if (result.variableNames.isEmpty() && parsedTransitions.values.any { pt -> pt.inputNames.isNotEmpty() }) {
                    exception("antares.fsm.inconsistentTransitionConditionNaming.error")
                }
                if (result.variableNames.isNotEmpty() && parsedTransitions.values.any { t -> t.inputNames.isEmpty() }) {
                    exception("antares.fsm.inconsistentTransitionConditionNaming.error")
                }

                inputNames = result.variableNames
                interpreter = FSMTransitionConditionInterpreter(result.ast, memory)

                // ---- Outputs

                if (!isBlank(t.output) && !isBlank(getState(t.origStateId).output)) {
                    exception("antares.fsm.outputInTransitionAndState.error", getState(t.origStateId).stateNumber)
                }
                var outputName: String? = null
                var outputValue: Int? = null
                if (StringUtils.isNotBlank(t.output)) {
                    val terms = t.output.trim().split("=")
                    when (terms.size) {
                        1 -> {
                            outputName = null
                            outputValue = terms[0].toInt()
                        }
                        2 -> {
                            outputName = terms[0]
                            outputValue = terms[1].toInt()
                        }
                        else -> {
                            exception("antares.fsm.invalidTransitionOutput.error")
                        }
                    }
                }
                if (outputValue != null && outputName == null && parsedTransitions.values.any { pt -> pt.outputName != null }) {
                    exception("antares.fsm.inconsistentTransitionOutputNaming.error")
                }
                val unnamedOutput = parsedTransitions.values.firstOrNull { pt -> pt.outputValue != null && pt.outputName == null }
                if (outputValue != null && outputName != null && unnamedOutput != null) {
                    exception("antares.fsm.inconsistentTransitionOutputNaming.error")
                }

                parsedTransitions.getOrPut(t) { ParsedTransition(t, inputNames, interpreter, outputName, outputValue) }

            } catch (e: NumberFormatException) {
                exception("antares.fsm.invalidTransitionConditionValue.error", getState(t.origStateId).stateNumber)
            } catch (e: FSMException) {
                // Validation: re-throw without logging
                throw e
            } catch (e: Throwable) {
                LOG.error("Error when creating FSMTruthTable", e)
                exception("antares.fsm.generalTransitionError.error", getState(t.origStateId).stateNumber)
            }
        }
        return parsedTransitions
    }

    private fun parseStateOutputs(): MutableMap<FSMState, ParsedStateOutput> {
        val parsedStateOutputs = mutableMapOf<FSMState, ParsedStateOutput>()
        for (state in states) {
            var outputName: String? = null
            var outputValue: Int? = null
            if (StringUtils.isNotBlank(state.output)) {
                val terms = state.output.trim().split("=")
                when (terms.size) {
                    1 -> {
                        outputName = null
                        outputValue = terms[0].toInt()
                    }
                    2 -> {
                        outputName = terms[0]
                        outputValue = terms[1].toInt()
                    }
                    else -> {
                        exception("antares.fsm.invalidTransitionOutput.error")
                    }
                }
                if (outputValue != null && outputValue > 1) {
                    exception("antares.fsm.valueOutOfRangeInState.error", state.stateNumber)
                }
                parsedStateOutputs[state] = ParsedStateOutput(state, outputName, outputValue)
            }
        }
        return parsedStateOutputs
    }

    private fun fillLogic(truthTable: TruthTable) {
        for (state in states) {
            val bitWidth = BitWidth.of(inputSignalNames.size)

            // Iterate over all input value combinations
            for (inputSignal in 0UL until BitOperation.power(bitWidth.width.toByte())) {

                val word = DigitalSignalFactory.of(bitWidth, inputSignal)
                word.bits.forEachIndexed { index, bit ->
                    val signalName = inputSignalNames.reversed()[index]
                    memory.preset(signalName, bit.numericalValue.toLong())
                }

                // For every input value:
                // - Find the matching outgoing transition, and fill truthTable for it
                var matchingTransition: FSMTransition? = null
                for (transition in service.getOutgoingTransitions(state, fsm)) {
                    val parsedTransition = parsedTransitions[transition]
                    val match = parsedTransition?.match() ?: false

                    // -- If more than one match: Exception
                    if (match) {
                        if (matchingTransition != null) {
                            exception("antares.fsm.multiTransitionMatch.error", state.stateNumber)
                        }
                        matchingTransition = transition
                    }
                }

                if (matchingTransition == null) {
                    // - If none found: Fill truthTable with "Stay in state"
                    writeTransition(truthTable, state, state, inputSignal.toInt(), null)
                } else {
                    // State change along the one and only matching transition
                    writeTransition(truthTable, state, service.getState(matchingTransition.destinationStateId, fsm), inputSignal.toInt(), matchingTransition)
                }
            }
        }
    }

    private fun rowOfStateNumber(stateNumber: Int): Int =
        stateNumber * BitOperation.power(inputSignalNames.size.toByte()).toInt()

    /**
     * @param transition only `null` for implicit "self" transitions
     */
    private fun writeTransition(truthTable: TruthTable, from: FSMState, to: FSMState, inputSignal: Int, transition: FSMTransition?) {
        val row = rowOfStateNumber(from.stateNumber) + inputSignal
        writeDestinationStateNumber(truthTable, row, to.stateNumber)

        // Caution: The "to" argument intentionally used the "from" state, because the output of the current row of the
        // truth table represents the output produced in the CURRENT state (which is the "from" state)
        writeOutputSignal(truthTable, row, from, parsedStateOutputs[from], to, transition?.let { parsedTransitions[it] })
    }

    private fun writeDestinationStateNumber(truthTable: TruthTable, row: Int, stateNumber: Int) {
        val stateNumberBits = DigitalSignalFactory.of(BitWidth.of(stateBits), stateNumber.toLong()).bits
        val minColumn = truthTable.columnCount - outputSignalNames.size - 1
        stateNumberBits.forEachIndexed { index, bit ->
            truthTable.setValue(row, minColumn - index, bit)
        }
    }

    private fun writeOutputSignal(truthTable: TruthTable, row: Int, from: FSMState, fromOutput: ParsedStateOutput?, to: FSMState, transition: ParsedTransition?) {
        var fromStateColumnId: Int? = null
        var fromStateSignal: Bit? = null
        if (fromOutput?.outputValue != null) {
            fromStateColumnId = if (isBlank(fromOutput.outputName)) {
                truthTable.columnCount - 1
            } else {
                truthTable.columnCount - outputSignalNames.indexOf(fromOutput.outputName) - 1
            }
            fromStateSignal = Bit.of(fromOutput.outputValue)
        }
        var fromTransitionColumnId: Int? = null
        var fromTransitionSignal: Bit? = null
        if (transition?.outputValue != null) {
            if (transition.outputValue > 1) {
                exception("antares.fsm.valueOutOfRangeInTransition.error", from.stateNumber, to.stateNumber)
            }
            fromTransitionColumnId = if (isBlank(transition.outputName)) {
                truthTable.columnCount - 1
            } else {
                truthTable.columnCount - outputSignalNames.indexOf(transition.outputName) - 1
            }
            fromTransitionSignal = Bit.of(transition.outputValue)
        }

        if (fromStateColumnId == fromTransitionColumnId) {
            if (fromStateSignal != fromTransitionSignal) {
                exception("antares.fsm.outputSignalConflict.error", from.stateNumber, to.stateNumber)
            }
        }

        // Set fallback default value
        for (column in truthTable.columnCount - outputSignalNames.size  until truthTable.columnCount) {
            truthTable.setValue(row, column, Bit.False)
        }

        if (fromStateColumnId != null) {
            truthTable.setValue(row, fromStateColumnId, fromStateSignal!!)
        }
        if (fromTransitionColumnId != null) {
            truthTable.setValue(row, fromTransitionColumnId, fromTransitionSignal!!)
        }
    }

    private fun exception(key: String, vararg params: Any): Nothing {
        throw FSMException(Translations.getString(key, *params))
    }

    private fun getState(id: Int): FSMState = states.find { it.id == id }!!

    data class ParsedTransition(
        val transition: FSMTransition,
        val inputNames: Set<String> = emptySet(),
        val conditionInterpreter: FSMTransitionConditionInterpreter,
        val outputName: String? = null,
        val outputValue: Int? = null
    ) {

        /**
         * Returns `true`if this [ParsedTransition] is triggered by the current values
         * in the [conditionInterpreter]'s [Memory] (set in calling scope), i.e. if the
         * expression matches the current input variable values.
         */
        fun match(): Boolean =
            if (inputNames.isNotEmpty()) {
                // If parsing produced variable names, use the interpreter to interpret the expression against the Memory value.
                conditionInterpreter.interpret(keepMemory = true) == 1L
            } else {
                // If parsing didn't produce variable names, the transition condition is an unnamed literal value.
                // Perform a hand-made comparison of that value against the Memory value.
                try {
                    conditionInterpreter.memory.getValue(DEF_INPUT_NAME) == transition.condition.toLong()
                } catch (e: NumberFormatException) {
                    false
                }
            }
    }

    data class ParsedStateOutput(
        val state: FSMState,
        val outputName: String? = null,
        val outputValue: Int? = null
    )
}