package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import javax.swing.JFrame
import javax.swing.JOptionPane

class ResetExecutionTimeAction(
    private val scheduler: Scheduler,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractSchedulerAction("execution.action.resetExecutionTime", eventBus) {

    private val schedulerStateHandler: EventHandler<SchedulerActivationStateEvent> = { updateEnabledness() }

    init {
        eventBus.register(SchedulerActivationStateEvent::class, schedulerStateHandler)
        updateEnabledness()
    }

    private fun updateEnabledness() {
        enabled = scheduler.isActive
    }

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