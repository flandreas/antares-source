package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.app.AbstractSelectionAwareAction
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.app.GraphViewAppService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
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