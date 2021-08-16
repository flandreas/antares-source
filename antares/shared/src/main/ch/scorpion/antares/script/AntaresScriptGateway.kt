package ch.scorpion.antares.script

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.script.dsl.*
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.script.Script
import ch.scorpion.jabbah.graph.script.ScriptEngine
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner
import ch.scorpion.jabbah.graph.view.usecase.UsecaseTestRunner

/**
 * Gateway for executing Javascript code related with [ch.scorpion.antares].
 */
class AntaresScriptGateway(
	private val engine: ScriptEngine = ScriptEngine(),
	eventBus: EventBus = BaseModule.eventBus
) : ScriptGateway {

	companion object {
		private const val GRAPH_WRAPPER = "function execGraph(circuit) {\$BODY}"
		private const val VERTICE_VIEW_WRAPPER = "function execVerticeView(elem) {\$BODY}"
		private const val VERTICE_WRAPPER = "function \$UUID_execVertice(elem, data, signalHandler, store) {\$BODY}"
		private const val USECASE_ACTION_WRAPPER = "function usecaseAction(circuit) {\$BODY}"
		private const val USECASE_TEST_WRAPPER = "function usecaseTest(circuit) {\$BODY}"
	}

	init {
		eventBus.register(SchedulerActivationStateEvent::class) { store.clear(it.scheduler) }
	}

	/** Allows [Vertice]s to store [DigitalSignal]s between individual script calls.*/
	private val store = Store()

	/** ---- [ScriptGateway] interface */

	private fun functionPrefix(uuid: UUID): String = "f_" + uuid.toString().replace('-', '_')

	override val isSupported: Boolean get() = engine.isSupported

	override fun defineVerticeExecutionScript(uuid: UUID, script: Script, vertice: SubGraphVerticeRef, signalHandler: SignalHandler): Any {
		engine.eval(script.copy(code = VERTICE_WRAPPER
			.replaceFirst("\$UUID", functionPrefix(uuid))
			.replaceFirst("\$BODY", script.code)
		))
		return CircuitElemModelBridge(script, vertice, signalHandler, null, store)
	}

	override fun runVerticeExecutionScript(uuid: UUID, data: GraphActorData, params: Any) {
		(params as CircuitElemModelBridge).data = data
		engine.invoke("${functionPrefix(uuid)}_execVertice", params.script,null, params)
	}

	override fun exec(script: Script, view: DrawingView<GraphView>): Any? {
		engine.eval(script.copy(code = GRAPH_WRAPPER.replaceFirst("\$BODY", script.code)))
		return engine.invoke("execGraph", script,  null, CircuitViewBridge(script, view, null, store))
	}

	override fun exec(script: Script, verticeView: VerticeView<*>, drawContext: DrawContext) {
		engine.eval(script.copy(code = VERTICE_VIEW_WRAPPER.replaceFirst("\$BODY", script.code)))
		engine.invoke("execVerticeView", script, null, CircuitElementViewBridge(script, verticeView, null, drawContext, store))
	}

	override fun condition(script: Script, view: DrawingView<GraphView>): Boolean {
		if (StringUtils.isEmpty(script.code)) {
			return false
		}
		return exec(script, view) as Boolean
	}

	override fun usecaseAction(script: Script, runner: UsecaseRunner, scheduler: Scheduler) {
		engine.eval(script.copy(code = USECASE_ACTION_WRAPPER.replaceFirst("\$BODY", script.code)))
		val usecaseBridge = UsecaseActionBridge(runner, scheduler)
		engine.invoke("usecaseAction", null, usecaseBridge, usecaseBridge)
	}

	override fun usecaseTest(script: Script, runner: UsecaseTestRunner) {
		engine.eval(script.copy(code = USECASE_TEST_WRAPPER.replaceFirst("\$BODY", script.code)))
		val usecaseTestBridge = UsecaseTestBridge(runner)
		engine.invoke("usecaseTest", null, usecaseTestBridge, usecaseTestBridge)
	}

	/** ---- [AntaresScriptGateway] */

	/**
	 * Allows [Vertice]s to store [DigitalSignal]s between separate script calls. The [Store] gets reset
	 * whenever the execution is restarted.
	 */
	class Store {

		private val stores = mutableMapOf<SignalHandler, StorePerSignalHandler>()

		fun clear(signalHandler: SignalHandler) {
			stores.remove(signalHandler)
		}

		fun put(signalHandler: SignalHandler, vertice: Vertice, name: String, value: DigitalSignal) {
			stores
				.getOrPut(signalHandler) { StorePerSignalHandler(signalHandler) }
				.put(vertice, name, value)
		}

		fun get(signalHandler: SignalHandler, vertice: Vertice, name: String): DigitalSignal? =
			stores[signalHandler]?.get(vertice, name)
	}

	private class StorePerSignalHandler(val signalHandler: SignalHandler) {
		private val entries = mutableMapOf<Vertice, MutableMap<String, DigitalSignal>>()

		fun put(vertice: Vertice, name: String, value: DigitalSignal) {
			entries.getOrPut(vertice) { mutableMapOf() }[name] = value
		}

		fun get(vertice: Vertice, name: String): DigitalSignal? = entries[vertice]?.get(name)
	}
}