package io.antarescircuit.jabbah.graph.ui.usecase

import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Usecase
import io.antarescircuit.jabbah.graph.view.app.UsecaseAppService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseImpl

/**
 * Posted by [UsecaseView] on its [EventBus] when the user defines the current [Usecase]
 * by changing the selection in the [UsecaseView].
 */
data class UsecaseSelectionEvent(
	val graphView: GraphView,
	val usecase: Usecase?
)

/**
 * Displays the [Usecase]s of a [GraphView] and allows the user to inspect, add, remove and edit them.
 */
interface UsecaseView : UIView {

	/** The [GraphView] whose [Usecase]s are displayed. */
	var graphView: GraphView?

	/**
	 * Asks the user for the name of a new [Usecase].
	 * @return `null` if the user cancelled the action
	 */
	fun getNewUsecaseName(): String?
}

class UsecaseViewController(
	editor: Editor,
	val applicationDataHolder: ApplicationDataHolder,
	val applicationContextHolder: GraphApplicationContextHolder,
	val applicationModeHolder: ApplicationModeHolder,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val service: UsecaseAppService = GraphViewModule.usecaseAppService
) : AbstractUIController<UsecaseView>() {

	private val usecaseSelectionHandler: EventHandler<UsecaseSelectionEvent> = { handle(it) }

	var usecase: Usecase? = null
		private set

	val propertyPanelController = UsecasePropertyPanelController(editor, eventBus)

	/** The [GraphView] whose [Usecase]s are displayed. */
	var graphView: GraphView? = null
		set(value) {
			if (field !== value) {
				field = value
				view.graphView = value
				usecase = null
				updateActions()
			}
		}

	val addUsecaseAction = AddUsecaseAction(this)

	val metaAddAction: Action = MetaAddAction()

	init {
		eventBus.register(UsecaseSelectionEvent::class, usecaseSelectionHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(usecaseSelectionHandler)
		propertyPanelController.dispose()
	}

	fun addUsecase(name: String) {
		service.addUsecase(applicationDataHolder, UsecaseImpl(name))
	}

	private fun handle(event: UsecaseSelectionEvent) {
		usecase = event.usecase
		updateActions()
	}

	private fun updateActions() {
		updateMetaAddAction()
	}

	private fun updateMetaAddAction() {
		metaAddAction.enabled = addUsecaseAction.enabled
	}

	private inner class MetaAddAction : AbstractAction("usecases.action.addUsecase", "/img/plus-18.png") {
		init {
		    description = addUsecaseAction.name
		}
		override fun execute(event: ActionEvent) {
			addUsecaseAction.execute(ActionEvent(null, this, 0, "", 0))
		}
	}
}