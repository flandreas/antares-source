package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.view.CurrentGraphViewAnimationType
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator

interface GraphNavigationPanelFactory {
    fun create(
            isRoot: Boolean,
            drawingView: DrawingView<GraphView<GraphElementView<*>>>,
            viewManager: ViewManager,
            closeHandler: ((GraphNavigationPanel) -> Unit)?,
            contextColor: CompositeColor?,
            scheduler: Scheduler,
            eventBus: EventBus,
            libraryHolder: LibraryHolder,
            storableCreator: StorableCreator,
            animator: Animator,
            systemSpeedCategory: CurrentSystemSpeedCategory,
            currentGraphViewAnimationType: CurrentGraphViewAnimationType,
            styleProvider: StyleProvider,
            scriptGateway: ScriptGateway
    ): GraphNavigationPanel

    fun create(
        isRoot: Boolean,
        drawingView: DrawingView<GraphView<GraphElementView<*>>>,
        viewManager: ViewManager,
        closeHandler: ((GraphNavigationPanel) -> Unit)?,
        contextColor: CompositeColor?,
        scheduler: Scheduler
    ): GraphNavigationPanel
}

class StandardGraphNavigationPanelFactory: GraphNavigationPanelFactory {
    override fun create(
            isRoot: Boolean,
            drawingView: DrawingView<GraphView<GraphElementView<*>>>,
            viewManager: ViewManager,
            closeHandler: ((GraphNavigationPanel) -> Unit)?,
            contextColor: CompositeColor?,
            scheduler: Scheduler,
            eventBus: EventBus,
            libraryHolder: LibraryHolder,
            storableCreator: StorableCreator,
            animator: Animator,
            systemSpeedCategory: CurrentSystemSpeedCategory,
            currentGraphViewAnimationType: CurrentGraphViewAnimationType,
            styleProvider: StyleProvider,
            scriptGateway: ScriptGateway
    ): GraphNavigationPanel {
        return GraphNavigationPanel(
            isRoot, drawingView, viewManager, closeHandler, contextColor, scheduler, animator, eventBus, libraryHolder,
                storableCreator, scriptGateway)
    }

    override fun create(
            isRoot: Boolean,
            drawingView: DrawingView<GraphView<GraphElementView<*>>>,
            viewManager: ViewManager,
            closeHandler: ((GraphNavigationPanel) -> Unit)?,
            contextColor: CompositeColor?,
            scheduler: Scheduler
    ): GraphNavigationPanel {
        return GraphNavigationPanel(
            isRoot, drawingView, viewManager, closeHandler, contextColor,
            scheduler,
            AnimationModule.animator,
            BaseModule.eventBus,
            LibraryModule.libraryHolder,
            IOModule.storableCreator,
            ScriptModule.scriptGateway)
    }
}
