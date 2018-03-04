package ch.scorpion.jabbah.graph.uifx

import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.draw.view.ViewManager
import javafx.scene.Node
import javafx.scene.control.ToolBar
import javafx.scene.layout.BorderPane

class ContainerPaneFx(
	private val viewManager: ViewManager = DrawViewModule.viewManager
) {

	private val _node = BorderPane()
	val node: Node get() = _node

	// TODO
	val toolbars: List<ToolBar> = listOf()

	fun activated() {
		// TODO
	}

	fun dispose() {
		// TODO
	}
}