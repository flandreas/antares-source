package io.antarescircuit.jabbah.graph.ui.usecase

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.ui.AbstractApplicationDataEditModeAction
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Usecase
import io.antarescircuit.jabbah.graph.view.app.UsecaseAppService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule

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

    override val opensDialog: Boolean get() = true

    override fun execute(event: ActionEvent) {
        controller.view.getNewUsecaseName()?.let {
            controller.addUsecase(it)
        }
    }

    override fun calculateEnabled(): Boolean =
        super.calculateEnabled() && applicationDataHolder.data?.content is MetaGraph && usecase == null
}