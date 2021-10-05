package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Action
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

class ExecutionToolbarBuilder(
	private val scheduler: Scheduler,
	private val systemSpeed: SystemSpeed,
	private val applicationModeHolder: ApplicationModeHolder,
	private val toggleApplicationModeAction: ToggleApplicationModeAction,
	private val eventBus: EventBus
) {

	fun build(): ToolBar {
		val modeToggleAction = ActionWrapperSwing(toggleApplicationModeAction)
		val modeToggleButton = JToggleButton(modeToggleAction)
		modeToggleButton.text = null
		modeToggleButton.hideActionText = true
		modeToggleButton.icon = UiUtil.themedIcon("/img/play24.png")
		modeToggleButton.toolTipText = Translations.getString("execution.action.execute.name")

		val singleStepModeAction = SingleStepModeAction(scheduler, eventBus)
		val singleStepModeButton = JToggleButton(ActionWrapperSwing(singleStepModeAction))
		singleStepModeButton.text = null
		singleStepModeButton.icon = UiUtil.themedIcon("/img/singleStepMode24.png")
		singleStepModeButton.toolTipText = singleStepModeAction.name

		val speedSlider = SystemSpeedSliderSwing(systemSpeed)
		speedSlider.maximumSize = Dimension(200, speedSlider.maximumSize.height)

		val usecaseSelector = UsecaseSelector(scheduler, applicationModeHolder)

		val mainToolBar = ToolBar()
		mainToolBar.isFloatable = false
		mainToolBar.isRollover = true
		mainToolBar.addSeparator()
		mainToolBar.add(modeToggleButton)
		mainToolBar.add(createPauseOrResumeButton(PauseOrResumeAction(scheduler, eventBus)))
		mainToolBar.add(singleStepModeButton)
		mainToolBar.add(speedSlider)
		mainToolBar.add(usecaseSelector)

		return mainToolBar
	}

	private fun createPauseOrResumeButton(action: Action): JToggleButton {
		val inactiveIcon = UiUtil.themedIcon("/img/pause24.png")
		val activeIcon = UiUtil.themedIcon("/img/pause-active24.png")
		val button = JToggleButton(ActionWrapperSwing(action))
		button.text = null
		button.icon = inactiveIcon

		action.addPropertyChangeListener { e ->
			if (e.name == Action.PROP_SELECTED) {
				button.icon = if (action.selected) activeIcon else inactiveIcon
			}
		}

		return button
	}
}