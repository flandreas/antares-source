package ch.scorpion.jabbah.graph.ui.usecase

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.AbstractApplicationDataEditModeAction
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.Usecase
import ch.scorpion.jabbah.graph.view.app.UsecaseAppService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule

abstract class AbstractUsecaseAction(
    protected val controller: UsecaseViewController,
    baseName: String,
    protected val service: UsecaseAppService = GraphViewModule.usecaseAppService,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractApplicationDataEditModeAction(baseName, controller.applicationDataHolder, controller.applicationModeHolder, eventBus) {

    protected var usecase: Usecase? = null

    private val usecaseSelectionHandler: EventHandler<UsecaseSelectionEvent> = {
        usecase = it.usecase
        updateEnabled()
    }

    init {
        eventBus.register(UsecaseSelectionEvent::class, usecaseSelectionHandler)
    }

    override fun dispose() {
        super.dispose()
        eventBus.unregister(usecaseSelectionHandler)
    }

    protected val graphView: GraphView? get() =
        (applicationDataHolder.data!!.content as? MetaGraph)?.graph?.graphView
}

/** Asks the user for the name of a new [Usecase] and adds it to the current [GraphView].*/
class AddUsecaseAction(
    controller: UsecaseViewController,
    service: UsecaseAppService = GraphViewModule.usecaseAppService,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractUsecaseAction(controller,"usecases.action.addUsecase", service, eventBus) {

    override fun execute(event: ActionEvent) {
        controller.view.getNewUsecaseName()?.let {
            controller.addUsecase(it)
        }
    }

    override fun calculateEnabled(): Boolean =
        super.calculateEnabled() && usecase == null
}