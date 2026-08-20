package io.antarescircuit.antares.ai

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.library.CurrentLibraryEvent
import io.antarescircuit.jabbah.graph.library.LibraryImportsEvent
import io.antarescircuit.jabbah.graph.library.LibraryItemAddedEvent
import io.antarescircuit.jabbah.graph.library.LibraryItemRemovedEvent
import io.antarescircuit.jabbah.graph.library.LibraryItemUpdatedEvent
import io.antarescircuit.jabbah.graph.view.GraphView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Drives one conversation with the circuit assistant.
 *
 * The controller owns the conversation history and the single in-flight request; the Swing panel
 * only renders what it reports through [Listener]. Everything except the HTTP call runs on the
 * event dispatch thread, so the circuit is only ever touched from the EDT.
 */
class AiChatController(
	/**
	 * Resolves the [Editor] that is active *now*. Deliberately a provider and not a captured value:
	 * the plan is bound to whichever circuit is open at the moment it gets applied.
	 */
	private val editorProvider: () -> Editor?,
	private val client: OpenRouterClient = OpenRouterClient(),
	private val executor: AiPlanExecutor = AiPlanExecutor(),
	private val eventBus: EventBus = BaseModule.eventBus,
) {

	companion object {

		private val LOG by logger(AiChatController::class)

		/** Number of previous conversation messages sent along with a request.*/
		private const val HISTORY_LIMIT = 12
	}

	/** What the panel needs to know about a running conversation. */
	interface Listener {
		fun onUserMessage(text: String)
		fun onAssistantMessage(text: String)
		fun onError(text: String)
		fun onInfo(text: String)
		fun onBusyChanged(busy: Boolean)

		/**
		 * Asks the user whether a plan that removes parts of the circuit may be applied.
		 * @return `true` if the plan may be applied
		 */
		fun confirmDestructive(deletions: Int, clearsCircuit: Boolean): Boolean
	}

	var listener: Listener? = null

	private val scope = MainScope()

	private val history = mutableListOf<OpenRouterMessage>()
	private var subcircuitCatalogMessage: OpenRouterMessage? = null
	private val invalidateCatalogHandler: EventHandler<Any> = { subcircuitCatalogMessage = null }
	private val catalogEventClasses = listOf(
		CurrentLibraryEvent::class,
		LibraryImportsEvent::class,
		LibraryItemAddedEvent::class,
		LibraryItemRemovedEvent::class,
		LibraryItemUpdatedEvent::class,
	)

	init {
		catalogEventClasses.forEach { eventBus.register(it, invalidateCatalogHandler) }
	}

	private var job: Job? = null

	val isBusy: Boolean get() = job?.isActive == true

	/** Discards the conversation history. The circuit is not affected. */
	fun clearConversation() {
		history.clear()
	}

	fun cancel() {
		job?.cancel()
	}

	fun dispose() {
		scope.cancel()
		eventBus.unregister(invalidateCatalogHandler)
	}

	/**
	 * Sends [prompt] to the model and applies the returned plan to the active circuit.
	 * Does nothing if a request is already running.
	 */
	fun send(prompt: String) {
		val text = prompt.trim()
		if (text.isEmpty() || isBusy) {
			return
		}

		val apiKey = OpenRouterConfig.apiKey()
		if (apiKey.isNullOrBlank()) {
			listener?.onUserMessage(text)
			listener?.onError(Translations.getString("antares.ai.error.noApiKey", OpenRouterConfig.ENV_API_KEY))
			return
		}

		val graphView = activeGraphView()
		if (graphView == null) {
			listener?.onUserMessage(text)
			listener?.onError(Translations.getString("antares.ai.error.noCircuit"))
			return
		}

		val catalogContext = if (subcircuitCatalogMessage == null) AiCircuitContext.of(graphView) else null
		if (catalogContext != null) {
			subcircuitCatalogMessage = OpenRouterMessage.system(AiPrompt.subcircuitCatalogMessage(catalogContext))
		}
		val context = catalogContext?.copy(availableSubcircuits = emptyList(), omittedSubcircuits = 0)
			?: AiCircuitContext.of(graphView, includeSubcircuits = false)
		val messages = buildMessages(context, text)

		listener?.onUserMessage(text)
		listener?.onBusyChanged(true)

		job = scope.launch(Dispatchers.Main) {
			try {
				val raw = client.complete(messages, apiKey)
				history.add(OpenRouterMessage.user(text))
				history.add(OpenRouterMessage.assistant(raw))
				trimHistory()
				handleResponse(raw, context.circuitId)
			} catch (e: CancellationException) {
				listener?.onInfo(Translations.getString("antares.ai.cancelled"))
			} catch (e: OpenRouterException) {
				listener?.onError(e.message ?: Translations.getString("antares.ai.error.unknown"))
			} catch (e: Throwable) {
				LOG.error("Unexpected error in AI request: ${e.message}", e)
				listener?.onError(Translations.getString("antares.ai.error.unknown"))
			} finally {
				listener?.onBusyChanged(false)
			}
		}
	}

	private fun buildMessages(context: AiCircuitContext, prompt: String): List<OpenRouterMessage> {
		val messages = mutableListOf(OpenRouterMessage.system(AiPrompt.systemPrompt()))
		subcircuitCatalogMessage?.let { messages.add(it) }
		messages.addAll(history)
		messages.add(OpenRouterMessage.system(AiPrompt.contextMessage(context)))
		messages.add(OpenRouterMessage.user(prompt))
		return messages
	}

	private fun trimHistory() {
		while (history.size > HISTORY_LIMIT) {
			history.removeAt(0)
		}
	}

	/**
	 * Interprets the model answer and applies it to the circuit that is active *at this moment*,
	 * which is not necessarily the one the request was made for: the user may have switched
	 * circuits while the model was answering. The plan is therefore re-validated against a fresh
	 * snapshot of the live circuit right before it is applied.
	 */
	private fun handleResponse(raw: String, requestCircuitId: String) {
		val parsed = when (val result = AiPlanParser.parse(raw)) {
			is AiPlanParser.Result.Failed -> {
				listener?.onError(result.message)
				return
			}
			is AiPlanParser.Result.Conversation -> {
				listener?.onAssistantMessage(result.text)
				return
			}
			is AiPlanParser.Result.Parsed -> result.dto
		}

		parsed.reply?.takeIf { it.isNotBlank() }?.let { listener?.onAssistantMessage(it.trim()) }

		if (parsed.operations.isEmpty()) {
			return
		}

		val editor = editorProvider()
		val graphView = editor?.drawing as? GraphView
		if (editor == null || graphView == null) {
			listener?.onError(Translations.getString("antares.ai.error.noCircuitAnymore"))
			return
		}
		if (graphView.graph?.uuid?.id.orEmpty() != requestCircuitId) {
			listener?.onError(Translations.getString("antares.ai.error.circuitChanged"))
			return
		}
		if (!editor.view.editable) {
			listener?.onError(Translations.getString("antares.ai.error.readOnly"))
			return
		}

		val result = AiPlanValidator.validate(parsed, AiCircuitContext.of(graphView))
		val plan = result.plan
		if (plan == null) {
			listener?.onError(Translations.getString(
				"antares.ai.error.rejected",
				result.errors.joinToString("\n") { "• $it" }))
			return
		}

		applyPlan(plan, editor)
	}

	private fun applyPlan(plan: AiValidatedPlan, editor: Editor) {
		if (plan.destructive) {
			val deletions = plan.operations.count { it is AiOperation.DeleteComponent }
			val clears = plan.operations.any { it is AiOperation.ClearCircuit }
			if (listener?.confirmDestructive(deletions, clears) != true) {
				listener?.onInfo(Translations.getString("antares.ai.notApplied"))
				return
			}
		}

		try {
			val result = executor.apply(plan, editor)
			listener?.onInfo(Translations.getString(
				"antares.ai.applied",
				result.addedComponents,
				result.connections,
				result.deletedComponents,
				result.changedBitWidths))
		} catch (e: AiPlanExecutionException) {
			listener?.onError(Translations.getString("antares.ai.error.notApplicable", e.message ?: ""))
		}
	}

	private fun activeGraphView(): GraphView? = editorProvider()?.drawing as? GraphView
}
