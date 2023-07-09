package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.AbstractLogicGate
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.draw.style.StyleProvider

/**
 *  Basic class for all OR-like gates whose [SymbolStyle.AMERICAN] have an OR-like shape.
 */
abstract class AbstractOrLikeGateView<T : AbstractLogicGate>(
    styleProvider: StyleProvider,
    currentSymbolStyle: CurrentSymbolStyle,
    text: String,
    gate: T
) : AbstractLogicGateView<T>(styleProvider, currentSymbolStyle, text, gate) {

    init {
        modelExchanged(null)
    }

    override val outsetLeft: Int
        get() = when(currentSymbolStyle.symbolStyle) {
            SymbolStyle.AMERICAN -> 2 * Look.SCALE
            SymbolStyle.EUROPEAN -> 0
            SymbolStyle.VERBOSE -> 0
        }

    override val outsetTop: Int
        get() = when(currentSymbolStyle.symbolStyle) {
            SymbolStyle.AMERICAN -> -Look.SCALE
            SymbolStyle.EUROPEAN -> 0
            SymbolStyle.VERBOSE -> 0
        }

    override val outsetBottom: Int
        get() = when(currentSymbolStyle.symbolStyle) {
            SymbolStyle.AMERICAN -> -Look.SCALE
            SymbolStyle.EUROPEAN -> 0
            SymbolStyle.VERBOSE -> 0
        }
}