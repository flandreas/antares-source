package io.antarescircuit.antares.ai

import io.antarescircuit.antares.model.Logic
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValueParser
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.antares.model.signal.BitWidth

/**
 * Turns the untrusted [AiPlanDto] wire format into a typed [AiValidatedPlan].
 *
 * This is the single trust boundary of the AI feature: anything that is not explicitly allowed
 * here never reaches the circuit. Validation is intentionally pure so that it can be unit-tested
 * without a running editor; the live circuit is represented by an [AiCircuitContext] snapshot.
 */
object AiPlanValidator {

	/** Upper bound of operations accepted in a single plan.*/
	const val MAX_OPERATIONS = 400

	/** Coordinates outside this range are rejected as implausible.*/
	private const val MAX_COORDINATE = 100_000

	/** Reported errors are truncated so that a completely broken plan doesn't flood the UI.*/
	private const val MAX_REPORTED_ERRORS = 10

	data class Result(
		val plan: AiValidatedPlan?,
		val errors: List<String> = emptyList()
	) {
		val isValid: Boolean get() = plan != null
	}

	fun validate(dto: AiPlanDto, context: AiCircuitContext): Result {
		val errors = mutableListOf<String>()
		val operations = mutableListOf<AiOperation>()

		/** The components created by the plan so far, mapped to their validated declaration.*/
		val declared = mutableMapOf<String, AiOperation.AddComponent>()

		/** Inputs occupied so far, either already in the circuit or by an earlier operation of the plan.*/
		val occupiedInputs = mutableSetOf<Pair<String, Int>>()
		context.components.forEach { component ->
			component.connectedInputs.forEach { occupiedInputs.add(component.ref to it) }
		}

		val usedPortNames = context.portNames().toMutableSet()

		if (dto.operations.size > MAX_OPERATIONS) {
			return Result(null, listOf("The plan contains ${dto.operations.size} operations, but at most $MAX_OPERATIONS are allowed."))
		}

		dto.operations.forEachIndexed { index, op ->
			val position = index + 1
			when (op.op?.lowercase()?.trim()) {
				AiOperationDto.OP_ADD_COMPONENT ->
					validateAdd(op, position, declared, usedPortNames, context, errors)?.let { operations.add(it) }

				AiOperationDto.OP_CONNECT ->
					validateConnect(op, position, declared, context, occupiedInputs, errors)?.let { operations.add(it) }

				AiOperationDto.OP_SET_BIT_WIDTH ->
					validateSetBitWidth(op, position, declared, context, errors)?.let { operations.add(it) }

				AiOperationDto.OP_DELETE_COMPONENT ->
					validateDelete(op, position, context, errors)?.let { operations.add(it) }

				AiOperationDto.OP_CLEAR_CIRCUIT -> {
					// Clearing removes everything, including components added earlier in this plan,
					// so occupied inputs, port names and plan-local declarations start over.
					occupiedInputs.clear()
					usedPortNames.clear()
					declared.clear()
					operations.add(AiOperation.ClearCircuit)
				}

				null -> errors.add("Operation $position has no 'op' field.")

				else -> errors.add("Operation $position uses the unsupported operation '${op.op}'. Allowed: ${AiOperationDto.ops.joinToString(", ")}.")
			}
		}

		if (errors.isNotEmpty()) {
			return Result(null, errors.take(MAX_REPORTED_ERRORS))
		}

		return Result(AiValidatedPlan(reply = dto.reply?.trim().orEmpty(), operations = operations))
	}

	private fun validateAdd(
		op: AiOperationDto,
		position: Int,
		declared: MutableMap<String, AiOperation.AddComponent>,
		usedPortNames: MutableSet<String>,
		context: AiCircuitContext,
		errors: MutableList<String>
	): AiOperation.AddComponent? {
		val id = op.id?.trim()
		if (id.isNullOrEmpty()) {
			errors.add("Operation $position ('add_component') has no 'id'.")
			return null
		}
		if (id.startsWith(AiRef.EXISTING_PREFIX)) {
			errors.add("Operation $position declares the id '$id'. New components must not start with '${AiRef.EXISTING_PREFIX}'.")
			return null
		}
		if (declared.containsKey(id)) {
			errors.add("Operation $position re-uses the id '$id', which was already declared.")
			return null
		}

		val type = op.type?.let { AiComponentType.withId(it) }
		if (type == null) {
			errors.add("Operation $position uses the unsupported component type '${op.type}'. Allowed: ${AiComponentType.ids.joinToString(", ")}.")
			return null
		}

		val subcircuit = if (type == AiComponentType.Subcircuit) {
			val uuid = op.metaGraphUuid?.trim()
			if (uuid.isNullOrEmpty()) {
				errors.add("Operation $position ('add_component' subcircuit) has no 'metaGraphUuid'.")
				return null
			}
			context.availableSubcircuits.find { it.uuid == uuid } ?: run {
				errors.add("Operation $position references MetaGraph '$uuid', which is not an available subcircuit in the current project or its included libraries.")
				return null
			}
		} else {
			if (op.metaGraphUuid != null) {
				errors.add("Operation $position sets 'metaGraphUuid' for '${type.id}', but only subcircuits use it.")
				return null
			}
			null
		}

		if (type == AiComponentType.Subcircuit && op.inputs != null) {
			errors.add("Operation $position sets 'inputs' for a subcircuit, whose ports are defined by its MetaGraph.")
			return null
		}

		val inputCount = if (subcircuit != null) {
			subcircuit.inputPorts.size
		} else if (type.configurableInputCount) {
			val requested = op.inputs ?: type.defaultInputCount
			if (requested < AiComponentType.MIN_GATE_INPUTS || requested > AiComponentType.MAX_GATE_INPUTS) {
				errors.add("Operation $position requests $requested inputs for '${type.id}', but only ${AiComponentType.MIN_GATE_INPUTS}..${AiComponentType.MAX_GATE_INPUTS} are supported.")
				return null
			}
			requested
		} else {
			type.defaultInputCount
		}

		if (op.bitWidth != null && !supportsBitWidth(type)) {
			errors.add("Operation $position sets 'bitWidth' for '${type.id}', but only circuit inputs, circuit outputs, constants, bus adapters, and logic gates support it.")
			return null
		}
		val bitWidth = op.bitWidth ?: if (type.isBusAdapter()) 8 else 1
		if (bitWidth !in 1..BitWidth.MAX) {
			errors.add("Operation $position requests bit width $bitWidth for '${type.id}', but only 1..${BitWidth.MAX} are supported.")
			return null
		}
		val branchCount = if (type.isBusAdapter()) {
			val requested = op.branchCount ?: 4
			if (requested !in 2..bitWidth || bitWidth % requested != 0) {
				errors.add("Operation $position cannot divide a $bitWidth-bit bus into $requested equal branches. 'branchCount' must be at least 2 and divide 'bitWidth' evenly.")
				return null
			}
			requested
		} else {
			if (op.branchCount != null) {
				errors.add("Operation $position sets 'branchCount' for '${type.id}', but only splitters and concentrators support it.")
				return null
			}
			null
		}
		val enableLogic = if (type == AiComponentType.TriStateBuffer) {
			when (op.enableLogic?.trim()?.lowercase() ?: Logic.POSITIVE.customName) {
				Logic.POSITIVE.customName -> Logic.POSITIVE
				Logic.NEGATIVE.customName -> Logic.NEGATIVE
				else -> {
					errors.add("Operation $position uses enable logic '${op.enableLogic}', but only 'positive' and 'negative' are supported.")
					return null
				}
			}
		} else {
			if (op.enableLogic != null) {
				errors.add("Operation $position sets 'enableLogic' for '${type.id}', but only tri-state buffers support it.")
				return null
			}
			null
		}
		val periodOrFrequency = if (type == AiComponentType.Clock) {
			op.periodOrFrequency?.let { raw ->
				try {
					MagnitudeValueParser.parseWithUnits(raw, SIUnit.Second, SIUnit.Hertz)
				} catch (_: IllegalArgumentException) {
					errors.add("Operation $position uses period or frequency '$raw'. Expected a positive time or frequency such as '10 ns', '500 ms', or '1 MHz'.")
					return null
				}
			}
		} else {
			if (op.periodOrFrequency != null) {
				errors.add("Operation $position sets 'periodOrFrequency' for '${type.id}', but only clocks support it.")
				return null
			}
			null
		}

		if (!isValidCoordinate(op.x) || !isValidCoordinate(op.y)) {
			errors.add("Operation $position uses a coordinate outside the supported range of +/-$MAX_COORDINATE.")
			return null
		}

		val name = op.name?.trim()?.ifEmpty { null }
		if (name != null && (type == AiComponentType.Input || type == AiComponentType.Output)) {
			if (!usedPortNames.add(name)) {
				errors.add("Operation $position names the circuit port '$name', but that name is already used. Port names must be unique.")
				return null
			}
		}

		return AiOperation.AddComponent(
			ref = AiRef.New(id),
			type = type,
			name = name,
			inputCount = if (type == AiComponentType.Concentrator) branchCount!! else inputCount,
			x = op.x,
			y = op.y,
			value = op.value ?: 0L,
			bitWidth = bitWidth,
			branchCount = branchCount,
			enableLogic = enableLogic,
			periodOrFrequency = periodOrFrequency,
			metaGraphUuid = subcircuit?.uuid,
			outputCount = subcircuit?.outputPorts?.size
				?: if (type == AiComponentType.Splitter) branchCount!! else type.outputCount
		).also { declared[id] = it }
	}

	private fun validateConnect(
		op: AiOperationDto,
		position: Int,
		declared: Map<String, AiOperation.AddComponent>,
		context: AiCircuitContext,
		occupiedInputs: MutableSet<Pair<String, Int>>,
		errors: MutableList<String>
	): AiOperation.Connect? {
		val from = resolve(op.from, position, "from", declared, context, errors) ?: return null
		val to = resolve(op.to, position, "to", declared, context, errors) ?: return null

		if (from.ref == to.ref) {
			errors.add("Operation $position connects '${op.from}' with itself.")
			return null
		}

		val fromPort = op.fromPort ?: 1
		if (fromPort < 1 || fromPort > from.outputCount) {
			errors.add("Operation $position uses output $fromPort of '${op.from}', which has ${from.outputCount} output(s).")
			return null
		}

		val toPort = op.toPort ?: 1
		if (toPort < 1 || toPort > to.inputCount) {
			errors.add("Operation $position uses input $toPort of '${op.to}', which has ${to.inputCount} input(s).")
			return null
		}

		if (!occupiedInputs.add(to.ref.toString() to toPort)) {
			errors.add("Operation $position connects to input $toPort of '${op.to}', which is already connected. Every input accepts a single wire.")
			return null
		}

		return AiOperation.Connect(from = from.ref, fromPort = fromPort, to = to.ref, toPort = toPort)
	}

	private fun validateDelete(
		op: AiOperationDto,
		position: Int,
		context: AiCircuitContext,
		errors: MutableList<String>
	): AiOperation.DeleteComponent? {
		val target = op.target ?: op.id
		val existing = parseExistingRef(target)
		if (existing == null) {
			errors.add("Operation $position ('delete_component') must target an existing component such as '${AiRef.EXISTING_PREFIX}12'.")
			return null
		}
		if (context.component(existing.componentId) == null) {
			errors.add("Operation $position deletes '$target', which is not part of the current circuit.")
			return null
		}
		return AiOperation.DeleteComponent(existing)
	}

	private fun validateSetBitWidth(
		op: AiOperationDto,
		position: Int,
		declared: Map<String, AiOperation.AddComponent>,
		context: AiCircuitContext,
		errors: MutableList<String>
	): AiOperation.SetBitWidth? {
		if (op.target.isNullOrBlank()) {
			errors.add("Operation $position ('set_bit_width') has no 'target'.")
			return null
		}
		val target = resolve(op.target, position, "target", declared, context, errors) ?: return null
		val supportsBitWidth = when (val ref = target.ref) {
			is AiRef.Existing -> context.component(ref.componentId)?.let {
				it.bitWidth != null && it.type != AiComponentType.Splitter.id && it.type != AiComponentType.Concentrator.id
			} == true
			is AiRef.New -> declared[ref.id]?.let { supportsMutableBitWidth(it.type) } == true
		}
		if (!supportsBitWidth) {
			errors.add("Operation $position cannot change the bit width of '${op.target}'. Only circuit inputs, circuit outputs, constants, and logic gates support it.")
			return null
		}
		val bitWidth = op.bitWidth
		if (bitWidth == null) {
			errors.add("Operation $position ('set_bit_width') has no 'bitWidth'.")
			return null
		}
		if (bitWidth !in 1..BitWidth.MAX) {
			errors.add("Operation $position requests bit width $bitWidth, but only 1..${BitWidth.MAX} are supported.")
			return null
		}
		return AiOperation.SetBitWidth(target.ref, bitWidth)
	}

	/** The resolved endpoint of a connection together with the port counts needed to check the port indices. */
	private data class Endpoint(val ref: AiRef, val inputCount: Int, val outputCount: Int)

	private fun resolve(
		raw: String?,
		position: Int,
		field: String,
		declared: Map<String, AiOperation.AddComponent>,
		context: AiCircuitContext,
		errors: MutableList<String>
	): Endpoint? {
		val ref = raw?.trim()
		if (ref.isNullOrEmpty()) {
			errors.add("Operation $position ('connect') has no '$field' reference.")
			return null
		}

		parseExistingRef(ref)?.let { existing ->
			val component = context.component(existing.componentId)
			if (component == null) {
				errors.add("Operation $position references '$ref', which is not part of the current circuit.")
				return null
			}
			return Endpoint(existing, component.inputCount, component.outputCount)
		}

		val declaration = declared[ref]
		if (declaration == null) {
			errors.add("Operation $position references '$ref', which was neither created earlier in this plan nor exists in the circuit.")
			return null
		}
		return Endpoint(declaration.ref, declaration.inputCount, declaration.outputCount)
	}

	private fun parseExistingRef(raw: String?): AiRef.Existing? {
		val ref = raw?.trim() ?: return null
		if (!ref.startsWith(AiRef.EXISTING_PREFIX)) {
			return null
		}
		val id = ref.removePrefix(AiRef.EXISTING_PREFIX).toIntOrNull() ?: return null
		return AiRef.Existing(id)
	}

	private fun isValidCoordinate(value: Int?): Boolean =
		value == null || (value >= -MAX_COORDINATE && value <= MAX_COORDINATE)

	private fun supportsBitWidth(type: AiComponentType): Boolean =
		type == AiComponentType.Input
			|| type == AiComponentType.Output
			|| type == AiComponentType.Constant
			|| type == AiComponentType.Splitter
			|| type == AiComponentType.Concentrator
			|| type == AiComponentType.TriStateBuffer
			|| type == AiComponentType.Not
			|| type == AiComponentType.Buffer
			|| type.configurableInputCount

	private fun supportsMutableBitWidth(type: AiComponentType): Boolean =
		!type.isBusAdapter() && supportsBitWidth(type)

	private fun AiComponentType.isBusAdapter(): Boolean =
		this == AiComponentType.Splitter || this == AiComponentType.Concentrator
}
