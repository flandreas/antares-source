package ch.scorpion.antares.view.oscilloscope

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.analog.AnalogOscilloscopeProbeVertice
import ch.scorpion.antares.model.analog.AnalogOscilloscopeSignalType
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import ch.scorpion.jabbah.graph.view.oscilloscope.OscilloscopeProbeVerticeView

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