package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogOscilloscopeProbeVertice
import io.antarescircuit.antares.model.analog.AnalogOscilloscopeSignalType
import io.antarescircuit.antares.model.analog.AnalogOscilloscopeSignalTypeEvent
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.drawable.RectangularDrawable
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.model.oscilloscope.OscilloscopeProbeVertice
import io.antarescircuit.jabbah.graph.view.oscilloscope.AbstractSignalHistoryYAxis

class AnalogSignalHistoryYAxis(
	private val port: Port<*>,
	topInset: Int = DEF_TOP_INSET,
	bottomInset: Int = DEF_BOTTOM_INSET,
	initDefaultValue: AnalogSignal = AnalogSignal.HIGH_VOLTAGE,
	defaultValueTopInset: Int = DEF_DEFAULT_VALUE_TO_INSET,
	color: CompositeColor = Themes.get<AntaresTheme>().figure.color,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractSignalHistoryYAxis<AnalogSignal>(topInset, bottomInset, initDefaultValue, defaultValueTopInset, color) {

	companion object {
		const val WIDTH = 40
	}

	private val signalTypeHandler: EventHandler<AnalogOscilloscopeSignalTypeEvent> = {
		if (it.source.getPort<AnalogSignal>().name == port.name) {
			updateDefaultValueFrom(it.newValue)
		}
	}

	init {
		eventBus.register(AnalogOscilloscopeSignalTypeEvent::class, signalTypeHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(signalTypeHandler)
	}

	/** ---- [RectangularDrawable] */

	override val lineWidth: Double get() = 0.0

	/** ---- [AbstractSignalHistoryYAxis] */

	override val preferredWidth: Int get() = WIDTH

	override fun toMetric(signal: AnalogSignal): Double = signal.dominantValue

	override fun loadedWith(probe: OscilloscopeProbeVertice<*>) {
		if (probe is AnalogOscilloscopeProbeVertice) {
			updateDefaultValueFrom(probe.signalType)
		}
	}

	private fun updateDefaultValueFrom(signalType: AnalogOscilloscopeSignalType) {
		defaultValue = when (signalType) {
			AnalogOscilloscopeSignalType.Voltage -> AnalogSignal.HIGH_VOLTAGE
			AnalogOscilloscopeSignalType.Current -> AnalogSignal.DEFAULT_CURRENT
		}
	}
}