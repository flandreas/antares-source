package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.PauseOrResumeAction
import ch.scorpion.jabbah.execution.SingleStepModeAction
import ch.scorpion.jabbah.execution.SystemSpeedSliderSwing
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.app.ToggleApplicationModeAction
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseSelector
import java.awt.Dimension
import javax.swing.JToggleButton

class ExecutionToolbarSwing(
	scheduler: Scheduler,
	systemSpeed: SystemSpeed,
	applicationModeHolder: ApplicationModeHolder,
	toggleApplicationModeAction: ToggleApplicationModeAction,
	eventBus: EventBus
) : ToolBar() {

	private val singleStepModeAction = SingleStepModeAction(scheduler, eventBus)
	private val pauseOrResumeAction = PauseOrResumeAction(scheduler, eventBus)
	private val speedSlider = SystemSpeedSliderSwing(systemSpeed)
	private val usecaseSelector = UsecaseSelector(scheduler, applicationModeHolder)

	init {
		val modeToggleButton = JToggleButton(ActionWrapperSwing(toggleApplicationModeAction))
		modeToggleButton.text = null
		modeToggleButton.hideActionText = true
		modeToggleButton.icon = UiUtil.themedIcon("/img/play24.png")
		modeToggleButton.toolTipText = Translations.getString("execution.action.execute.name")

		val singleStepModeButton = JToggleButton(ActionWrapperSwing(singleStepModeAction))
		singleStepModeButton.text = null
		singleStepModeButton.icon = UiUtil.themedIcon("/img/singleStepMode24.png")
		singleStepModeButton.toolTipText = singleStepModeAction.name

		val pauseOrResumeButton = JToggleButton(ActionWrapperSwing(pauseOrResumeAction))
		pauseOrResumeButton.text = null

		speedSlider.maximumSize = Dimension(200, speedSlider.maximumSize.height)

		isFloatable = false
		isRollover = true
		addSeparator()
		add(modeToggleButton)
		add(pauseOrResumeButton)
		add(singleStepModeButton)
		add(speedSlider)
		add(usecaseSelector)
	}

	fun dispose() {
		singleStepModeAction.dispose()
		pauseOrResumeAction.dispose()
		speedSlider.dispose()
		usecaseSelector.dispose()
	}
}