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
 *  A gateway to [Graph] related functionality implemented as javascripts.
 */
interface ScriptGateway {

	val isSupported: Boolean

	/**
	 * Defines the script function to be used when executing a [SubGraphVerticeRef].
	 * @return the object to be passed in in [runVerticeExecutionScript]
	 */
	fun defineVerticeExecutionScript(uuid: UUID, script: Script, vertice: SubGraphVerticeRef, signalHandler: SignalHandler): Any

	/**
	 * Runs the script function defined by [defineVerticeExecutionScript]
	 * @param the parameters returned from [defineVerticeExecutionScript]
	 */
	fun runVerticeExecutionScript(uuid: UUID, data: GraphActorData, params: Any)

	/**
	 * Executes a javascript [script] based on the current state of a [DrawingView].
	 * @param script the script to be executed.
	 * @param view the [DrawingView] that represents the execution context.
	 * @return the object that is returned by the script
	 */
	fun exec(script: Script, view: DrawingView<GraphView>): Any?

	/**
	 * Executes a javascript [script] based on the current state of a [VerticeView].
	 */
	fun exec(script: Script, verticeView: VerticeView<*>, drawContext: DrawContext)

	/**
	 * Evaluates a javascript condition based on the current state of a [DrawingView].
	 * @param script the javascript string to be executed.
	 * @param view the [DrawingView] representing the evaluation context.
	 * @return `true` if the condition could be satisfied
	 */
	fun condition(script: Script, view: DrawingView<GraphView>): Boolean

	fun usecaseAction(script: Script, runner: UsecaseRunner, scheduler: Scheduler)

	fun usecaseTest(script: Script, runner: UsecaseTestRunner)
}