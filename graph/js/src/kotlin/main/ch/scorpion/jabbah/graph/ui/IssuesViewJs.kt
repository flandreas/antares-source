package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.execution.issue.IssuesView
import ch.scorpion.jabbah.execution.issue.IssuesViewController
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

external interface IssuesViewJsProps : RProps {
	var controller: IssuesViewController
}

class IssuesViewJs(
	props: IssuesViewJsProps
) : RComponent<IssuesViewJsProps, RState>(props), IssuesView {

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