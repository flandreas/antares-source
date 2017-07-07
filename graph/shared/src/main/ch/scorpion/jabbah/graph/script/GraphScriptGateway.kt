package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Gateway for executing Javascript code related with [Graph]s.
 */
class GraphScriptGateway(private val engine: ScriptEngine) : ScriptGateway {

    companion object {
        val GRAPH_WRAPPER = "function execGraph(graph) {\$BODY}"
        val VERTICE_WRAPPER = "function execVertice(vertice, data, signalHandler) {\$BODY}"
    }

    override fun exec(script: String, view: DrawingView<GraphView<GraphElementView<*>>>): Any? {
        val code = GRAPH_WRAPPER.replaceFirst("\$BODY", script)
        engine.eval(code)
        return engine.invoke("execGraph", GraphViewBridge(view))
    }

    override fun exec(script: String, vertice: Vertice, data: GraphActorData, signalHandler: SignalHandler) {
        val code = VERTICE_WRAPPER.replaceFirst("\$BODY", script)
        engine.eval(code)
        engine.invoke("execVertice", VerticeBridge(vertice, signalHandler))
    }

    override fun condition(script: String, view: DrawingView<GraphView<GraphElementView<*>>>): Boolean {
        return exec(script, view) as Boolean
    }
}