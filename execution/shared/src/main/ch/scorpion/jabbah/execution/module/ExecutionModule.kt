package ch.scorpion.jabbah.execution.module

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.noise.NoiseGeneratorHolder
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerImpl
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory

/**
 * Module definitions for the [ch.scorpion.jabbah.execution] module
 */
object ExecutionModule : AbstractModule() {

    val noiseGeneratorHolder: NoiseGeneratorHolder by lazy { NoiseGeneratorHolder() }

    val scheduler: Scheduler by lazy { SchedulerImpl() }

    val currentSystemSpeedCategory: CurrentSystemSpeedCategory by lazy { CurrentSystemSpeedCategory() }

    override fun initialize() {
        BaseModule.require()
    }
}