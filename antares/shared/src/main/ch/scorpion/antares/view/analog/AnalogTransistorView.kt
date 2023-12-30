package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogTransistor
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementProxy
import ch.scorpion.antares.view.net.AbstractTransistorView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition.HIDE
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
		setBounds(AbstractAntaresPortView.LENGTH, -5 * Look.SCALE, WIDTH, HEIGHT)
	}

	override val drawOnOff: Boolean get() = true

	override val switchOffDisplacement: Double get() =
		abs(MAX_CONDUCTANCE - model.conductance) / MAX_CONDUCTANCE * MAX_SWITCH_OFF_DISPLACEMENT.coerceIn(0.0, MAX_SWITCH_OFF_DISPLACEMENT)


	override fun modelExchanged(oldModel: AnalogTransistor?) {
		super.modelExchanged(oldModel)

		analogElement.bind(model)

		addPortView(AnalogPortView(styleProvider, model.gatePort, 0, 0, WEST, HIDE))
		addPortView(AnalogPortView(styleProvider, model.sourcePort, 0, 0, NORTH, HIDE))
		addPortView(AnalogPortView(styleProvider, model.drainPort, 0, 0, SOUTH, HIDE))

		updateGeometry()
	}
}