package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogTransistor
import io.antarescircuit.antares.model.net.TransistorType
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementProxy
import io.antarescircuit.antares.view.net.AbstractTransistorView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.jabbah.base.geom.Direction.*
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import kotlin.math.abs

class AnalogTransistorView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: AnalogTransistor = AnalogTransistor(),
	handedness: Handedness = DEFAULT_HANDEDNESS,
	private val analogElement: AnalogElementProxy = AnalogElementProxy()
) : AbstractTransistorView<AnalogTransistor>(styleProvider, model, handedness),
	AnalogElement by analogElement
{
	companion object {
		private const val MAX_SWITCH_OFF_DISPLACEMENT = 1.0 * SCALE
		private const val MAX_CONDUCTANCE = 0.07
	}

	constructor(type: TransistorType): this(model = AnalogTransistor(type), handedness = DEFAULT_HANDEDNESS)

	/** ---- UI properties */

	@Suppress("unused") // Reflection
	var gain: Double
		get() = model.gain
		set(value) {
			model.gain = value
		}

	init {
		modelExchanged(null)
		setBounds(AbstractAntaresPortView.LENGTH, -5 * SCALE, WIDTH, HEIGHT)
	}

	override val drawOnOff: Boolean get() = true

	override val switchOffDisplacement: Double get() =
		abs(MAX_CONDUCTANCE - model.conductance) / MAX_CONDUCTANCE * MAX_SWITCH_OFF_DISPLACEMENT.coerceIn(0.0, MAX_SWITCH_OFF_DISPLACEMENT)


	override fun modelExchanged(oldModel: AnalogTransistor?) {
		super.modelExchanged(oldModel)

		analogElement.bind(model)

		addPortView(AnalogPortView(styleProvider, model.gatePort, 0, 0, WEST, portLabelPosition,
			horizontalExternalLabel = true, externalPortLabelDistance = externalPortLabelDistance))
		addPortView(AnalogPortView(styleProvider, model.sourcePort, 0, 0, NORTH, portLabelPosition,
			horizontalExternalLabel = true, externalPortLabelDistance = externalPortLabelDistance))
		addPortView(AnalogPortView(styleProvider, model.drainPort, 0, 0, SOUTH, portLabelPosition,
			horizontalExternalLabel = true, externalPortLabelDistance = externalPortLabelDistance))

		updateGeometry()
	}
}