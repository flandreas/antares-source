package io.antarescircuit.antares.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AiPlanParserTest {

	@Test
	fun shouldParsePlainJsonObject() {
		val result = AiPlanParser.parse("""{"reply":"Done","operations":[{"op":"add_component","id":"a","type":"and"}]}""")

		val parsed = assertIs<AiPlanParser.Result.Parsed>(result)
		assertEquals("Done", parsed.dto.reply)
		assertEquals(1, parsed.dto.operations.size)
		assertEquals("and", parsed.dto.operations.first().type)
	}

	@Test
	fun shouldParseSubcircuitMetaGraphUuid() {
		val result = AiPlanParser.parse(
			"""{"reply":"Done","operations":[{"op":"add_component","id":"custom","type":"subcircuit","metaGraphUuid":"1234"}]}""")

		val operation = assertIs<AiPlanParser.Result.Parsed>(result).dto.operations.single()
		assertEquals("1234", operation.metaGraphUuid)
	}

	@Test
	fun shouldParseJsonWrappedInMarkdownFence() {
		val result = AiPlanParser.parse(
			"""
			Here you go:
			```json
			{"reply":"Built an AND gate","operations":[]}
			```
			""".trimIndent())

		assertEquals("Built an AND gate", assertIs<AiPlanParser.Result.Parsed>(result).dto.reply)
	}

	@Test
	fun shouldIgnoreProseAroundTheJsonObject() {
		val result = AiPlanParser.parse("""Sure! {"reply":"ok","operations":[]} Let me know if you need more.""")

		assertEquals("ok", assertIs<AiPlanParser.Result.Parsed>(result).dto.reply)
	}

	@Test
	fun shouldParseBareConcentratorOperationsArray() {
		val result = AiPlanParser.parse(
			"""
			```json
			[
			  {"op":"add_component","id":"i1","type":"input"},
			  {"op":"add_component","id":"i2","type":"input"},
			  {"op":"add_component","id":"i3","type":"input"},
			  {"op":"add_component","id":"i4","type":"input"},
			  {"op":"add_component","id":"join","type":"concentrator","bitWidth":4,"branchCount":4},
			  {"op":"add_component","id":"y","type":"output","bitWidth":4},
			  {"op":"connect","from":"i1","to":"join","toPort":1},
			  {"op":"connect","from":"i2","to":"join","toPort":2},
			  {"op":"connect","from":"i3","to":"join","toPort":3},
			  {"op":"connect","from":"i4","to":"join","toPort":4},
			  {"op":"connect","from":"join","to":"y"}
			]
			```
			""".trimIndent())

		val parsed = assertIs<AiPlanParser.Result.Parsed>(result)
		assertEquals(11, parsed.dto.operations.size)
		assertEquals("concentrator", parsed.dto.operations[4].type)
		assertEquals(4, parsed.dto.operations[4].branchCount)
		val validated = AiPlanValidator.validate(parsed.dto, AiCircuitContext(circuitName = "Test"))
		assertEquals(emptyList(), validated.errors)
	}

	@Test
	fun shouldNotBeConfusedByBracesInsideStrings() {
		val result = AiPlanParser.parse("""{"reply":"use {\"op\":\"connect\"} for wires","operations":[]}""")

		assertEquals("""use {"op":"connect"} for wires""", assertIs<AiPlanParser.Result.Parsed>(result).dto.reply)
	}

	@Test
	fun shouldIgnoreUnknownFields() {
		val result = AiPlanParser.parse("""{"reply":"ok","thinking":"...","operations":[{"op":"clear_circuit","why":"asked"}]}""")

		val parsed = assertIs<AiPlanParser.Result.Parsed>(result)
		assertEquals(AiOperationDto.OP_CLEAR_CIRCUIT, parsed.dto.operations.first().op)
	}

	@Test
	fun shouldRepairAComponentIdWrittenIntoTheOperationField() {
		val result = AiPlanParser.parse(
			"""{"reply":"ok","operations":[{"op":"carryAnd","type":"and","inputs":2}]}""")

		val operation = assertIs<AiPlanParser.Result.Parsed>(result).dto.operations.single()
		assertEquals(AiOperationDto.OP_ADD_COMPONENT, operation.op)
		assertEquals("carryAnd", operation.id)
		assertEquals("and", operation.type)
	}

	@Test
	fun shouldTreatAnswerWithAnIncidentalJsonObjectAsConversation() {
		val text = """To create a wire, use {"op":"connect"} with a "from" and a "to" reference."""

		assertEquals(text, assertIs<AiPlanParser.Result.Conversation>(AiPlanParser.parse(text)).text)
	}

	@Test
	fun shouldTreatAnswerWithoutJsonAsConversation() {
		val result = AiPlanParser.parse("The circuit contains two AND gates.")

		assertEquals("The circuit contains two AND gates.", assertIs<AiPlanParser.Result.Conversation>(result).text)
	}

	@Test
	fun shouldFailOnMalformedJson() {
		assertIs<AiPlanParser.Result.Failed>(AiPlanParser.parse("""{"reply":"ok","operations":[{"op":}]}"""))
	}

	@Test
	fun shouldFailOnEmptyAnswer() {
		assertIs<AiPlanParser.Result.Failed>(AiPlanParser.parse("   "))
		assertIs<AiPlanParser.Result.Failed>(AiPlanParser.parse(null))
	}
}
