package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.animation.AnimationModule
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.view.ViewManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
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
		contextColor: CompositeColor?,
		scheduler: Scheduler,
		eventBus: EventBus,
		repository: MetaGraphRepository,
		storableCreator: StorableCreator,
		animator: Animator,
		systemSpeedCategory: CurrentSystemSpeedCategory,
		currentGraphViewAnimationType: CurrentGraphViewAnimationType,
		styleProvider: StyleProvider,
		scriptGateway: ScriptGateway,
		currentSystemSpeedCategory: CurrentSystemSpeedCategory
	): GraphNavigationPanel

	fun create(
		isRoot: Boolean,
		drawingView: DrawingView<GraphView<GraphElementView<*>>>,
		viewManager: ViewManager,
		contextColor: CompositeColor?,
		scheduler: Scheduler
	): GraphNavigationPanel
}

class StandardGraphNavigationPanelFactory : GraphNavigationPanelFactory {
	override fun create(
		isRoot: Boolean,
		drawingView: DrawingView<GraphView<GraphElementView<*>>>,
		viewManager: ViewManager,
		contextColor: CompositeColor?,
		scheduler: Scheduler,
		eventBus: EventBus,
		repository: MetaGraphRepository,
		storableCreator: StorableCreator,
		animator: Animator,
		systemSpeedCategory: CurrentSystemSpeedCategory,
		currentGraphViewAnimationType: CurrentGraphViewAnimationType,
		styleProvider: StyleProvider,
		scriptGateway: ScriptGateway,
		currentSystemSpeedCategory: CurrentSystemSpeedCategory
	): GraphNavigationPanel {
		return GraphNavigationPanel(
			isRoot, drawingView, viewManager, contextColor, scheduler, animator, eventBus, repository,
			storableCreator, scriptGateway, currentSystemSpeedCategory)
	}

	override fun create(
		isRoot: Boolean,
		drawingView: DrawingView<GraphView<GraphElementView<*>>>,
		viewManager: ViewManager,
		contextColor: CompositeColor?,
		scheduler: Scheduler
	): GraphNavigationPanel {
		return GraphNavigationPanel(
			isRoot, drawingView, viewManager, contextColor,
			scheduler,
			AnimationModule.animator,
			BaseModule.eventBus,
			GraphModelModule.metaGraphRepository,
			IOModule.storableCreator,
			ScriptModule.scriptGateway,
			ExecutionModule.currentSystemSpeedCategory)
	}
}
