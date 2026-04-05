package io.antarescircuit.jabbah.execution.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.issue.IssueCollector
import io.antarescircuit.jabbah.execution.noise.NoNoiseGenerator
import io.antarescircuit.jabbah.execution.noise.NoiseGeneratorHolder
import io.antarescircuit.jabbah.execution.noise.RandomNoiseGenerator
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.scheduler.SchedulerTask
import io.antarescircuit.jabbah.execution.scheduler.TimedSchedulerTask
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.execution.speed.SystemSpeedCategory

/**
 * Module definitions for the [io.antarescircuit.jabbah.execution] module
 */
object ExecutionModule : AbstractModule() {

	val noNoiseGenerator = NoNoiseGenerator()

	val randomNoiseGenerator = RandomNoiseGenerator()

	val noiseGeneratorHolder: NoiseGeneratorHolder by lazy { NoiseGeneratorHolder(noNoiseGenerator) }

	var schedulerTaskFactory: (CurrentSystemSpeedCategory, EventBus) -> SchedulerTask = { speedCategory, eventBus -> TimedSchedulerTask(speedCategory, eventBus = eventBus) }

	val issueCollector: IssueCollector = IssueCollector(eventBus = BaseModule.eventBus, clearOnExecutionStart = true)

	override fun initialize() {
		BaseModule.require()
		fillProperties(BaseModule.properties)
		Translations.addBundle("jabbah-execution")
	}

	override fun resetDependencies() {
		BaseModule.reset()
	}

	private fun fillProperties(properties: Properties) {
		properties.set(Scheduler.PROP_SCHEDULER_EVENT_SYSTEM_SPEED_LIMIT, SystemSpeedCategory.Observe.customName)
		properties.set(IssueCollector.PROP_MAX_ISSUES_COUNT, 100)
		properties.set(TimedSchedulerTask.PROP_SLOWDOWN_FACTOR, TimedSchedulerTask.DEF_SLOWDOWN_FACTOR)
	}
}