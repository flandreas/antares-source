package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.usecase.UsecaseRunner
import ch.scorpion.jabbah.graph.view.usecase.UsecaseTestRunner

/**
 * Gateway for executing Javascript code related with [Graph]s.
 */
class GraphScriptGateway(private val engine: ScriptEngine) : ScriptGateway {

	companion object {
		private const val GRAPH_WRAPPER = "function execGraph(graph) {\$BODY}"
		private const val VERTICE_VIEW_WRAPPER = "function execVerticeView(verticeView) {\$BODY}"
	}

	override fun defineVerticeExecutionScript(uuid: UUID, script: Script, vertice: SubGraphVerticeRef, signalHandler: SignalHandler): Any {
		throw UnsupportedOperationException("not implemented")
	}

	override fun runVerticeExecutionScript(uuid: UUID, data: GraphActorData, params: Any) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun exec(script: Script, view: DrawingView<GraphView>): Any? {
		engine.eval(script.copy(code = GRAPH_WRAPPER.replaceFirst("\$BODY", script.code)))
		return engine.invoke("execGraph", null, null, GraphViewBridge(view))
	}

	override fun exec(script: Script, verticeView: VerticeView<*>, drawContext: DrawContext) {
		engine.eval(script.copy(code = VERTICE_VIEW_WRAPPER.replaceFirst("\$BODY", script.code)))
		engine.invoke("execVerticeView", null,null, GraphElementViewBridge(verticeView))
	}

	override fun condition(script: Script, view: DrawingView<GraphView>): Boolean {
		return exec(script, view) as Boolean
	}

	override fun usecaseAction(script: Script, runner: UsecaseRunner, scheduler: Scheduler) {
		throw UnsupportedOperationException("not implemented")
	}

	override fun usecaseTest(script: Script, runner: UsecaseTestRunner) {
		throw UnsupportedOperationException("not implemented")
	}
}