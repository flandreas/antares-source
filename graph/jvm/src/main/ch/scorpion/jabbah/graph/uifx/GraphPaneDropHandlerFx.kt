package ch.scorpion.jabbah.graph.uifx

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentDropHandlerFx
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.IOModule
import javafx.scene.control.Alert

class GraphPaneDropHandlerFx(
	editor: Editor,
	eventBus: EventBus = BaseModule.eventBus,
	private val libraryHolder: LibraryHolder
) : ComponentDropHandlerFx(editor, eventBus) {

	companion object {
		private val LOG by logger(GraphPaneDropHandlerFx::class)
	}

	override fun canImport(dropComponent: Component): Boolean {
		if (dropComponent !is GraphElementView<*>) {
			return super.canImport(dropComponent)
		}
		if (!editor.view.editable) {
			return false
		}

		if (dropComponent.model !is SubGraphVertice) {
			return super.canImport(dropComponent)
		}

		val dropVertice = dropComponent.model as SubGraphVertice?
		val dropGraph = libraryHolder.library.getMetaGraph(dropVertice!!.graphUUID!!).graph!!.model

		val canImport = !libraryHolder.library.graphContainsRecursively(
			dropGraph!!.uuid,
			(editor.drawing as GraphView<*>).graph!!.uuid)

		if (!canImport) {
			LOG.debug("Preventing dropping '${dropVertice.name}' in order to prevent Graph cycle")
			val alert = Alert(Alert.AlertType.ERROR)
			alert.title = Translations.getString("graph.action.addElementToGraph.name")
			alert.contentText = Translations.getString("graph.cycleError.msg")
			//alert.initOwner(primaryStage)
			alert.showAndWait()
		}

		return canImport
	}

	override fun extractComponent(transferData: String): Component {
		val graphStorable = IOModule.storableClonerProvider.invoke().deserialize(transferData) as GraphStorable
		return graphStorable.graphView!!.get(0)
	}
}