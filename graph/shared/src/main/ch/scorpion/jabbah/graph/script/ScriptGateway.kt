package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView

/**
 *  A gateway to [Graph] related functionality implemented as javascripts.
 */
interface ScriptGateway {

    /**
     * Executes a javascript [script] based on the current state of a [DrawingView].
     * @param script the script to be executed.
     * @param view the [DrawingView] that represents the execution context.
     * @return the object that is returned by the script
     */
    fun exec(script: Script, view: DrawingView<GraphView<GraphElementView<*>>>): Any?

    /** Executes a javascript [script] based on the the current state of a [Vertice].*/
    fun exec(script: Script, vertice: Vertice, data: GraphActorData, signalHandler: SignalHandler)

    /**
     * Evaluates a javascript condition based on the current state of a [DrawingView].
     * @param script the javascript string to be executed.
     * @param view the [DrawingView] representing the evaluation context.
     * @return `true` if the condition could be satisfied
     */
    fun condition(script: Script, view: DrawingView<GraphView<GraphElementView<*>>>): Boolean
}