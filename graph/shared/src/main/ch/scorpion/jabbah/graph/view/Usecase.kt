package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.io.Storable

/**
 * A [Usecase] is a representation of a single way the user can use a [GraphView].
 * The author of a [GraphView] programs the individual interactions that should occur with the [GraphView]
 * while a [Usecase] is executed. The user can then start the execution of a [Usecase] and observe
 * how the [GraphView] behaves. During [Usecase] execution, the user cannot interact with the [GraphView].
 * <p>
 * In addition, a [Usecase] can contain assertion code that checks certain conditions after execution of
 * a [Usecase], which can be used to automatically testing a [GraphView].
 */
interface Usecase : Namable, Describable, Storable {

	/** The identification of this [Usecase] that is unique within a [GraphView]. */
	var id: Int

	/** The JavaScript script to be executed when this [Usecase] is executed.*/
	var executionScript: String

	/** The JavaScript script to be executed when testing this [Usecase].*/
	var testScript: String?

	val hasTest: Boolean get() = testScript?.isNotBlank() ?: false

	fun executionStart(graphView: GraphView, signalHandler: SignalHandler)

	fun run()

	fun runTest()

	fun dispose()
}