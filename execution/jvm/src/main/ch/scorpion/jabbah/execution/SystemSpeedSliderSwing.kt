package ch.scorpion.jabbah.execution

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategoryEvent
import java.awt.BorderLayout
import javax.swing.*

/**
 * A [JSlider] that allows to change a [SystemSpeed] and displays the current [SystemSpeedCategory].
 */
class SystemSpeedSliderSwing(
    systemSpeed: SystemSpeed,
    private val eventBus: EventBus = BaseModule.eventBus
) : JPanel() {

    /** The [JSlider] that allows the use to change the [SystemSpeed].*/
    private val slider = JSlider(JSlider.HORIZONTAL, SystemSpeed.MIN_SPEED, SystemSpeed.MAX_SPEED, systemSpeed.speed)

	private val systemSpeedCategoryHandler: EventHandler<SystemSpeedCategoryEvent> = {
		slider.toolTipText = "${Translations.getString("execution.action.speed.name")}: ${it.newValue}"
	}

    init {
	    eventBus.register(SystemSpeedCategoryEvent::class, systemSpeedCategoryHandler)

	    slider.toolTipText = Translations.getString("execution.action.speed.name")
        slider.paintLabels = false
        slider.paintTicks = true
        slider.majorTickSpacing = 33
	    slider.minorTickSpacing = 3
	    slider.snapToTicks = true
        slider.addChangeListener { systemSpeed.speed = slider.value }

	    buildUIWithoutLabel()
    }

	fun dispose() {
		eventBus.unregister(systemSpeedCategoryHandler)
	}

	private fun buildUIWithoutLabel() {
		layout = BorderLayout(0, 0)
		add(slider, BorderLayout.CENTER)
	}
}