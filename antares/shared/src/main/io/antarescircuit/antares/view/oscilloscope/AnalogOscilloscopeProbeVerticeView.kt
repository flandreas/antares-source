package io.antarescircuit.antares.view.oscilloscope

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.analog.AnalogOscilloscopeProbeVertice
import io.antarescircuit.antares.model.analog.AnalogOscilloscopeSignalType
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import io.antarescircuit.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView

class AnalogOscilloscopeProbeVerticeView(
    name: String = "",
    color: CompositeColor = CompositeColor(),
    model: AnalogOscilloscopeProbeVertice = OscilloscopeProbeVertice.create<AnalogSignal>(name, AntaresGraphTypes.Analog) as AnalogOscilloscopeProbeVertice,
    dragGhost: Boolean = false,
    styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : OscilloscopeProbeVerticeView<AnalogSignal>(name, AntaresGraphTypes.Analog, color, model, dragGhost, styleProvider) {

    private val analogModel: AnalogOscilloscopeProbeVertice get() = model as AnalogOscilloscopeProbeVertice

    /** ---- UI properties */

    @Suppress("unused") // Reflection
    var signalType: AnalogOscilloscopeSignalType
        get() = analogModel.signalType
        set(value) {
            analogModel.signalType = value
            update()
        }
}