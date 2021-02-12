package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.ui.logview.LogView
import ch.scorpion.jabbah.graph.ui.logview.LogViewController
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState

external interface LogViewJsProps : RProps {
	var controller: LogViewController
}

class LogViewJs(
	props: LogViewJsProps
) : RComponent<LogViewJsProps, RState>(props), LogView {

	init {
		props.controller.view = this
	}

	override fun RBuilder.render() {
		// Not implemented so far
	}

	override fun refresh(oldColumnsCount: Int) {
		forceUpdate()
	}

	override fun dispose() { }
}