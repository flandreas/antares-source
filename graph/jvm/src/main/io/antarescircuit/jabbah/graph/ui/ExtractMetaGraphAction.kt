package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.ApplicationDataViewController
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.app.AbstractSelectionAwareAction
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import java.awt.Frame
import javax.swing.JComponent
import javax.swing.JOptionPane

/**
 * An [Action] for extracting the selected [GraphElementView]s as a new [MetaGraph].
 */
class ExtractMetaGraphAction(
	private val controller: ApplicationDataViewController,
	private val service: GraphViewAppService = GraphViewModule.graphViewAppService,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	eventBus: EventBus = BaseModule.eventBus
): AbstractSelectionAwareAction("graph.action.extractMetaGraph", eventBus) {

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {

		if (EditModule.commandManager.canUndo()) {
			JOptionPane.showMessageDialog(
				Frame.getFrames()[0],
				Translations.getString("library.action.newGraph.unsavedChanges.error"),
				name,
				JOptionPane.ERROR_MESSAGE
			)
			return
		}

		if (JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("graph.action.extractMetaGraph.warning"),
			name,
			JOptionPane.YES_NO_OPTION
		) != JOptionPane.YES_OPTION) {
			return
		}

		val savable = controller.data!!.savable as AbstractContainerLibraryElementSavable

		val info = NewGraphAction.requestNewGraphInfo(
			drawingView!!.canvas as JComponent,
			Translations.getString("graph.action.extractMetaGraph.name"),
			(drawingView!!.drawing as GraphView).graph!!.type
		) ?: return

		val library = libraryHolder.library

		service.extractMetaGraph(
			info.name,
			info.type,
			castedDrawingView<DrawingView<GraphElementView<*>, GraphView>>()!!,
			library.libraryService.getDirectoryOf(library, savable.item),
			controller
		)
	}
}