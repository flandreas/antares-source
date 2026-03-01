package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.app.AbstractSelectionAwareAction
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.app.GraphViewAppService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import javax.swing.JComponent

/**
 * An [Action] for extracting the selected [GraphElementView]s as a new [MetaGraph].
 */
class ExtractMetaGraphAction(
	private val applicationDataHolder: ApplicationDataHolder,
	private val service: GraphViewAppService = GraphViewModule.graphViewAppService,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	eventBus: EventBus = BaseModule.eventBus
): AbstractSelectionAwareAction("graph.action.extractMetaGraph", eventBus) {

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		val savable = applicationDataHolder.data!!.savable as AbstractContainerLibraryElementSavable

		val info = NewGraphAction.requestNewGraphInfo(
			drawingView!!.canvas as JComponent,
			Translations.getString("graph.action.extractMetaGraph.name"),
			(drawingView!!.drawing as GraphView).graph!!.type
		) ?: return

		val library = libraryHolder.library

		service.extractMetaGraph(
			info.name,
			info.type,
			drawingView as DrawingView<GraphView>,
			library.libraryService.getDirectoryOf(library, savable.item)
		)
	}
}