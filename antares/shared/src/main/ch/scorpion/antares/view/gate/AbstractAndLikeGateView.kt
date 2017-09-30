package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.draw.style.StyleProvider

/**
 *  Basic class for all AND-like gates whose [SymbolStyle.AMERICAN] have an AND-like shape.
 */
abstract class AbstractAndLikeGateView<T : AbstractDigitalGate>(
        styleProvider: StyleProvider,
        val currentSymbolStyle: CurrentSymbolStyle,
        text: String,
        baseResourceKey: String,
        gate: T
) : AbstractDigitalGateView<T>(styleProvider, text, baseResourceKey, gate) {

    override val outsetTop: Int
        get() = when(currentSymbolStyle.symbolStyle) {
            SymbolStyle.AMERICAN -> -Look.SCALE
            SymbolStyle.EUROPEAN -> 0
        }

    override val outsetBottom: Int
        get() = when(currentSymbolStyle.symbolStyle) {
            SymbolStyle.AMERICAN -> -Look.SCALE
            SymbolStyle.EUROPEAN -> 0
        }
}