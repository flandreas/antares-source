package ch.scorpion.antares.model.fsm

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.StringUtils.isBlank
import ch.scorpion.jabbah.base.Translations
import kotlin.math.max

class FSMException(message: String) : Exception(message)

/**
 * Creates a [TruthTable] representing the logic of an [FSMDrawing].
 */
class FSMTruthTableCreator(
    private val fsm: FSMDrawing,
    private val stateVar: String = DEF_STATE_VAR
) {

    companion object {
        const val DEF_STATE_VAR = "Z"
    }

    private val states: Collection<FSMState> = fsm.states

    private val transitions: Collection<FSMTransition> = fsm.transitions

    private val stateBits: Int = calculateStateVariableBits(states)

    private val initState: FSMState = getInitState(states)

    /** Maps [FSMState]s to their outgoing [ParsedTransition]s. */
    private val parsedTransitions = mutableMapOf<FSMState, MutableList<ParsedTransition>>()

    private val parsedStateOutputs = mutableMapOf<FSMState, MutableList<ParsedStateOutput>>()

    /**
     * Creates a [TruthTable] representing the logic of [fsm].
     * @throws FSMException if validation of [fsm] failed
     */
    fun create(): TruthTable {
        val inputColumNames = mutableListOf<String>()
        addStateVariableInputColumns(inputColumNames)

        val outputColumNames = mutableListOf<String>()
        addStateVariableOutputColumns(outputColumNames)

        parseTransitions()
        inputColumNames.addAll(
            parsedTransitions.values
                .flatten()
                .filter { it.inputName != null }
                .map { it.inputName as String }
                .toSet())
        outputColumNames.addAll(
            parsedTransitions.values
                .flatten()
                .filter { it.outputName != null }
                .map { it.outputName as String }
                .toSet())

        parseStateOutputs()
        outputColumNames.addAll(
            parsedStateOutputs.values
                .flatten()
                .filter { it.outputName != null }
                .map { it.outputName as String }
                .toSet())

        val truthTable = TruthTable(
            fsm.name.getTranslation(),
            inputColumNames,
            outputColumNames
        )

        for (i in 0 until outputColumNames.size) {
            // TruthTable uses Bit.Error (X) for "Any value"
            truthTable.setColumnValue(inputColumNames.size + i, Bit.Error)
        }

        return truthTable
    }

    private fun calculateStateVariableBits(states: Collection<FSMState>): Int {
        val numbers = setOf<Int>()
        var maxNumber = 0
        for (state in states) {
            maxNumber = max(maxNumber, state.stateNumber)
            if (numbers.contains(state.stateNumber)) {
                exception("antares.fsm.duplicateStateNumber.error", state.stateNumber)
            }
        }
        return BitOperation.bitCount(maxNumber.toULong())
    }

    private fun getInitState(states: Collection<FSMState>): FSMState {
        val initStates = states.filter { it.stateType == FSMStateType.Initial }
        if (initStates.size != 1) {
            exception("antares.fsm.notOneInitState.error", initStates.size)
        }
        return initStates.first()
    }

    private fun addStateVariableInputColumns(inputColumnNames: MutableList<String>) {
        for (i in stateBits - 1 downTo 0) {
            inputColumnNames.add("${stateVar}_$i^n")
        }
    }

    private fun addStateVariableOutputColumns(outputColumnNames: MutableList<String>) {
        for (i in stateBits - 1 downTo 0) {
            outputColumnNames.add("${stateVar}_$i^(n+1)")
        }
    }

    private fun parseTransitions() {
        for (t in transitions) {
            try {
                // ---- Conditions

                if (isBlank(t.condition)) {
                    exception("antares.fsm.emptyTransitionCondition.error", getState(t.origStateId).stateNumber)
                }
                var inputName: String? = null
                var inputValue = 0
                var terms = t.condition.trim().split("=")
                when (terms.size) {
                    1 -> {
                        inputName = null
                        inputValue = terms[0].toInt()
                    }
                    2 -> {
                        inputName = terms[0]
                        inputValue = terms[1].toInt()
                    }
                    else -> {
                        exception("antares.fsm.invalidTransitionCondition.error", getState(t.origStateId).stateNumber)
                    }
                }
                if (inputName == null && parsedTransitions.values.any { it.any { pt -> pt.inputName != null }  }) {
                    exception("antares.fsm.inconsistentTransitionConditionNaming.error")
                }
                if (inputName != null && parsedTransitions.values.any { it.any { pt -> pt.inputName == null }  }) {
                    exception("antares.fsm.inconsistentTransitionConditionNaming.error")
                }

                // ---- Outputs

                if (isBlank(t.output) && isBlank(getState(t.destinationStateId).output)) {
                    exception("antares.fsm.missingOutputInTransition.error", getState(t.destinationStateId).stateNumber)
                }
                if (!isBlank(t.output) && !isBlank(getState(t.destinationStateId).output)) {
                    exception("antares.fsm.outputInTransitionAndState.error", getState(t.destinationStateId).stateNumber)
                }
                var outputName: String? = null
                var outputValue = 0
                if (StringUtils.isNotBlank(t.output)) {
                    terms = t.output.trim().split("=")
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
                if (outputName == null && parsedTransitions.values.any { it.any { pt -> pt.outputName != null }  }) {
                    exception("antares.fsm.inconsistentTransitionOutputNaming.error")
                }
                if (outputName != null && parsedTransitions.values.any { it.any { pt -> pt.outputName == null }  }) {
                    exception("antares.fsm.inconsistentTransitionOutputNaming.error")
                }

                parsedTransitions.getOrPut(getState(t.origStateId)) { mutableListOf() }.add(
                    ParsedTransition(t, inputName, inputValue, outputName, outputValue))

            } catch (e: NumberFormatException) {
                exception("antares.fsm.invalidTransitionConditionValue.error", getState(t.origStateId).stateNumber)
            } catch (e: Throwable) {
                exception("antares.fsm.generalTransitionError.error", getState(t.origStateId).stateNumber)
            }
        }
    }

    private fun parseStateOutputs() {
        for (state in states) {
            var outputName: String? = null
            var outputValue = 0
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
                parsedStateOutputs.getOrPut(state) { mutableListOf() }.add(
                    ParsedStateOutput(state, outputName, outputValue)
                )
            }
        }
    }

    private fun exception(key: String, vararg params: Any) {
        throw FSMException(Translations.getString(key, *params))
    }

    private fun getState(id: Int): FSMState = states.find { it.id == id }!!

    data class ParsedTransition(
        val transition: FSMTransition,
        val inputName: String? = null,
        val inputValue: Int,
        val outputName: String? = null,
        val outputValue: Int? = null
    )

    data class ParsedStateOutput(
        val state: FSMState,
        val outputName: String? = null,
        val outputValue: Int? = null
    )
}