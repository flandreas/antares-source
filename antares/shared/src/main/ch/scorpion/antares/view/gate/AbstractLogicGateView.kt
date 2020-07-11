package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.StyleProvider

abstract class AbstractLogicGateView<T: AbstractDigitalGate>(
	styleProvider: StyleProvider,
	protected val currentSymbolStyle: CurrentSymbolStyle,
	text: String,
	gate: T
) : AbstractDigitalGateView<T>(styleProvider, text, gate), CustomShapeContent {

	override fun drawCustomShapeContent(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		drawMnemonics(context, foregroundColor, backgroundColor)
	}

	protected abstract fun drawMnemonics(context: DrawContext, foregroundColor: Color, backgroundColor: Color)
}