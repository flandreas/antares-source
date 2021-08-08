package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.app.ToolBar
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.execution.PauseExecutionAction
import ch.scorpion.jabbah.execution.ResumeExecutionAction
import ch.scorpion.jabbah.execution.SystemSpeedSliderSwing
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.app.ToggleApplicationModeAction
import ch.scorpion.jabbah.graph.ui.usecase.UsecaseSelector
import java.awt.Dimension
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JToggleButton

class ExecutionToolbarBuilder(
	private val scheduler: Scheduler,
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

		val pauseAction = PauseExecutionAction(scheduler, eventBus)
		val pauseToggleButton = JToggleButton(ActionWrapperSwing(pauseAction))
		pauseToggleButton.text = null
		pauseToggleButton.icon = UiUtil.themedIcon("/img/pause24.png")
		pauseToggleButton.toolTipText = pauseAction.name

		val speedSlider = SystemSpeedSliderSwing()
		speedSlider.maximumSize = Dimension(200, speedSlider.maximumSize.height)

		val usecaseSelector = UsecaseSelector(scheduler, applicationModeHolder)

		val mainToolBar = ToolBar()
		mainToolBar.isFloatable = false
		mainToolBar.isRollover = true
		mainToolBar.addSeparator()
		mainToolBar.add(modeToggleButton)
		mainToolBar.add(pauseToggleButton)
		mainToolBar.add(createStepButton(ResumeExecutionAction(scheduler, eventBus)))
		mainToolBar.add(speedSlider)
		mainToolBar.add(usecaseSelector)

		return mainToolBar
	}

	private fun createStepButton(action: Action): JButton {
		val inactiveIcon = UiUtil.themedIcon("/img/resume24.png")
		val activeIcon = ImageIcon(GraphPanelViewSwing::class.java.getResource("/img/resume-active24.png"))
		val button = JButton(ActionWrapperSwing(action))
		button.text = null
		button.icon = inactiveIcon

		action.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) {
				if (e.name == Action.PROP_ENABLED) {
					button.icon = if (action.enabled) {
						activeIcon
					} else {
						inactiveIcon
					}
				}
			}
		})

		return button
	}
}