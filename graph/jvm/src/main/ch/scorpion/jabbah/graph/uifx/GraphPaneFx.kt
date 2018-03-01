package ch.scorpion.jabbah.graph.uifx

import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.view.FocusPaneFx
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentPropertyPaneFx
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.library.uifx.LibraryPaneFx
import ch.scorpion.jabbah.graph.view.GraphView
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox

/**
 * A controller of a [Node] for editing and executing a root [GraphView].
 */
class GraphPaneFx(
	private val editor: Editor,
	private val outerLibraryDrawableDrawer: DrawableDrawer<Component>? = null
) {

	private lateinit var _node: BorderPane
	val node: Node get() = _node

	init {
		buildUI()
	}

	/** ---- [GraphPaneFx] */

	private fun buildUI() {
		val leftPane = VBox()
		val libraryScrollPane = ScrollPane()
		libraryScrollPane.vvalue
		libraryScrollPane.content = LibraryPaneFx(outerDrawableDrawer = outerLibraryDrawableDrawer).node
		leftPane.children.setAll(buildPropertyPaneNode(), libraryScrollPane)
		_node = BorderPane()
		_node.left = leftPane
		_node.center = FocusPaneFx(editor.view)
	}

	private fun buildPropertyPaneNode(): Node {
		val propertyPane = ComponentPropertyPaneFx(editor)
		propertyPane.padding = Insets(10.0, 10.0, 10.0, 10.0)
		val scrollPane = ScrollPane(propertyPane)
		scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
		scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
		scrollPane.prefViewportWidth = 350.0
		return propertyPane
	}
}