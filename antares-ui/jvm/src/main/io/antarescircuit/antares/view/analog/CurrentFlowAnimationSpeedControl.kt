package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.view.analog.CurrentFlowAnimationSpeed.MAX_SPEED
import io.antarescircuit.antares.view.analog.CurrentFlowAnimationSpeed.MIN_SPEED
import io.antarescircuit.jabbah.base.Disposable
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import java.awt.Dimension
import javax.swing.*
import kotlin.math.min

/**
 * Contains a [JSlider] (preceded by a label) the user can use to change [CurrentFlowAnimationSpeed] during simulation.
 */
class CurrentFlowAnimationSpeedControl(
    private val currentFlowAnimationSpeed: CurrentFlowAnimationSpeed = CurrentFlowAnimationSpeed,
    private val eventBus: EventBus = BaseModule.eventBus
) : JPanel(), Disposable {

    private val slider = JSlider(
        JSlider.HORIZONTAL,
        MIN_SPEED,
        MAX_SPEED,
        min(MAX_SPEED, CurrentFlowAnimationSpeed.speed)
    )

    private val checkbox = JCheckBox(
        Translations.getString("antares.analog.currentFlowAnimFactor.turbo.name"),
        CurrentFlowAnimationSpeed.turbo
    )

    private val currentFlowAnimationSpeedHandler: EventHandler<CurrentFlowAnimationSpeedEvent> = {
        if (it.newSpeed != slider.value) {
            slider.value = it.newSpeed
        }
    }

    init {
        eventBus.register(CurrentFlowAnimationSpeedEvent::class, currentFlowAnimationSpeedHandler)

        slider.maximumSize = Dimension(200, slider.preferredSize.height)

        buildUI()
        slider.addChangeListener { update() }
        checkbox.addActionListener { update() }

        checkbox.toolTipText = Translations.getString("antares.analog.currentFlowAnimFactor.turbo.desc")
    }

    private fun update() {
        currentFlowAnimationSpeed.speed = slider.value
        currentFlowAnimationSpeed.turbo = checkbox.isSelected
    }

    override fun dispose() {
        eventBus.unregister(currentFlowAnimationSpeedHandler)
    }

    private fun buildUI() {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        add(JLabel("${Translations.getString("antares.analog.currentFlowAnimFactor")}:"))
        add(slider)
        add(Box.createHorizontalStrut(15))
        add(checkbox)
        add(Box.createGlue())
    }
}