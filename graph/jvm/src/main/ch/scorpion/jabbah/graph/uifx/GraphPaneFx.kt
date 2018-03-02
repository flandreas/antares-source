package ch.scorpion.jabbah.graph.uifx

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.view.FocusPaneFx
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentPropertyPaneFx
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.library.uifx.LibraryPaneFx
import ch.scorpion.jabbah.graph.view.GraphView
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.SplitPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox

/**
 * A controller of a [Node] for editing and executing a root [GraphView].
 */
class GraphPaneFx(
	private val editor: Editor,
	private val outerLibraryDrawableDrawer: DrawableDrawer<Component>? = null
) {

	companion object {
		private val PROP_MAIN_SPLIT_POS = "graphPanel.mainSplitPosFX"
		private val PROP_LIBRARY_SPLIT_POS = "graphPanel.librarySplitPosFX"
	}

	private var mainSplitPane = SplitPane()

	private val librarySplitPane = SplitPane()

	val node: Node get() = mainSplitPane

	init {
		buildUI()
	}

	/** ---- [GraphPaneFx] */

	fun dispose() {
		BaseModule.settings.set(PROP_MAIN_SPLIT_POS, mainSplitPane.dividerPositions[0])
		BaseModule.settings.set(PROP_LIBRARY_SPLIT_POS, librarySplitPane.dividerPositions[0])
	}

	private fun buildUI() {
		val libraryPane = LibraryPaneFx(outerDrawableDrawer = outerLibraryDrawableDrawer).node
		val libraryScrollPane = ScrollPane()
		libraryScrollPane.content = libraryPane

		librarySplitPane.orientation = Orientation.VERTICAL
		librarySplitPane.items.setAll(libraryScrollPane, buildPropertyPaneNode())
		SplitPane.setResizableWithParent(librarySplitPane, false)

		// TEST BEGIN
		libraryScrollPane.widthProperty().addListener { _ -> libraryPane.prefWidth = libraryScrollPane.viewportBounds.width - 1 }
		// TEST END

		val rightPane = FocusPaneFx(editor.view)
		SplitPane.setResizableWithParent(rightPane, true)

		mainSplitPane.orientation = Orientation.HORIZONTAL
		mainSplitPane.items.setAll(librarySplitPane, rightPane)

		Platform.runLater {
			librarySplitPane.setDividerPositions(BaseModule.settings.getFloat(PROP_MAIN_SPLIT_POS, 0.5f).toDouble())
			mainSplitPane.setDividerPositions(BaseModule.settings.getFloat(PROP_LIBRARY_SPLIT_POS, 0.3f).toDouble())
		}
	}

	private fun buildPropertyPaneNode(): Node {
		val propertyPane = ComponentPropertyPaneFx(editor)
		propertyPane.padding = Insets(10.0, 10.0, 10.0, 10.0)
		val scrollPane = ScrollPane(propertyPane)
		scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
		scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
		return propertyPane
	}
}