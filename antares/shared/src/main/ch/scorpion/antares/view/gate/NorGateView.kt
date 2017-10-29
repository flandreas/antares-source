package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.NorGate
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.antares.view.truthtable.TruthTableView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawableExplanation
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke

/**
 * A view of a [NorGate].
 */
class NorGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
    norGate: NorGate = NorGate()
) : AbstractOrLikeGateView<NorGate>(styleProvider, currentSymbolStyle, "≥1", "library.element.NorGate", norGate) {

    private val explanation: DrawableExplanation by lazy {
        DrawableExplanation(
                TruthTableView(NorGate.TRUTH_TABLE, model!!),
                Point2D(boundingBox.centerX, boundingBox.minY))
    }

    override fun getExplanation(x: Double, y: Double): DrawableExplanation? {
        return if (model!!.inputCount == 2) explanation else null
    }

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawOrGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawNor(this, context, foregroundColor, backgroundColor)
    }
}