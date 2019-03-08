package ch.scorpion.antares.script

import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.script.*
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner

/**
 * Gateway for executing Javascript code related with [antares].
 */
class AntaresScriptGateway(
	private val engine: ScriptEngine = ScriptModule.scriptEngineProvider.invoke(),
	eventBus: EventBus = BaseModule.eventBus
) : ScriptGateway {

	companion object {
		private const val GRAPH_WRAPPER = "function execGraph(circuit) {\$BODY}"
		private const val VERTICE_VIEW_WRAPPER = "function execVerticeView(elem) {\$BODY}"
		private const val VERTICE_WRAPPER = "function execVertice(elem, data, signalHandler, store) {\$BODY}"
		private const val USECASE_WRAPPER = "function runUsecase(circuit) {\$BODY}"
	}

	init {
		eventBus.register(SchedulerActivationStateEvent::class) { store.clear() }
	}

	/** Allows [Vertice]s to store [Word]s between individual script calls.*/
	private val store = Store()

	/** ---- [ScriptGateway] interface */

	override fun exec(script: Script, view: DrawingView<GraphView<GraphElementView<*>>>): Any? {
		engine.eval(script.copy(code = GRAPH_WRAPPER.replaceFirst("\$BODY", script.code)))
		return engine.invoke("execGraph", null, CircuitViewBridge(view, null))
	}

	override fun exec(script: Script, verticeView: VerticeView<*>, drawContext: DrawContext) {
		engine.eval(script.copy(code = VERTICE_VIEW_WRAPPER.replaceFirst("\$BODY", script.code)))
		engine.invoke("execVerticeView", null, CircuitElementViewBridge(verticeView, null, drawContext))
	}

	override fun exec(script: Script, vertice: Vertice, data: GraphActorData, signalHandler: SignalHandler) {
		engine.eval(script.copy(code = VERTICE_WRAPPER.replaceFirst("\$BODY", script.code)))
		engine.invoke("execVertice", null, CircuitElemModelBridge(vertice, signalHandler, data, store))
	}

	override fun condition(script: Script, view: DrawingView<GraphView<GraphElementView<*>>>): Boolean {
		if (StringUtils.isEmpty(script.code)) {
			return false
		}
		return exec(script, view) as Boolean
	}

	override fun usecase(script: Script, runner: UsecaseRunner, signalHandler: SignalHandler) {
		engine.eval(script.copy(code = USECASE_WRAPPER.replaceFirst("\$BODY", script.code)))
		val usecaseBridge = UsecaseBridge(runner, signalHandler)
		engine.invoke("runUsecase", usecaseBridge, usecaseBridge)
	}

	/** ---- [AntaresScriptGateway] */

	/**
	 * Allows [Vertice]s to store [Word]s between separate script calls. The [Store] gets reset
	 * whenever the execution is restarted.
	 *
	 * @param context the [Vertice] whose script is currently being executed
	 */
	class Store(var context: Vertice? = null) {

		private val words = mutableMapOf<Vertice, Word>()

		fun clear() = words.clear()

		fun put(word: Word) = words.put(context!!, word)

		fun get(): Word = words[context]!!
	}
}