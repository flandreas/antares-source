package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.OrGate
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
 * A view of a [OrGate].
 */
class OrGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
    orGate: OrGate = OrGate()
) : AbstractOrLikeGateView<OrGate>(styleProvider, currentSymbolStyle, "≥1", "library.element.OrGate", orGate) {

    companion object {
        private val EXPLANATION: DrawableExplanation<TruthTableView> = DrawableExplanation(
                TruthTableView(OrGate.TRUTH_TABLE, null), Point2D())
    }

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawOrGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawOr(this, context, foregroundColor, backgroundColor)
    }

    override fun getExplanation(x: Double, y: Double): DrawableExplanation<*>? {
        return if (model!!.inputCount == 2) {
            EXPLANATION.explanation.vertice = model
            EXPLANATION.location = Point2D(boundingBox.centerX, boundingBox.minY)
            EXPLANATION
        } else null
    }
}