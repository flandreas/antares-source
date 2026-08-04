package io.antarescircuit.antares.ai

import kotlin.test.Test
import kotlin.test.assertTrue

class AiPromptTest {

	/** Guards against the allow-list and the instructions given to the model drifting apart. */
	@Test
	fun shouldDescribeEveryAllowedComponentTypeAndOperation() {
		val prompt = AiPrompt.systemPrompt()

		AiComponentType.ids.forEach {
			assertTrue(prompt.contains(it), "the system prompt does not mention the component type '$it'")
		}
		AiOperationDto.ops.forEach {
			assertTrue(prompt.contains(it), "the system prompt does not mention the operation '$it'")
		}
	}

	@Test
	fun shouldTellTheModelAboutTheCircuitContents() {
		val context = AiCircuitContext(
			circuitName = "Half adder",
			circuitId = "c1",
			components = listOf(AiCircuitComponent(ref = "#7", type = "XOR", inputCount = 2, outputCount = 1)))

		val message = AiPrompt.contextMessage(context)

		assertTrue(message.contains("Half adder"), message)
		assertTrue(message.contains("#7"), message)
		assertTrue(message.contains("XOR"), message)
	}

	@Test
	fun shouldReportAnEmptyCircuitAndOmittedComponents() {
		assertTrue(AiPrompt.contextMessage(AiCircuitContext("New circuit")).contains("empty"))

		val truncated = AiCircuitContext(
			circuitName = "Big",
			omittedComponents = 5,
			components = listOf(AiCircuitComponent(ref = "#1", type = "and")))
		assertTrue(AiPrompt.contextMessage(truncated).contains("5 further component"))
	}
}
