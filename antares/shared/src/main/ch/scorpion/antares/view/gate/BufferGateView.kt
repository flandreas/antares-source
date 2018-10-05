package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.BufferGate
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke


/**
 * A view of a [BufferGate].
 */
class BufferGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    val currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
    bufferGate: BufferGate = BufferGate()
) : AbstractDigitalGateView<BufferGate>(styleProvider, "1", bufferGate) {

    init {
        modelExchanged(null)
    }

    var bitWidth: BitWidth
        get() = model!!.bitWidth
        set(value) {
            model?.bitWidth = value
        }

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawBufferGate(this, context, foregroundColor, backgroundColor, stroke)
        GateMnemonic.drawBuffer(this, context, foregroundColor)
    }
}