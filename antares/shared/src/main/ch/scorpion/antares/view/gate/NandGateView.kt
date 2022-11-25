package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.NandGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

/** A view of an [NandGate].*/
class NandGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
    nandGate: NandGate = NandGate()
) : AbstractAndLikeGateView<NandGate>(styleProvider, currentSymbolStyle, "&", nandGate) {

    init {
        modelExchanged(null)
    }

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawNandGate(this, context, foregroundColor, backgroundColor, stroke)
    }

	override fun drawMnemonics(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		GateMnemonic.drawNand(this, context, foregroundColor, backgroundColor)
	}
}