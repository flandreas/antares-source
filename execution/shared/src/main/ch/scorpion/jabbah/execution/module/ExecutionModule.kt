package ch.scorpion.jabbah.execution.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.issue.IssueCollector
import ch.scorpion.jabbah.execution.noise.NoNoiseGenerator
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import ch.scorpion.jabbah.execution.noise.RandomNoiseGenerator
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerTask
import ch.scorpion.jabbah.execution.scheduler.TimedSchedulerTask
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory

/**
 * Module definitions for the [ch.scorpion.jabbah.execution] module
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