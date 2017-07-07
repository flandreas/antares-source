package ch.scorpion.antares.view

import ch.scorpion.antares.view.gate.GateMnemonicsEvent
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyleChangedEvent
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.script.ScriptGateway
import ch.scorpion.jabbah.graph.script.ScriptModule
import ch.scorpion.jabbah.graph.ui.GraphNavigationPanel
import ch.scorpion.jabbah.graph.ui.GraphNavigationPanelFactory
import ch.scorpion.jabbah.graph.view.CurrentGraphAnimationTypeEvent
import ch.scorpion.jabbah.graph.view.CurrentGraphViewAnimationType
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator

/**
 * An Antares-specific [GraphNavigationPanel] with the following additional responsibilities:
 *
 * - Repaint the [GraphView] when the [CurrentSymbolStyle] has changed
 * - Uses [GraphViewAnimator] to animate signal flow
 */
class AntaresGraphNavigationPanel(
    isRoot: Boolean,
    drawingView: DrawingView<GraphView<GraphElementView<*>>>,
    viewManager: ViewManager,
    closeHandler: ((GraphNavigationPanel) -> Unit)?,
    scheduler: Scheduler,
    eventBus: EventBus,
    libraryHolder: LibraryHolder,
    storableCreator: StorableCreator,
    animator: Animator,
    systemSpeed: SystemSpeed,
    systemSpeedCategory: CurrentSystemSpeedCategory,
    currentGraphViewAnimationType: CurrentGraphViewAnimationType,
    styleProvider: StyleProvider,
    scriptGateway: ScriptGateway
) : GraphNavigationPanel(isRoot, drawingView, viewManager, closeHandler, scheduler, animator, eventBus, libraryHolder, storableCreator, scriptGateway) {

    private val graphAnimator = GraphViewAnimator(drawingView, scheduler, animator, systemSpeed, systemSpeedCategory, eventBus, currentGraphViewAnimationType, styleProvider)

    init {
        eventBus.register(CurrentGraphAnimationTypeEvent::class, { drawingView.repaint() })
        eventBus.register(CurrentSymbolStyleChangedEvent::class, { drawingView.repaint() })
        eventBus.register(GateMnemonicsEvent::class, { drawingView.repaint() })
    }

    override fun dispose() {
        super.dispose()
        graphAnimator.dispose()
    }
}

class AntaresGraphNavigationPanelFactory : GraphNavigationPanelFactory {

    override fun create(
            isRoot: Boolean,
            drawingView: DrawingView<GraphView<GraphElementView<*>>>,
            viewManager: ViewManager,
            closeHandler: ((GraphNavigationPanel) -> Unit)?,
            scheduler: Scheduler,
            eventBus: EventBus,
            libraryHolder: LibraryHolder,
            storableCreator: StorableCreator,
            animator: Animator,
            systemSpeed: SystemSpeed,
            systemSpeedCategory: CurrentSystemSpeedCategory,
            currentGraphViewAnimationType: CurrentGraphViewAnimationType,
            styleProvider: StyleProvider,
            scriptGateway: ScriptGateway
    ): GraphNavigationPanel {
        return AntaresGraphNavigationPanel(
            isRoot, drawingView, viewManager, closeHandler, scheduler, eventBus, libraryHolder, storableCreator,
                animator, systemSpeed, systemSpeedCategory, currentGraphViewAnimationType, styleProvider, scriptGateway
        )
    }

    override fun create(
            isRoot: Boolean,
            drawingView: DrawingView<GraphView<GraphElementView<*>>>,
            viewManager: ViewManager,
            closeHandler: ((GraphNavigationPanel) -> Unit)?,
            scheduler: Scheduler
    ): GraphNavigationPanel {
        return create(
            isRoot, drawingView, viewManager, closeHandler, scheduler,
            BaseModule.eventBus,
            LibraryModule.libraryHolder,
            IOModule.storableCreator,
            AnimationModule.animator,
            BaseModule.systemSpeed,
            ExecutionModule.currentSystemSpeedCategory,
            AntaresViewModule.currentGraphViewAnimationType,
            DrawStyleModule.styleProvider,
            ScriptModule.scriptGateway)
    }
}