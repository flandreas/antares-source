package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.app.ToolBar
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.execution.PauseOrResumeAction
import io.antarescircuit.jabbah.execution.SystemSpeedSliderSwing
import io.antarescircuit.jabbah.execution.scheduler.Scheduler
import io.antarescircuit.jabbah.graph.app.ApplicationModeHolder
import io.antarescircuit.jabbah.graph.ui.usecase.UsecaseSelector
import java.awt.Dimension
import javax.swing.JToggleButton

class ExecutionToolbarSwing(
	scheduler: Scheduler,
	systemSpeed: SystemSpeed,
	applicationModeHolder: ApplicationModeHolder,
	toggleApplicationModeAction: Action,
	singleStepModeAction: Action,
	pauseOrResumeAction: PauseOrResumeAction,
	executionDepthAction: Action,
) : ToolBar() {

	private val toggleApplicationModeWrapper = ActionWrapperSwing(toggleApplicationModeAction)
	private val singleStepModeWrapper = ActionWrapperSwing(singleStepModeAction)
	private val pauseOrResumeButton = PauseOrResumeButton(pauseOrResumeAction)
	private val executionDepthWrapper = ActionWrapperSwing(executionDepthAction)

	private val speedSlider = SystemSpeedSliderSwing(systemSpeed)
	private val usecaseSelector = UsecaseSelector(scheduler, applicationModeHolder)

	init {
		val modeToggleButton = JToggleButton(toggleApplicationModeWrapper)
		modeToggleButton.text = null
		modeToggleButton.hideActionText = true
		modeToggleButton.icon = UiUtil.themedIcon("/img/play24.png")
		modeToggleButton.toolTipText = Translations.getString("execution.action.start.desc")

		val singleStepModeButton = JToggleButton(singleStepModeWrapper)
		singleStepModeButton.text = null
		singleStepModeButton.icon = UiUtil.themedIcon("/img/singleStepMode24.png")
		singleStepModeButton.toolTipText = singleStepModeAction.name

		speedSlider.preferredSize = Dimension(150, speedSlider.preferredSize.height)
		speedSlider.maximumSize = Dimension(150, speedSlider.maximumSize.height)

		val executionDepthButton = JToggleButton(executionDepthWrapper)
		executionDepthButton.hideActionText = true
		executionDepthButton.icon = UiUtil.themedIcon("/img/execution-shallow24.png")
		executionDepthButton.selectedIcon = UiUtil.themedIcon("/img/execution-deep24.png")
		fun updateExecutionDepthTooltip() {
			val depth = if (executionDepthButton.isSelected) "deepSimulation" else "shallowSimulation"
			executionDepthButton.toolTipText = Translations.getString("execution.action.$depth.name")
		}
		executionDepthButton.addItemListener { updateExecutionDepthTooltip() }
		updateExecutionDepthTooltip()

		isFloatable = false
		isRollover = true
		addSeparator()
		add(modeToggleButton)
		add(pauseOrResumeButton)
		add(singleStepModeButton)
		add(speedSlider)
		add(executionDepthButton)
		addGap()
		add(usecaseSelector)
	}

	fun dispose() {
		toggleApplicationModeWrapper.dispose()
		singleStepModeWrapper.dispose()
		pauseOrResumeButton.dispose()
		executionDepthWrapper.dispose()
		speedSlider.dispose()
		usecaseSelector.dispose()
	}
}
