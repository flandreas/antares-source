package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule

/** Holds the one and only [Project].*/
class ProjectHolder(
	p: Project? = null,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	companion object {
		private val LOG by logger(ProjectHolder::class)
	}

	var p: Project? = p
		set(value) {
			if (field != value) {
				LOG.trace("setting current Project to '${value?.name}'")
				field?.dispose()
				field = value
				eventBus.post(CurrentProjectEvent(field))
			}
		}

	val project: Project? get() = p
}

/**
 * Posted on an [EventBus] when the current [Project] has changed.
 */
data class CurrentProjectEvent(val project: Project?)