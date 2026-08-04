package io.antarescircuit.antares.ai

import kotlinx.serialization.Serializable

/**
 * The typed, validated operation contract between the AI assistant and the Antares circuit editor.
 *
 * The assistant is never allowed to emit code or to touch files. It may only return a plan
 * consisting of the allow-listed operations defined in this file. Everything that arrives from
 * the model is first parsed into the [AiPlanDto] wire format, then checked by [AiPlanValidator],
 * which is the only place that can produce the typed [AiValidatedPlan] applied by [AiPlanExecutor].
 */

/** The component types the assistant is allowed to create. All other types are rejected. */
enum class AiComponentType(
	val id: String,
	/** `true` if the number of inputs can be chosen by the assistant.*/
	val configurableInputCount: Boolean,
	val defaultInputCount: Int,
	val outputCount: Int
) {
	/** A circuit input port (feeds a signal into the circuit, hence one output).*/
	Input("input", false, 0, 1),

	/** A circuit output port (consumes a signal, hence one input).*/
	Output("output", false, 1, 0),

	/** An interactive toggle switch.*/
	Switch("switch", false, 0, 1),

	/** An LED for making a signal visible.*/
	Led("led", false, 1, 0),

	/** A constant signal source.*/
	Constant("constant", false, 0, 1),

	Not("not", false, 1, 1),
	Buffer("buffer", false, 1, 1),
	And("and", true, 2, 1),
	Or("or", true, 2, 1),
	Nand("nand", true, 2, 1),
	Nor("nor", true, 2, 1),
	Xor("xor", true, 2, 1),
	Xnor("xnor", true, 2, 1);

	companion object {

		/** The smallest number of inputs of a gate with a configurable input count.*/
		const val MIN_GATE_INPUTS = 2

		/** The largest number of inputs of a gate with a configurable input count (limit of `PortCount`).*/
		const val MAX_GATE_INPUTS = 16

		fun withId(id: String): AiComponentType? = entries.find { it.id == id.lowercase().trim() }

		val ids: List<String> get() = entries.map { it.id }
	}
}

/** Identifies the target of an operation: either a component created by the plan, or an existing one. */
sealed interface AiRef {

	/** A component created earlier in the same plan, identified by the plan-local `id`.*/
	data class New(val id: String) : AiRef {
		override fun toString(): String = id
	}

	/** A component that already exists in the circuit, identified by its editor component ID.*/
	data class Existing(val componentId: Int) : AiRef {
		override fun toString(): String = "$EXISTING_PREFIX$componentId"
	}

	companion object {
		/** Prefix marking a reference to an already existing component, e.g. `#42`.*/
		const val EXISTING_PREFIX = "#"
	}
}

/** The allow-listed operations. Nothing outside this hierarchy can ever be applied. */
sealed interface AiOperation {

	data class AddComponent(
		val ref: AiRef.New,
		val type: AiComponentType,
		val name: String?,
		val inputCount: Int,
		val x: Int?,
		val y: Int?,
		val value: Long
	) : AiOperation

	data class Connect(
		val from: AiRef,
		val fromPort: Int,
		val to: AiRef,
		val toPort: Int
	) : AiOperation

	data class DeleteComponent(val target: AiRef.Existing) : AiOperation

	/** Removes every component of the currently open circuit. Requires explicit user confirmation. */
	data object ClearCircuit : AiOperation
}

/**
 * A plan that passed [AiPlanValidator] and may therefore be applied to a circuit.
 * @property destructive `true` if the plan removes anything from the existing circuit,
 * in which case the UI must ask the user for confirmation before applying it
 */
data class AiValidatedPlan(
	val reply: String,
	val operations: List<AiOperation>
) {
	val destructive: Boolean get() = operations.any {
		it is AiOperation.ClearCircuit || it is AiOperation.DeleteComponent
	}
}

/** ---- Wire format ------------------------------------------------------------------------- */

/**
 * The wire format of a model response. Deliberately flat and fully optional so that a malformed
 * response yields precise validation messages instead of a serialization exception.
 */
@Serializable
data class AiPlanDto(
	val reply: String? = null,
	val operations: List<AiOperationDto> = emptyList()
)

@Serializable
data class AiOperationDto(
	val op: String? = null,
	val id: String? = null,
	val type: String? = null,
	val name: String? = null,
	val inputs: Int? = null,
	val x: Int? = null,
	val y: Int? = null,
	val value: Long? = null,
	val from: String? = null,
	val fromPort: Int? = null,
	val to: String? = null,
	val toPort: Int? = null,
	val target: String? = null
) {
	companion object {
		const val OP_ADD_COMPONENT = "add_component"
		const val OP_CONNECT = "connect"
		const val OP_DELETE_COMPONENT = "delete_component"
		const val OP_CLEAR_CIRCUIT = "clear_circuit"

		val ops = listOf(OP_ADD_COMPONENT, OP_CONNECT, OP_DELETE_COMPONENT, OP_CLEAR_CIRCUIT)
	}
}
