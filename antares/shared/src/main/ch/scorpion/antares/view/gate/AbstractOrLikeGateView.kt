package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.AbstractDigitalGate
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.port.PortView

/**
 *  Basic class for all OR-like gates whose [SymbolStyle.AMERICAN] have an OR-like shape.
 */
abstract class AbstractOrLikeGateView<T : AbstractDigitalGate>(
    styleProvider: StyleProvider,
    val currentSymbolStyle: CurrentSymbolStyle,
    text: String,
    baseResourceKey: String,
    gate: T
) : AbstractDigitalGateView<T>(styleProvider, text, baseResourceKey, gate) {

    init {
        modelExchanged(null)
    }

    override fun createInputPortView(inputPort: Port<DigitalSignal>, index: Int): PortView<*> {
        val portView = super.createInputPortView(inputPort, index) as DigitalPortView
        portView.predefinedConnectedLength = currentSymbolStyle.symbolStyle.getOrShapeConnectedPortViewLength(this, index)
        return portView
    }

    override val outsetLeft: Int
        get() = when(currentSymbolStyle.symbolStyle) {
            SymbolStyle.AMERICAN -> 2 * Look.SCALE
            SymbolStyle.EUROPEAN -> 0
        }

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