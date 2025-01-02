package ch.scorpion.antares.view.analog

import ch.scorpion.jabbah.base.Disposable
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import java.awt.Dimension
import javax.swing.*

/**
 * Contains a [JSlider] (preceded by a label) the user can use to change [CurrentFlowAnimationSpeed] during simulation.
 */
class CurrentFlowAnimationSpeedSlider(
    private val currentFlowAnimationSpeed: CurrentFlowAnimationSpeed = CurrentFlowAnimationSpeed,
    private val eventBus: EventBus = BaseModule.eventBus
) : JPanel(), Disposable {

    private val slider = JSlider(JSlider.HORIZONTAL, CurrentFlowAnimationSpeed.MIN_SPEED, CurrentFlowAnimationSpeed.MAX_SPEED, CurrentFlowAnimationSpeed.speed)

    private val currentFlowAnimationSpeedHandler: EventHandler<CurrentFlowAnimationSpeedEvent> = {
        if (it.newSpeed != slider.value) {
            slider.value = it.newSpeed
        }
    }

    init {
        eventBus.register(CurrentFlowAnimationSpeedEvent::class, currentFlowAnimationSpeedHandler)

        slider.maximumSize = Dimension(200, slider.preferredSize.height)

        buildUI()
        slider.addChangeListener { currentFlowAnimationSpeed.speed = slider.value }
    }

    override fun dispose() {
        eventBus.unregister(currentFlowAnimationSpeedHandler)
    }

    private fun buildUI() {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        add(JLabel("${Translations.getString("antares.analog.currentFlowAnimFactor")}:"))
        add(slider)
        add(Box.createGlue())
    }
}