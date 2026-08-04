package io.antarescircuit.antares.ai

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
					validateAdd(op, position, declared, usedPortNames, errors)?.let { operations.add(it) }

				AiOperationDto.OP_CONNECT ->
					validateConnect(op, position, declared, context, occupiedInputs, errors)?.let { operations.add(it) }

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

		val inputCount = if (type.configurableInputCount) {
			val requested = op.inputs ?: type.defaultInputCount
			if (requested < AiComponentType.MIN_GATE_INPUTS || requested > AiComponentType.MAX_GATE_INPUTS) {
				errors.add("Operation $position requests $requested inputs for '${type.id}', but only ${AiComponentType.MIN_GATE_INPUTS}..${AiComponentType.MAX_GATE_INPUTS} are supported.")
				return null
			}
			requested
		} else {
			type.defaultInputCount
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
			inputCount = inputCount,
			x = op.x,
			y = op.y,
			value = op.value ?: 0L
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
		return Endpoint(declaration.ref, declaration.inputCount, declaration.type.outputCount)
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
}
