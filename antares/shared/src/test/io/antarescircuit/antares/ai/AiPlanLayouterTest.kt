package io.antarescircuit.antares.ai

import io.antarescircuit.jabbah.edit.Look
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AiPlanLayouterTest {

	private fun add(
		id: String,
		type: AiComponentType,
		inputCount: Int = type.defaultInputCount,
		y: Int? = null
	) = AiOperation.AddComponent(AiRef.New(id), type, id, inputCount, 0, y, 0L)

	private fun connect(from: String, to: String, toPort: Int = 1) =
		AiOperation.Connect(AiRef.New(from), 1, AiRef.New(to), toPort)

	@Test
	fun shouldLayOutAHalfAdderInTopologyColumns() {
		val plan = AiValidatedPlan("", listOf(
			add("a", AiComponentType.Input),
			add("b", AiComponentType.Input),
			add("xor", AiComponentType.Xor),
			add("and", AiComponentType.And),
			add("sum", AiComponentType.Output),
			add("carry", AiComponentType.Output),
			connect("a", "xor", 1),
			connect("a", "and", 1),
			connect("b", "xor", 2),
			connect("b", "and", 2),
			connect("xor", "sum"),
			connect("and", "carry")
		))

		val components = AiPlanLayouter.layout(plan).componentsById()

		assertEquals(components.getValue("a").x, components.getValue("b").x)
		assertEquals(components.getValue("xor").x, components.getValue("and").x)
		assertEquals(components.getValue("sum").x, components.getValue("carry").x)
		assertTrue(components.getValue("a").x!! < components.getValue("xor").x!!)
		assertTrue(components.getValue("xor").x!! < components.getValue("sum").x!!)
		assertTrue(components.getValue("a").y != components.getValue("b").y)
		assertTrue(components.getValue("xor").y != components.getValue("and").y)
	}

	@Test
	fun shouldUseModelCoordinatesOnlyAsVerticalOrderingHints() {
		val plan = AiValidatedPlan("", listOf(
			add("lower", AiComponentType.And, y = 500),
			add("upper", AiComponentType.And, y = -500)
		))

		val components = AiPlanLayouter.layout(plan).componentsById()

		assertEquals(components.getValue("upper").x, components.getValue("lower").x)
		assertTrue(components.getValue("upper").y!! < components.getValue("lower").y!!)
	}

	@Test
	fun shouldReserveRoomForTallGates() {
		val plan = AiValidatedPlan("", listOf(
			add("large", AiComponentType.And, inputCount = AiComponentType.MAX_GATE_INPUTS),
			add("small", AiComponentType.And)
		))

		val components = AiPlanLayouter.layout(plan).componentsById()
		val distance = components.getValue("small").y!! - components.getValue("large").y!!

		assertTrue(distance >= AiComponentType.MAX_GATE_INPUTS * 2 * Look.GRID)
	}

	@Test
	fun shouldBeDeterministicAndGridAligned() {
		val plan = AiValidatedPlan("", listOf(
			add("a", AiComponentType.Input),
			add("gate", AiComponentType.And),
			add("output", AiComponentType.Output),
			connect("a", "gate"),
			connect("gate", "output")
		))

		val first = AiPlanLayouter.layout(plan)
		val second = AiPlanLayouter.layout(plan)

		assertEquals(first, second)
		first.operations.filterIsInstance<AiOperation.AddComponent>().forEach {
			assertEquals(0, it.x!! % Look.GRID)
			assertEquals(0, it.y!! % Look.GRID)
		}
	}

	@Test
	fun shouldLayOutEachClearCircuitSegmentIndependently() {
		val plan = AiValidatedPlan("", listOf(
			add("component", AiComponentType.Input),
			add("output", AiComponentType.Output),
			connect("component", "output"),
			AiOperation.ClearCircuit,
			add("component", AiComponentType.And)
		))

		val components = AiPlanLayouter.layout(plan).operations
			.filterIsInstance<AiOperation.AddComponent>()

		assertTrue(components[0].x!! < components[1].x!!)
		assertEquals(0, components[2].x)
		assertEquals(0, components[2].y)
	}

	private fun AiValidatedPlan.componentsById(): Map<String, AiOperation.AddComponent> =
		operations.filterIsInstance<AiOperation.AddComponent>().associateBy { it.ref.id }
}
