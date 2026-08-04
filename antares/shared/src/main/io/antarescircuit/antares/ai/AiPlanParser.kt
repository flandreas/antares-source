package io.antarescircuit.antares.ai

import kotlinx.serialization.json.Json

/**
 * Parses the raw text of a model response into the [AiPlanDto] wire format.
 *
 * Models tend to wrap JSON in Markdown fences or to add a sentence before or after the object,
 * so the parser locates the outermost JSON object before decoding it. If no JSON object is
 * present at all, the answer is treated as plain conversation without operations.
 */
object AiPlanParser {

	private val json = Json {
		ignoreUnknownKeys = true
		isLenient = true
	}

	sealed interface Result {
		data class Parsed(val dto: AiPlanDto) : Result
		/** The answer contained no operation object and is therefore plain conversation.*/
		data class Conversation(val text: String) : Result
		data class Failed(val message: String) : Result
	}

	fun parse(raw: String?): Result {
		val text = raw?.trim().orEmpty()
		if (text.isEmpty()) {
			return Result.Failed("The assistant returned an empty answer.")
		}

		val candidate = extractJsonObject(stripCodeFences(text))
			?: return Result.Conversation(text)

		val decoded = try {
			json.decodeFromString(AiPlanDto.serializer(), candidate)
		} catch (e: Exception) {
			return Result.Failed("The assistant returned malformed JSON: ${e.message ?: "unknown error"}")
		}
		val dto = repairMisplacedComponentIds(decoded)

		// A JSON object without any plan field is an incidental object inside a prose answer,
		// e.g. an operation quoted as an example. Treating it as a plan would show nothing at all.
		if (dto.reply == null && dto.operations.isEmpty()) {
			return Result.Conversation(text)
		}

		return Result.Parsed(dto)
	}

	/**
	 * Repairs the harmless and unambiguous mistake of writing a new component's ID into `op`.
	 * Destructive and connection operations are deliberately never inferred.
	 */
	private fun repairMisplacedComponentIds(dto: AiPlanDto): AiPlanDto = dto.copy(
		operations = dto.operations.map { operation ->
			val misplacedId = operation.op
			if (!misplacedId.isNullOrBlank()
				&& misplacedId !in AiOperationDto.ops
				&& operation.id == null
				&& AiComponentType.withId(operation.type.orEmpty()) != null
				&& operation.from == null
				&& operation.to == null
				&& operation.target == null
			) {
				operation.copy(op = AiOperationDto.OP_ADD_COMPONENT, id = misplacedId)
			} else {
				operation
			}
		})

	/** Removes surrounding Markdown code fences, keeping the fenced content. */
	private fun stripCodeFences(text: String): String {
		val start = text.indexOf("```")
		if (start < 0) {
			return text
		}
		val afterFence = text.indexOf('\n', start)
		if (afterFence < 0) {
			return text
		}
		val end = text.indexOf("```", afterFence)
		return if (end < 0) text.substring(afterFence + 1) else text.substring(afterFence + 1, end)
	}

	/**
	 * Returns the first balanced JSON object of [text], ignoring braces inside string literals.
	 */
	private fun extractJsonObject(text: String): String? {
		val start = text.indexOf('{')
		if (start < 0) {
			return null
		}
		var depth = 0
		var inString = false
		var escaped = false
		for (i in start until text.length) {
			val c = text[i]
			when {
				escaped -> escaped = false
				c == '\\' && inString -> escaped = true
				c == '"' -> inString = !inString
				inString -> Unit
				c == '{' -> depth++
				c == '}' -> {
					depth--
					if (depth == 0) {
						return text.substring(start, i + 1)
					}
				}
			}
		}
		return null
	}
}
