package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Transistor
import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition

class TransistorView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Transistor = Transistor(),
	handedness: Handedness = DEFAULT_HANDEDNESS
) : AbstractTransistorView<Transistor>(styleProvider, model, handedness)
{
	constructor(type: TransistorType): this(model = Transistor(type), handedness = DEFAULT_HANDEDNESS)

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			model.bitWidth = value
		}

	init {
		modelExchanged(null)
		setBounds(AbstractAntaresPortView.LENGTH, -5 * SCALE, WIDTH, HEIGHT)
	}

	override fun modelExchanged(oldModel: Transistor?) {
		super.modelExchanged(oldModel)

		// Gate
		val gate = DigitalPortView(
			styleProvider,
			model.gatePort,
			0, 0,
			WEST,
			showLogicAnnotation = false
		)
		gate.portLabelPosition = PortLabelPosition.HIDE
		addPortView(gate)

		// Source
		val source = DigitalPortView(
			styleProvider,
			model.sourcePort,
			0, 0,
			NORTH
		)
		source.portLabelPosition = PortLabelPosition.HIDE
		addPortView(source)

		// Drain
		val drain = DigitalPortView(
			styleProvider,
			model.drainPort,
			0, 0,
			SOUTH
		)
		drain.portLabelPosition = PortLabelPosition.HIDE
		addPortView(drain)

		updateGeometry()
	}
}