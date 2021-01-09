package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.StorableCreator

/**
 * Uses by object that hold [GraphView]s to control starting and stopping
 * execution of these [GraphView]s and their [Graph]s.
 *
 * Listens for [SchedulerActivationStateEvent] and does the necessary controlling logic
 * such as binding the [Graph] and calling the callback methods.
 */
class GraphViewExecutionController(
	private val isRoot: Boolean,
	private val rootGraphProvider: () -> Graph,
	private val graphViewsProvider: () -> Collection<GraphView>,
	private val repository: MetaGraphRepository = GraphModelModule.metaGraphRepository,
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	private val schedulerActivationStateHandler: (SchedulerActivationStateEvent) -> Unit = { handle(it) }

	init {
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
	}

	fun dispose() {
		eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivationStateHandler)
	}

	private fun handle(event: SchedulerActivationStateEvent) {
		val rootGraph = rootGraphProvider.invoke()
		if (event.scheduler.isActive) {
			if (isRoot) {
				rootGraph.bind(repository, storableCreator)
			}
			graphViewsProvider.invoke().forEach { it.bind() }
			rootGraph.executionStarted(event.scheduler)
		} else {
			rootGraph.executionStopped(event.scheduler)
		}
	}
}