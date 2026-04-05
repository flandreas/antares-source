package io.antarescircuit.antares.model.fsm

import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.antares.model.signal.Word
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.StringUtils.isBlank
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.dsl.Memory
import io.antarescircuit.jabbah.base.dsl.RuntimeError
import io.antarescircuit.jabbah.base.logger
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

        /** The max. number of bits in output values. Corresponds with max. number of output ports.*/
        private const val MAX_OUTPUT_BITS = 8
    }

    private val memory = Memory()

    private val states: Collection<FSMState> = fsm.states

    private val transitions: Collection<FSMTransition> = fsm.transitions

    /** The number of bits required to address all [FSMState]s. */
    private val stateBits: Int = calculateStateVariableBits(states)

    private val parsedTransitions: MutableMap<FSMTransition, ParsedTransition> =
        parseTransitions()

    private val parsedStateOutputs: MutableMap<FSMState, MutableList<ParsedStateOutput>> =
        parseStateOutputs()

    private val inputSignalNames = mutableListOf<String>()

    private val outputSignalNames = mutableListOf<String>()

    private val inputColumnNames = mutableListOf<String>()

    private val outputColumnNames = mutableListOf<String>()

    init {
        getInitState(states)
    }

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
                .flatMap { it.outputs }
                .map { it.name }
                .toSet()
                .sortedDescending())
        outputSignalNames.addAll(
            parsedStateOutputs.flatMap { it.value }
                .filter { !outputSignalNames.contains(it.output.name) }
                .map { it.output.name }
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
                if (result.variableNames.isNotEmpty() && parsedTransitions.values.any { it.inputNames.isEmpty() }) {
                    exception("antares.fsm.inconsistentTransitionConditionNaming.error")
                }
                if (result.maxValue > 1) {
                    exception("antares.fsm.valueOutOfRangeInTransition.error", getState(t.origStateId).stateNumber, getState(t.destinationStateId).stateNumber)
                }

                inputNames = result.variableNames
                interpreter = FSMTransitionConditionInterpreter(result.ast, memory)

                val parsedTransition = ParsedTransition(t, inputNames, interpreter)
                parsedTransitions.getOrPut(t) { parsedTransition }

                // ---- Outputs

                if (!isBlank(t.output) && !isBlank(getState(t.origStateId).output)) {
                    exception("antares.fsm.valueOutOfRangeInTransition.error", getState(t.origStateId).stateNumber)
                }

                if (StringUtils.isNotBlank(t.output)) {
                    for (pair in parseOutputList(t.output, "antares.fsm.invalidTransitionOutput.error", getState(t.origStateId).stateNumber)) {
                        val (name, value) = pair
                        if (value > BitWidth.of(MAX_OUTPUT_BITS).maxValue.toInt()) {
                            exception("antares.fsm.valueOutOfRangeInState.error", getState(t.origStateId).stateNumber, getState(t.destinationStateId).stateNumber)
                        }

                        val bitWidth = BitWidth.smallest(value.toULong())
                            ?: exception("antares.fsm.valueOutOfRangeInTransition.error", getState(t.origStateId).stateNumber, getState(t.destinationStateId).stateNumber)
                        val bitValue = Word.of(bitWidth, value.toULong())

                        for (i in bitWidth.width - 1 downTo 0) {
                            val effName = if (bitWidth == BitWidth.BW_1) {
                                name
                            } else {
                                "$name$i"
                            }
                            parsedTransition.outputs.add(ParsedOutput(effName, bitValue.bits[i].isSet))
                        }
                    }
                }
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

    private fun parseStateOutputs(): MutableMap<FSMState, MutableList<ParsedStateOutput>> {
        val parsedStateOutputs = mutableMapOf<FSMState, MutableList<ParsedStateOutput>>()
        for (state in states) {
            if (StringUtils.isNotBlank(state.output)) {
                for (pair in parseOutputList(state.output, "antares.fsm.invalidStateOutput.error", state.stateNumber)) {
                    val (outputName, outputValue) = pair

                    if (outputValue > BitWidth.of(MAX_OUTPUT_BITS).maxValue.toInt()) {
                        exception("antares.fsm.valueOutOfRangeInState.error", state.stateNumber)
                    }

                    val bitWidth = BitWidth.smallest(outputValue.toULong())
                        ?: exception("antares.fsm.valueOutOfRangeInState.error", state.stateNumber)
                    val bitValue = Word.of(bitWidth, outputValue.toULong())

                    for (i in bitWidth.width - 1 downTo 0) {
                        val name = if (bitWidth == BitWidth.BW_1) {
                            outputName
                        } else {
                            "$outputName$i"
                        }
                        val value = bitValue.bits[i].isSet
                        parsedStateOutputs.getOrPut(state) { mutableListOf() }
                            .add(ParsedStateOutput(state, ParsedOutput(name, value)))
                    }
                }
            }
        }
        return parsedStateOutputs
    }

    private fun parseOutputList(output: String, errorKey: String, vararg params: Any): List<Pair<String, Int>> = output
        .split(",")
        .map { parseOutput(it, errorKey, params) }
        .toList()

    private fun parseOutput(output: String, errorKey: String, vararg params: Any): Pair<String, Int> {
        val outputName: String?
        val outputValue: Int?
        val terms = output.trim().split("=")
        try {
            when (terms.size) {
                1 -> {
                    outputName = null
                    outputValue = parseOutputValue(terms[0])
                }

                2 -> {
                    outputName = terms[0]
                    outputValue = parseOutputValue(terms[1])
                }

                else -> {
                    exception(errorKey, params)
                }
            }
            return Pair(StringUtils.orElse(outputName, DEF_OUTPUT_NAME), outputValue)
        } catch (x: NumberFormatException) {
            exception(errorKey, params)
        }
    }

    private fun parseOutputValue(value: String): Int {
        val uppercaseValue = value.trim().uppercase()
        return if (uppercaseValue.startsWith("0X")) {
            uppercaseValue.substring(2).toInt(16)
        } else if (uppercaseValue.startsWith("0B")) {
            uppercaseValue.substring(2).toInt(2)
        } else {
            uppercaseValue.toInt()
        }
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
                    try {
                        val parsedTransition = parsedTransitions[transition]
                        val match = parsedTransition?.match() ?: false

                        // -- If more than one match: Exception
                        if (match) {
                            if (matchingTransition != null) {
                                exception("antares.fsm.multiTransitionMatch.error", state.stateNumber)
                            }
                            matchingTransition = transition
                        }
                    } catch (e: RuntimeError) {
                        exception("antares.fsm.invalidTransitionCondition.error", state.stateNumber, e.message ?: "Error")
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

        // Set fallback default value
        for (column in truthTable.columnCount - outputSignalNames.size  until truthTable.columnCount) {
            truthTable.setValue(row, column, Bit.False)
        }

        // Caution: The "to" argument intentionally uses the "from" state, because the output of the current row of the
        // truth table represents the output produced in the CURRENT state (which is the "from" state)

        val stateOutputs = parsedStateOutputs[from]
        if (stateOutputs != null) {
            stateOutputs.forEach { pso ->
                val transitionOutput =
                    transition?.let { t -> parsedTransitions[t] }?.outputs?.firstOrNull { it.name == pso.output.name }
                writeOutputSignal(truthTable, row, from, pso, to, transitionOutput)
            }
        } else {
            val transitionsOutputs = parsedTransitions[transition]?.outputs
            if (transitionsOutputs?.isNotEmpty() == true) {
                transitionsOutputs.forEach {
                    writeOutputSignal(truthTable, row, from, null, to, it)
                }
            } else {
                writeOutputSignal(truthTable, row, from, null, to, null)
            }
        }
    }

    private fun writeDestinationStateNumber(truthTable: TruthTable, row: Int, stateNumber: Int) {
        val stateNumberBits = DigitalSignalFactory.of(BitWidth.of(stateBits), stateNumber.toLong()).bits
        val minColumn = truthTable.columnCount - outputSignalNames.size - 1
        stateNumberBits.forEachIndexed { index, bit ->
            truthTable.setValue(row, minColumn - index, bit)
        }
    }

    private fun writeOutputSignal(truthTable: TruthTable, row: Int, from: FSMState, fromOutput: ParsedStateOutput?,
        to: FSMState, transitionOutput: ParsedOutput?
    ) {
        var fromStateColumnId: Int? = null
        var fromStateSignal: Bit? = null
        if (fromOutput != null) {
            fromStateColumnId = truthTable.columnCount - outputSignalNames.indexOf(fromOutput.output.name) - 1
            fromStateSignal = Bit.of(fromOutput.output.value)
        }
        var fromTransitionColumnId: Int? = null
        var fromTransitionSignal: Bit? = null
        if (transitionOutput != null) {
            fromTransitionColumnId = truthTable.inputColumnCount + truthTable.stateColumnCount + outputSignalNames.indexOf(transitionOutput.name)
            fromTransitionSignal = Bit.of(transitionOutput.value)
        }

        if (fromStateColumnId == fromTransitionColumnId) {
            if (fromStateSignal != fromTransitionSignal) {
                exception("antares.fsm.outputSignalConflict.error", from.stateNumber, to.stateNumber)
            }
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
        val outputs: MutableList<ParsedOutput> = mutableListOf()
    ) {

        /**
         * Returns `true` if this [ParsedTransition] is triggered by the current values
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
        val output: ParsedOutput
    )

    data class ParsedOutput(
        val name: String,
        val value: Boolean
    )
}