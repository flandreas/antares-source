package io.antarescircuit.antares.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiPlanValidatorTest {

	private val emptyCircuit = AiCircuitContext(circuitName = "Test", circuitId = "c1")
	private val circuitWithSubcircuit = emptyCircuit.copy(availableSubcircuits = listOf(
		AiAvailableSubcircuit(
			uuid = "11111111-1111-1111-1111-111111111111",
			name = "Byte inverter",
			libraryName = "Test library",
			inputPorts = listOf(AiSubcircuitPort(1, "A", 8)),
			outputPorts = listOf(AiSubcircuitPort(1, "Y", 8)),
		)))

	/** A circuit with an input (#1), a 2-input AND gate (#2, first input already wired) and an output (#3). */
	private val populatedCircuit = AiCircuitContext(
		circuitName = "Test",
		circuitId = "c1",
		components = listOf(
			AiCircuitComponent(ref = "#1", type = "input", name = "A", inputCount = 0, outputCount = 1, bitWidth = 1),
			AiCircuitComponent(ref = "#2", type = "AND", inputCount = 2, outputCount = 1, connectedInputs = listOf(1), bitWidth = 1),
			AiCircuitComponent(ref = "#3", type = "output", name = "Y", inputCount = 1, outputCount = 0, bitWidth = 1)
		),
		connections = listOf(AiCircuitConnection(from = "#1", fromPort = 1, to = "#2", toPort = 1))
	)

	private fun plan(vararg operations: AiOperationDto) = AiPlanDto(reply = "ok", operations = operations.toList())

	private fun add(id: String, type: String, inputs: Int? = null, name: String? = null, bitWidth: Int? = null) =
		AiOperationDto(op = AiOperationDto.OP_ADD_COMPONENT, id = id, type = type, inputs = inputs, name = name, bitWidth = bitWidth)

	private fun connect(from: String, to: String, fromPort: Int? = null, toPort: Int? = null) =
		AiOperationDto(op = AiOperationDto.OP_CONNECT, from = from, to = to, fromPort = fromPort, toPort = toPort)

	@Test
	fun shouldAcceptASimpleAndCircuit() {
		val result = AiPlanValidator.validate(
			plan(
				add("a", "input", name = "A"),
				add("b", "input", name = "B"),
				add("g", "and", inputs = 2),
				add("y", "output", name = "Y"),
				connect("a", "g", toPort = 1),
				connect("b", "g", toPort = 2),
				connect("g", "y")
			),
			emptyCircuit)

		assertTrue(result.isValid, "expected a valid plan but got ${result.errors}")
		val plan = result.plan!!
		assertEquals(7, plan.operations.size)
		assertFalse(plan.destructive)

		val gate = plan.operations.filterIsInstance<AiOperation.AddComponent>().first { it.ref.id == "g" }
		assertEquals(AiComponentType.And, gate.type)
		assertEquals(2, gate.inputCount)

		val wire = assertIs<AiOperation.Connect>(plan.operations.last())
		assertEquals(AiRef.New("g"), wire.from)
		assertEquals(1, wire.fromPort)
		assertEquals(1, wire.toPort)
	}

	@Test
	fun shouldRejectUnknownOperation() {
		val result = AiPlanValidator.validate(
			plan(AiOperationDto(op = "run_script", name = "rm -rf /")),
			emptyCircuit)

		assertNull(result.plan)
		assertTrue(result.errors.single().contains("run_script"), result.errors.toString())
	}

	@Test
	fun shouldRejectUnknownComponentType() {
		val result = AiPlanValidator.validate(plan(add("m", "microprocessor")), emptyCircuit)

		assertNull(result.plan)
		assertTrue(result.errors.single().contains("microprocessor"), result.errors.toString())
	}

	@Test
	fun shouldAcceptAvailableSubcircuitAndUseItsDynamicPorts() {
		val uuid = circuitWithSubcircuit.availableSubcircuits.single().uuid
		val result = AiPlanValidator.validate(
			plan(
				AiOperationDto(
					op = AiOperationDto.OP_ADD_COMPONENT,
					id = "custom",
					type = "subcircuit",
					metaGraphUuid = uuid),
				add("y", "output", bitWidth = 8),
				connect("custom", "y"),
			),
			circuitWithSubcircuit)

		assertTrue(result.isValid, result.errors.toString())
		val operation = result.plan!!.operations.filterIsInstance<AiOperation.AddComponent>().first()
		assertEquals(uuid, operation.metaGraphUuid)
		assertEquals(1, operation.inputCount)
		assertEquals(1, operation.outputCount)
	}

	@Test
	fun shouldRejectUnavailableOrInvalidSubcircuitFields() {
		fun subcircuit(uuid: String? = circuitWithSubcircuit.availableSubcircuits.single().uuid, inputs: Int? = null, bitWidth: Int? = null) =
			AiOperationDto(
				op = AiOperationDto.OP_ADD_COMPONENT,
				id = "custom",
				type = "subcircuit",
				metaGraphUuid = uuid,
				inputs = inputs,
				bitWidth = bitWidth)

		assertNull(AiPlanValidator.validate(plan(subcircuit(uuid = null)), circuitWithSubcircuit).plan)
		assertNull(AiPlanValidator.validate(plan(subcircuit(uuid = "unknown")), circuitWithSubcircuit).plan)
		assertNull(AiPlanValidator.validate(plan(subcircuit(inputs = 2)), circuitWithSubcircuit).plan)
		assertNull(AiPlanValidator.validate(plan(subcircuit(bitWidth = 8)), circuitWithSubcircuit).plan)
	}

	@Test
	fun shouldRejectGateInputCountOutOfRange() {
		assertNull(AiPlanValidator.validate(plan(add("g", "and", inputs = 1)), emptyCircuit).plan)
		assertNull(AiPlanValidator.validate(plan(add("g", "and", inputs = 17)), emptyCircuit).plan)
		assertTrue(AiPlanValidator.validate(plan(add("g", "and", inputs = 16)), emptyCircuit).isValid)
	}

	@Test
	fun shouldAcceptBitWidthForCircuitPortsConstantsAndLogicGates() {
		val result = AiPlanValidator.validate(
			plan(
				add("a", "input", bitWidth = 8),
				add("c", "constant", bitWidth = 8),
				add("g", "and", bitWidth = 8),
				add("y", "output", bitWidth = 8),
			),
			emptyCircuit)

		assertTrue(result.isValid, result.errors.toString())
		assertEquals(listOf(8, 8, 8, 8), result.plan!!.operations.filterIsInstance<AiOperation.AddComponent>().map { it.bitWidth })
	}

	@Test
	fun shouldDefaultBitWidthToOne() {
		val result = AiPlanValidator.validate(plan(add("g", "not")), emptyCircuit)

		assertEquals(1, result.plan!!.operations.filterIsInstance<AiOperation.AddComponent>().single().bitWidth)
	}

	@Test
	fun shouldRejectUnsupportedBitWidthsAndComponents() {
		assertNull(AiPlanValidator.validate(plan(add("g", "and", bitWidth = 0)), emptyCircuit).plan)
		assertNull(AiPlanValidator.validate(plan(add("g", "and", bitWidth = 65)), emptyCircuit).plan)
		assertNull(AiPlanValidator.validate(plan(add("s", "switch", bitWidth = 8)), emptyCircuit).plan)
	}

	@Test
	fun shouldValidateSplitterBranchCountAndExposeItsOutputs() {
		val valid = AiPlanValidator.validate(
			plan(
				AiOperationDto(
					op = AiOperationDto.OP_ADD_COMPONENT,
					id = "split",
					type = "splitter",
					bitWidth = 8,
					branchCount = 4),
				add("y", "output", bitWidth = 2),
				connect("split", "y", fromPort = 4),
			),
			emptyCircuit)

		assertTrue(valid.isValid, valid.errors.toString())
		val splitter = valid.plan!!.operations.filterIsInstance<AiOperation.AddComponent>().first()
		assertEquals(1, splitter.inputCount)
		assertEquals(4, splitter.outputCount)
		assertEquals(4, splitter.branchCount)

		assertNull(AiPlanValidator.validate(plan(AiOperationDto(
			op = AiOperationDto.OP_ADD_COMPONENT,
			id = "split",
			type = "splitter",
			bitWidth = 8,
			branchCount = 3)), emptyCircuit).plan)
		assertNull(AiPlanValidator.validate(plan(add("g", "and").copy(branchCount = 2)), emptyCircuit).plan)
	}

	@Test
	fun shouldValidateConcentratorBranchCountAndExposeItsInputs() {
		val result = AiPlanValidator.validate(
			plan(
				add("a", "input", bitWidth = 2),
				AiOperationDto(
					op = AiOperationDto.OP_ADD_COMPONENT,
					id = "join",
					type = "concentrator",
					bitWidth = 8,
					branchCount = 4),
				connect("a", "join", toPort = 4),
			),
			emptyCircuit)

		assertTrue(result.isValid, result.errors.toString())
		val concentrator = result.plan!!.operations.filterIsInstance<AiOperation.AddComponent>().last()
		assertEquals(4, concentrator.inputCount)
		assertEquals(1, concentrator.outputCount)
		assertEquals(4, concentrator.branchCount)
	}

	@Test
	fun shouldValidateTriStateBufferEnableLogicAndPorts() {
		val result = AiPlanValidator.validate(
			plan(
				add("data", "input", bitWidth = 8),
				add("enable", "input"),
				add("buffer", "tri_state_buffer", bitWidth = 8).copy(enableLogic = "negative"),
				add("y", "output", bitWidth = 8),
				connect("data", "buffer", toPort = 1),
				connect("enable", "buffer", toPort = 2),
				connect("buffer", "y"),
			),
			emptyCircuit)

		assertTrue(result.isValid, result.errors.toString())
		val buffer = result.plan!!.operations.filterIsInstance<AiOperation.AddComponent>()[2]
		assertEquals(2, buffer.inputCount)
		assertEquals(1, buffer.outputCount)
		assertEquals(io.antarescircuit.antares.model.Logic.NEGATIVE, buffer.enableLogic)

		assertNull(AiPlanValidator.validate(
			plan(add("buffer", "tri_state_buffer").copy(enableLogic = "falling")), emptyCircuit).plan)
		assertNull(AiPlanValidator.validate(
			plan(add("g", "and").copy(enableLogic = "positive")), emptyCircuit).plan)
	}

	@Test
	fun shouldValidateClockPeriodOrFrequency() {
		val result = AiPlanValidator.validate(
			plan(add("clk", "clock", name = "CLK").copy(periodOrFrequency = "20 MHz")),
			emptyCircuit)

		assertTrue(result.isValid, result.errors.toString())
		val clock = result.plan!!.operations.filterIsInstance<AiOperation.AddComponent>().single()
		assertEquals(0, clock.inputCount)
		assertEquals(1, clock.outputCount)
		assertEquals("20 MHz", clock.periodOrFrequency.toString())

		assertNull(AiPlanValidator.validate(
			plan(add("clk", "clock").copy(periodOrFrequency = "fast")), emptyCircuit).plan)
		assertNull(AiPlanValidator.validate(
			plan(add("clk", "clock").copy(periodOrFrequency = "0 Hz")), emptyCircuit).plan)
		assertNull(AiPlanValidator.validate(
			plan(add("g", "and").copy(periodOrFrequency = "1 Hz")), emptyCircuit).plan)
	}

	@Test
	fun shouldAcceptChangingBitWidthOfExistingAndNewComponents() {
		val result = AiPlanValidator.validate(
			plan(
				AiOperationDto(op = AiOperationDto.OP_SET_BIT_WIDTH, target = "#1", bitWidth = 8),
				add("g", "not"),
				AiOperationDto(op = AiOperationDto.OP_SET_BIT_WIDTH, target = "g", bitWidth = 8),
				add("c", "constant"),
				AiOperationDto(op = AiOperationDto.OP_SET_BIT_WIDTH, target = "c", bitWidth = 8),
			),
			populatedCircuit)

		assertTrue(result.isValid, result.errors.toString())
		assertEquals(3, result.plan!!.operations.filterIsInstance<AiOperation.SetBitWidth>().size)
	}

	@Test
	fun shouldRejectChangingBitWidthWithoutValidTargetOrWidth() {
		assertNull(AiPlanValidator.validate(
			plan(AiOperationDto(op = AiOperationDto.OP_SET_BIT_WIDTH, target = "#1")), populatedCircuit).plan)
		assertNull(AiPlanValidator.validate(
			plan(AiOperationDto(op = AiOperationDto.OP_SET_BIT_WIDTH, target = "#1", bitWidth = 65)), populatedCircuit).plan)
		assertNull(AiPlanValidator.validate(
			plan(add("led", "led"), AiOperationDto(op = AiOperationDto.OP_SET_BIT_WIDTH, target = "led", bitWidth = 8)), emptyCircuit).plan)
	}

	@Test
	fun shouldRejectDuplicateIds() {
		val result = AiPlanValidator.validate(plan(add("g", "and"), add("g", "or")), emptyCircuit)

		assertNull(result.plan)
		assertTrue(result.errors.single().contains("'g'"), result.errors.toString())
	}

	@Test
	fun shouldRejectReferenceToUndeclaredComponent() {
		val result = AiPlanValidator.validate(plan(add("g", "and"), connect("ghost", "g")), emptyCircuit)

		assertNull(result.plan)
		assertTrue(result.errors.single().contains("ghost"), result.errors.toString())
	}

	@Test
	fun shouldRejectReferenceToComponentThatIsNotInTheCircuit() {
		val result = AiPlanValidator.validate(plan(add("g", "and"), connect("#99", "g")), populatedCircuit)

		assertNull(result.plan)
		assertTrue(result.errors.single().contains("#99"), result.errors.toString())
	}

	@Test
	fun shouldRejectPortIndexOutOfRange() {
		val outOfRange = AiPlanValidator.validate(
			plan(add("a", "input"), add("g", "and", inputs = 2), connect("a", "g", toPort = 3)),
			emptyCircuit)
		assertNull(outOfRange.plan)

		val noOutput = AiPlanValidator.validate(
			plan(add("y", "output"), add("g", "and"), connect("y", "g")),
			emptyCircuit)
		assertNull(noOutput.plan)
	}

	@Test
	fun shouldRejectTwoWiresOnTheSameInput() {
		val result = AiPlanValidator.validate(
			plan(add("a", "input"), add("b", "input"), add("y", "output"), connect("a", "y"), connect("b", "y")),
			emptyCircuit)

		assertNull(result.plan)
		assertTrue(result.errors.single().contains("already connected"), result.errors.toString())
	}

	@Test
	fun shouldRejectWiringAnInputThatIsAlreadyConnectedInTheCircuit() {
		val result = AiPlanValidator.validate(plan(add("a", "input"), connect("a", "#2", toPort = 1)), populatedCircuit)

		assertNull(result.plan)
		assertTrue(result.errors.single().contains("already connected"), result.errors.toString())

		// The free second input of the same gate is still accepted
		assertTrue(AiPlanValidator.validate(plan(add("a", "input"), connect("a", "#2", toPort = 2)), populatedCircuit).isValid)
	}

	@Test
	fun shouldRejectDuplicateCircuitPortNames() {
		assertNull(AiPlanValidator.validate(plan(add("a", "input", name = "A")), populatedCircuit).plan)
		assertNull(AiPlanValidator.validate(plan(add("a", "input", name = "X"), add("b", "output", name = "X")), emptyCircuit).plan)
		assertTrue(AiPlanValidator.validate(plan(add("a", "input", name = "B")), populatedCircuit).isValid)
	}

	@Test
	fun shouldRejectSelfConnection() {
		assertNull(AiPlanValidator.validate(plan(add("g", "and"), connect("g", "g")), emptyCircuit).plan)
	}

	@Test
	fun shouldRejectDeletionOfSomethingThatDoesNotExist() {
		val result = AiPlanValidator.validate(
			plan(AiOperationDto(op = AiOperationDto.OP_DELETE_COMPONENT, target = "#99")),
			populatedCircuit)

		assertNull(result.plan)
	}

	@Test
	fun shouldMarkRemovingPlansAsDestructive() {
		val delete = AiPlanValidator.validate(
			plan(AiOperationDto(op = AiOperationDto.OP_DELETE_COMPONENT, target = "#2")),
			populatedCircuit)
		assertTrue(delete.plan!!.destructive)

		val clear = AiPlanValidator.validate(plan(AiOperationDto(op = AiOperationDto.OP_CLEAR_CIRCUIT)), populatedCircuit)
		assertTrue(clear.plan!!.destructive)
	}

	@Test
	fun shouldAllowRebuildingAfterClearCircuit() {
		// Re-uses the port name "A" of the circuit that the same plan clears first
		val result = AiPlanValidator.validate(
			plan(
				AiOperationDto(op = AiOperationDto.OP_CLEAR_CIRCUIT),
				add("a", "input", name = "A"),
				add("y", "output", name = "Y"),
				connect("a", "y")
			),
			populatedCircuit)

		assertTrue(result.isValid, result.errors.toString())
		assertTrue(result.plan!!.destructive)
	}

	@Test
	fun shouldRejectOversizedPlans() {
		val operations = (0..AiPlanValidator.MAX_OPERATIONS).map { add("c$it", "and") }
		val result = AiPlanValidator.validate(AiPlanDto(operations = operations), emptyCircuit)

		assertNull(result.plan)
	}

	@Test
	fun shouldRejectAbsurdCoordinates() {
		val result = AiPlanValidator.validate(
			plan(AiOperationDto(op = AiOperationDto.OP_ADD_COMPONENT, id = "a", type = "and", x = 10_000_000, y = 0)),
			emptyCircuit)

		assertNull(result.plan)
	}
}
