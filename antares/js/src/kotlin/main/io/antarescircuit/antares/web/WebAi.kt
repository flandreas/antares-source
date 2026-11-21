package io.antarescircuit.antares.web

import io.antarescircuit.antares.ai.AiCircuitContext
import io.antarescircuit.antares.ai.AiOperation
import io.antarescircuit.antares.ai.AiPlanExecutionException
import io.antarescircuit.antares.ai.AiPlanExecutor
import io.antarescircuit.antares.ai.AiPlanParser
import io.antarescircuit.antares.ai.AiPlanValidator
import io.antarescircuit.antares.ai.AiPrompt
import io.antarescircuit.antares.ai.AiValidatedPlan
import io.antarescircuit.antares.ai.OpenRouterDefaults
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.library.CurrentLibraryEvent
import io.antarescircuit.jabbah.graph.library.LibraryImportsEvent
import io.antarescircuit.jabbah.graph.library.LibraryItemAddedEvent
import io.antarescircuit.jabbah.graph.library.LibraryItemRemovedEvent
import io.antarescircuit.jabbah.graph.library.LibraryItemUpdatedEvent
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * The circuit assistant (desktop: `AiChatController`). Owns the conversation history and builds the requests;
 * the web application sends them to OpenRouter and hands the raw answer back to [complete], which validates the
 * returned plan against the live circuit and applies it as one undoable transaction, exactly like the desktop.
 *
 * Usage: [prepare] → HTTP request with the returned messages → [complete] → if it asks for confirmation,
 * [applyPending] with the user's decision.
 */
@JsExport
class WebAi internal constructor(private val ctx: WebEditorContext) {

	companion object {
		/** Number of previous conversation messages sent along with a request (as on the desktop).*/
		private const val HISTORY_LIMIT = 12
	}

	private class Message(val role: String, val content: String)

	private val history = mutableListOf<Message>()
	private var catalogMessage: Message? = null

	/** A destructive plan waiting for the user's confirmation, bound to the circuit it was validated for. */
	private var pending: Pair<AiValidatedPlan, String>? = null

	private val invalidateCatalog: EventHandler<Any> = { catalogMessage = null }

	init {
		listOf(
			CurrentLibraryEvent::class,
			LibraryImportsEvent::class,
			LibraryItemAddedEvent::class,
			LibraryItemRemovedEvent::class,
			LibraryItemUpdatedEvent::class,
		).forEach { BaseModule.eventBus.register(it, invalidateCatalog) }
	}

	/** Describes the connection defaults and the example prompts of the desktop as JSON. */
	fun info(): String = ctx.guarded("null") {
		val examples = listOf("halfAdder", "resultLeds").map { id ->
			val name = Translations.getString("antares.ai.example.$id.name")
			val prompt = Translations.getString("antares.ai.example.$id.prompt")
			obj {
				this.name = name
				this.prompt = prompt
			}
		}
		toJson(obj {
			this.defaultModel = OpenRouterDefaults.MODEL
			this.endpoint = OpenRouterDefaults.CHAT_COMPLETIONS_URL
			this.referer = OpenRouterDefaults.REFERER
			this.title = OpenRouterDefaults.TITLE
			this.examples = examples.toJsArray()
		})
	}

	/**
	 * Builds the chat messages for [prompt] about the open circuit, as JSON `{messages, circuitId}`, or `{error}`
	 * if no circuit is open. The circuit snapshot is the only information about the user's work that is sent.
	 */
	fun prepare(prompt: String): String = ctx.guarded(toJson(obj { this.error = Translations.getString("antares.ai.error.unknown") })) {
		val graphView = activeGraphView()
			?: return@guarded toJson(obj { this.error = Translations.getString("antares.ai.error.noCircuit") })

		val catalogContext = if (catalogMessage == null) AiCircuitContext.of(graphView) else null
		if (catalogContext != null) {
			catalogMessage = Message("system", AiPrompt.subcircuitCatalogMessage(catalogContext))
		}
		val context = catalogContext?.copy(availableSubcircuits = emptyList(), omittedSubcircuits = 0)
			?: AiCircuitContext.of(graphView, includeSubcircuits = false)

		val messages = buildList {
			add(Message("system", AiPrompt.systemPrompt()))
			catalogMessage?.let { add(it) }
			addAll(history)
			add(Message("system", AiPrompt.contextMessage(context)))
			add(Message("user", prompt.trim()))
		}.map { m ->
			val role = m.role
			val content = m.content
			obj {
				this.role = role
				this.content = content
			}
		}
		val circuitId = context.circuitId
		toJson(obj {
			this.messages = messages.toJsArray()
			this.circuitId = circuitId
		})
	}

	/**
	 * Records the exchange of [prompt] and the model's [raw] answer and applies the returned plan to the open
	 * circuit if it is still the one with [circuitId]. Returns JSON `{entries: [{role, text}], confirm?}` where role
	 * is assistant|error|info; `confirm: {deletions, clears}` means a destructive plan awaits [applyPending].
	 */
	fun complete(prompt: String, raw: String, circuitId: String): String = ctx.guarded(result(listOf(error(Translations.getString("antares.ai.error.unknown"))))) {
		pending = null
		history.add(Message("user", prompt.trim()))
		history.add(Message("assistant", raw))
		while (history.size > HISTORY_LIMIT) history.removeAt(0)

		val parsed = when (val parseResult = AiPlanParser.parse(raw)) {
			is AiPlanParser.Result.Failed -> return@guarded result(listOf(error(parseResult.message)))
			is AiPlanParser.Result.Conversation -> return@guarded result(listOf(entry("assistant", parseResult.text)))
			is AiPlanParser.Result.Parsed -> parseResult.dto
		}

		val entries = mutableListOf<dynamic>()
		parsed.reply?.takeIf { it.isNotBlank() }?.let { entries.add(entry("assistant", it.trim())) }
		if (parsed.operations.isEmpty()) return@guarded result(entries)
		val fail = { key: String, args: Array<Any> -> result(entries.also { it.add(error(Translations.getString(key, *args))) }) }

		val graphView = activeGraphView()
			?: return@guarded fail("antares.ai.error.noCircuitAnymore", emptyArray())
		if (graphView.graph?.uuid?.id.orEmpty() != circuitId) {
			return@guarded fail("antares.ai.error.circuitChanged", emptyArray())
		}
		if (!isEditable()) return@guarded fail("antares.ai.error.readOnly", emptyArray())

		val validation = AiPlanValidator.validate(parsed, AiCircuitContext.of(graphView))
		val plan = validation.plan
			?: return@guarded fail("antares.ai.error.rejected", arrayOf(validation.errors.joinToString("\n") { "• $it" }))

		if (plan.destructive) {
			pending = plan to circuitId
			val deletions = plan.operations.count { it is AiOperation.DeleteComponent }
			val clears = plan.operations.any { it is AiOperation.ClearCircuit }
			val message = if (clears) {
				Translations.getString("antares.ai.confirmClear.msg")
			} else {
				Translations.getString("antares.ai.confirmDelete.msg", deletions)
			}
			return@guarded result(entries, confirm = message)
		}
		result(entries.also { it.add(apply(plan)) })
	}

	/** Applies the destructive plan awaiting confirmation if [confirmed], returning the outcome like [complete]. */
	fun applyPending(confirmed: Boolean): String = ctx.guarded(result(listOf(error(Translations.getString("antares.ai.error.unknown"))))) {
		val (plan, circuitId) = pending ?: return@guarded result(emptyList())
		pending = null
		when {
			!confirmed -> result(listOf(entry("info", Translations.getString("antares.ai.notApplied"))))
			activeGraphView()?.graph?.uuid?.id.orEmpty() != circuitId ->
				result(listOf(error(Translations.getString("antares.ai.error.circuitChanged"))))
			!isEditable() -> result(listOf(error(Translations.getString("antares.ai.error.readOnly"))))
			else -> result(listOf(apply(plan)))
		}
	}

	/** Forgets the conversation so far. The circuit is not changed. */
	fun clear() {
		history.clear()
		pending = null
	}

	private fun apply(plan: AiValidatedPlan): dynamic =
		try {
			val applied = AiPlanExecutor().apply(plan, ctx.editor)
			ctx.notifyChanged()
			entry("info", Translations.getString(
				"antares.ai.applied",
				applied.addedComponents,
				applied.connections,
				applied.deletedComponents,
				applied.changedBitWidths))
		} catch (e: AiPlanExecutionException) {
			ctx.notifyChanged()
			error(Translations.getString("antares.ai.error.notApplicable", e.message ?: ""))
		}

	private fun activeGraphView(): GraphView? =
		if (ctx.document() != null) null else ctx.editor.drawing as? GraphView

	private fun isEditable(): Boolean =
		ctx.editor.view.editable && !ctx.frame().applicationModeHolder.currentMode.isExecute()

	private fun entry(role: String, text: String): dynamic = obj {
		this.role = role
		this.text = text
	}

	private fun error(text: String): dynamic = entry("error", text)

	private fun result(entries: List<dynamic>, confirm: String? = null): String {
		val array = entries.toJsArray()
		return toJson(obj {
			this.entries = array
			this.confirm = confirm
		})
	}
}
