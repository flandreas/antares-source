package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.net.Transistor
import io.antarescircuit.antares.model.net.TransistorType
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.geom.Direction.*
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider

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

	override val drawOnOff: Boolean get() = true

	override val switchOffDisplacement: Double get() = 0.5 * SCALE

	override fun modelExchanged(oldModel: Transistor?) {
		super.modelExchanged(oldModel)

		addPortView(DigitalPortView(styleProvider, model.gatePort, 0, 0, WEST, portLabelPosition,
			showLogicAnnotation = false, horizontalExternalLabel = true, externalPortLabelDistance = externalPortLabelDistance))
		addPortView(DigitalPortView(styleProvider, model.sourcePort, 0, 0, NORTH, portLabelPosition,
			showLogicAnnotation = false, horizontalExternalLabel = true, externalPortLabelDistance = externalPortLabelDistance))
		addPortView(DigitalPortView(styleProvider, model.drainPort, 0, 0, SOUTH, portLabelPosition,
			showLogicAnnotation = false, horizontalExternalLabel = true, externalPortLabelDistance = externalPortLabelDistance))

		updateGeometry()
	}
}