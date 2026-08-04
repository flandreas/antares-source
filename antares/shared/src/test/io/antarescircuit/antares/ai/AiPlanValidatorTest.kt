package io.antarescircuit.antares.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiPlanValidatorTest {

	private val emptyCircuit = AiCircuitContext(circuitName = "Test", circuitId = "c1")

	/** A circuit with an input (#1), a 2-input AND gate (#2, first input already wired) and an output (#3). */
	private val populatedCircuit = AiCircuitContext(
		circuitName = "Test",
		circuitId = "c1",
		components = listOf(
			AiCircuitComponent(ref = "#1", type = "input", name = "A", inputCount = 0, outputCount = 1),
			AiCircuitComponent(ref = "#2", type = "AND", inputCount = 2, outputCount = 1, connectedInputs = listOf(1)),
			AiCircuitComponent(ref = "#3", type = "output", name = "Y", inputCount = 1, outputCount = 0)
		),
		connections = listOf(AiCircuitConnection(from = "#1", fromPort = 1, to = "#2", toPort = 1))
	)

	private fun plan(vararg operations: AiOperationDto) = AiPlanDto(reply = "ok", operations = operations.toList())

	private fun add(id: String, type: String, inputs: Int? = null, name: String? = null) =
		AiOperationDto(op = AiOperationDto.OP_ADD_COMPONENT, id = id, type = type, inputs = inputs, name = name)

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
	fun shouldRejectGateInputCountOutOfRange() {
		assertNull(AiPlanValidator.validate(plan(add("g", "and", inputs = 1)), emptyCircuit).plan)
		assertNull(AiPlanValidator.validate(plan(add("g", "and", inputs = 17)), emptyCircuit).plan)
		assertTrue(AiPlanValidator.validate(plan(add("g", "and", inputs = 16)), emptyCircuit).isValid)
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
