package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Resistor
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class ResistorView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Resistor = Resistor()
) : OrientableRectangularVerticeView<Resistor>(styleProvider, model) {

	@Suppress("unused") // Reflective bean property
	var resistance: Double
		get() = model.resistance
		set(value) {
			model.resistance = value
		}

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: Resistor?) {
		super.modelExchanged(oldModel)
		addPortView(AnalogPortView(styleProvider, model.getPort(1), -LENGTH, 0, Direction.EAST))
		addPortView(AnalogPortView(styleProvider, model.getPort(2), -LENGTH - SymbolStyle.RESISTOR_WIDTH.toInt(), 0, Direction.WEST))
		setBounds(
			-LENGTH.toDouble() - SymbolStyle.RESISTOR_WIDTH, -SymbolStyle.RESISTER_HEIGHT_HALF,
			SymbolStyle.RESISTOR_WIDTH, 2 * SymbolStyle.RESISTER_HEIGHT_HALF)
	}

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		AntaresViewModule.currentSymbolStyle.symbolStyle.drawResistor(
			this,
			context,
			context.chooseForeground(foregroundColor),
			context.chooseBackground(backgroundColor),
			SymbolStyle.RESISTOR_STROKE)
	}
}