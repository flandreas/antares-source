package io.antarescircuit.jabbah.execution

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.execution.scheduler.SchedulerActivationStateEvent
import javax.swing.JFrame
import javax.swing.JOptionPane

class ResetExecutionTimeAction(
    private val scheduler: Scheduler,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.resetExecutionTime", eventBus) {

    private val schedulerStateHandler: EventHandler<SchedulerActivationStateEvent> = { updateEnabled() }

    init {
        eventBus.register(SchedulerActivationStateEvent::class, schedulerStateHandler)
        updateEnabled()
    }

    override fun calculateEnabled(): Boolean = scheduler.isActive

    override fun dispose() {
        super.dispose()
        eventBus.unregister(schedulerStateHandler)
    }

    override var enabled: Boolean
        get() = super.enabled
        set(value) {
            super.enabled = value && scheduler.isActive
        }

    override fun execute(event: ActionEvent) {
        if (!scheduler.isQueueEmpty) {
            JOptionPane.showConfirmDialog(
                JFrame.getFrames()[0],
                Translations.getString("execution.action.resetExecutionTime.queueNotEmpty.msg"),
                name,
                JOptionPane.OK_OPTION,
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        scheduler.resetExecutionTime()
    }
}