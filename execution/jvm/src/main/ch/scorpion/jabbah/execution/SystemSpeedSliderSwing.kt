package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategoryEvent
import java.awt.BorderLayout
import javax.swing.*

/**
 * A [JSlider] that allows to change the current [SystemSpeed] and displays the current [SystemSpeedCategory].
 */
class SystemSpeedSliderSwing(
    systemSpeed: SystemSpeed = BaseModule.systemSpeed,
    eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

    /** The [JSlider] that allows the use to change the [SystemSpeed].*/
    private val slider = JSlider(JSlider.HORIZONTAL, SystemSpeed.MIN_SPEED, SystemSpeed.MAX_SPEED, systemSpeed.speed)

    init {
        eventBus.register(SystemSpeedCategoryEvent::class) {
	        slider.toolTipText = "${Translations.getString("execution.action.speed.name")}: ${it.newValue}"
        }

	    slider.toolTipText = Translations.getString("execution.action.speed.name")
        slider.paintLabels = false
        slider.paintTicks = true
        slider.majorTickSpacing = 33
        slider.addChangeListener { systemSpeed.speed = slider.value }

	    buildUIWithoutLabel()
    }

	private fun buildUIWithoutLabel() {
		layout = BorderLayout(0, 0)
		add(slider, BorderLayout.CENTER)
	}
}