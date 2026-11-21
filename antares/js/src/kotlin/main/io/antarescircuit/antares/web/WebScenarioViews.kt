package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.graph.ui.scenario.ScenarioView
import io.antarescircuit.jabbah.graph.ui.usecase.UsecaseView
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Browser [ScenarioView]. The web application asks the user before invoking a controller action and
 * passes the answer via [answer], which the controller then reads through the view's question methods.
 */
internal class WebScenarioView : ScenarioView {
	override var graphView: GraphView? = null

	/** The name for the next new scenario or step, or the confirmation for the next deletion. */
	private var pendingName: String? = null
	private var pendingConfirmation = false

	/** Runs [block] with [name] as answer to name questions and [confirmed] as answer to confirmations. */
	fun answer(name: String? = null, confirmed: Boolean = false, block: () -> Unit) {
		pendingName = name
		pendingConfirmation = confirmed
		try {
			block()
		} finally {
			pendingName = null
			pendingConfirmation = false
		}
	}

	override fun getNewScenarioName(): String? = pendingName
	override fun getNewScenarioStepName(): String? = pendingName
	override fun confirmDeleteScenario(): Boolean = pendingConfirmation
	override fun confirmDeleteScenarioStep(): Boolean = pendingConfirmation
	override fun dispose() {}
}

/** Browser [UsecaseView], answering questions like [WebScenarioView]. */
internal class WebUsecaseView : UsecaseView {
	override var graphView: GraphView? = null

	private var pendingName: String? = null

	/** Runs [block] with [name] as answer to the question for a new use case name. */
	fun answer(name: String?, block: () -> Unit) {
		pendingName = name
		try {
			block()
		} finally {
			pendingName = null
		}
	}

	override fun getNewUsecaseName(): String? = pendingName
	override fun dispose() {}
}
