package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AbstractAnalogBouncingSwitch
import io.antarescircuit.antares.view.input.AbstractSwitchView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider

abstract class AbstractAnalogBouncingSwitchView<T: AbstractAnalogBouncingSwitch<T>>(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: T
) : AbstractSwitchView<T>(styleProvider, model) {

    override val relativeExternalLabelLocation: Point2D get() =
        Point2D(LENGTH + REAL_SWITCH_WIDTH / 2, -REAL_SWITCH_HEIGHT_ABOVE - LABEL_DIST)

    @Suppress("unused") // Reflection
    var bounce: Boolean
        get() = model.bounce
        set(value) {
            model.bounce = value
        }

    @Suppress("unused") // Reflection
    var bounceDuration: Long
        get() = model.bounceDuration
        set(value) {
            model.bounceDuration = value
        }

    override fun getEditPortViewColor(styleProvider: StyleProvider): CompositeColor =
        customColor?.color ?: super.getEditPortViewColor(styleProvider)
}