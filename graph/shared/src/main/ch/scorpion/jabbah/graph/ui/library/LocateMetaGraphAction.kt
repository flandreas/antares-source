package ch.scorpion.jabbah.graph.ui.library

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.view.ContentView
import ch.scorpion.jabbah.draw.view.AbstractContentViewAction
import ch.scorpion.jabbah.draw.view.ContentViewManager
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.SelectionChangeEvent
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.ui.GraphNavigationView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * Locates the currently active [ContentView] containing a [MetaGraph]
 * in a [LibraryTreeView] by expanding the entire tree to the active element.
 * If a [SubGraphVerticeView] is selected, its [MetaGraph] is located in the tree.
 */
class LocateMetaGraphAction(
	private val controller: LibraryTreePanelController,
	viewManager: ContentViewManager = DrawViewModule.viewManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractContentViewAction("graph.action.locateActiveMetaGraph", eventBus, viewManager) {

	private val selectionListener: EventHandler<SelectionChangeEvent> = { updateEnabled() }

	private val noSelection: Boolean get() =
		(contentView as GraphNavigationView).drawingView?.content?.selectionManager?.selection?.isEmpty() == true

	private val singleSubGraphVerticeViewSelection: SubGraphVerticeView<SubGraphVerticeRef>? get() {
		val selection = (contentView as GraphNavigationView).drawingView?.content?.selectionManager?.selection
		if (selection != null && selection.size == 1) {
			if (selection.first() is SubGraphVerticeView<*>) {
				return selection.first() as SubGraphVerticeView<SubGraphVerticeRef>
			}
		}
		return null
	}

	init {
		imagePath = "/img/crosshair.png"
		eventBus.register(SelectionChangeEvent::class, selectionListener)
		updateEnabled()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(selectionListener)
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled()
			&& contentView is GraphNavigationView
			&& (noSelection || singleSubGraphVerticeViewSelection != null)

	override fun execute(event: ActionEvent) {
		controller.locateMetaGraph(
			singleSubGraphVerticeViewSelection?.subGraphVertice?.graphUUID
			?: (contentView as GraphNavigationView).graphView.graph!!.uuid
		)
	}
}