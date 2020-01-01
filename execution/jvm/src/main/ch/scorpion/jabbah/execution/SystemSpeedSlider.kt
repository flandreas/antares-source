package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion. jabbah.execution.speed.SystemSpeedCategoryEvent
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*

/**
 * A [JSlider] that allows to change the current [SystemSpeed] and displays the current [SystemSpeedCategory].
 */
class SystemSpeedSlider(
    systemSpeed: SystemSpeed = BaseModule.systemSpeed,
    currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
    eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

    /** Displays the name of the current [SystemSpeedCategory].*/
    private val label = JLabel(currentSystemSpeedCategory.systemSpeedCategory.toString(), JLabel.CENTER)

    /** The [JSlider] that allows the use to change the [SystemSpeed].*/
    private val slider = JSlider(JSlider.HORIZONTAL, SystemSpeed.MIN_SPEED, SystemSpeed.MAX_SPEED, systemSpeed.speed)

    init {
        eventBus.register(SystemSpeedCategoryEvent::class) {
	        label.text = it.newValue.toString()
	        slider.toolTipText = "${Translations.getString("execution.action.speed.name")}: ${it.newValue}"
        }

	    slider.toolTipText = Translations.getString("execution.action.speed.name")
        slider.paintLabels = false
        slider.paintTicks = true
        slider.majorTickSpacing = 33
        slider.addChangeListener { systemSpeed.speed = slider.value }

        //buildUI()
	    buildUIWithoutLabel()
    }

    private fun buildUI() {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        label.alignmentX = Component.CENTER_ALIGNMENT
        slider.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)

        add(label)
        add(slider)
    }

	private fun buildUIWithoutLabel() {
		layout = BorderLayout(0, 0)
		add(slider, BorderLayout.CENTER)
	}
}