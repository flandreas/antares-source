package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.execution.issue.IssuesView
import ch.scorpion.jabbah.execution.issue.IssuesViewController
import react.*

external interface IssuesViewJsProps : Props {
	var controller: IssuesViewController
}

class IssuesViewJs(
	props: IssuesViewJsProps
) : RComponent<IssuesViewJsProps, State>(props), IssuesView {

	init {
		props.controller.view = this
	}

	override fun RBuilder.render() {
		// Not implemented so far
	}

	override fun refresh() {
		forceUpdate()
	}

	override fun dispose() { }
}