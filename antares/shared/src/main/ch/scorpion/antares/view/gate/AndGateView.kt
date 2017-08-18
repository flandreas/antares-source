package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.InputPortNumber
import ch.scorpion.antares.model.gate.AndGate
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable

/**
 * A view of an [AndGate].
 */
class AndGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    val currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
    andGate: AndGate = AndGate()
) : AbstractDigitalGateView<AndGate>(styleProvider, "&", "library.element.AndGate", andGate) {

    companion object {
        // TODO Refactor: Use [StyleProvider] instead of [Themes] to access style information
        private val CLOSED_DATA_PATH_STROKE = Stroke(width = Themes.get<GraphTheme>().edge.stroke.width)
        private val OPEN_DATA_PATH_STROKE = Stroke(width = Themes.get<GraphTheme>().edge.stroke.width, dash = floatArrayOf(5.0f), dashPhase = 0f)
        private val DATA_PATH_COLOR = Themes.get<GraphTheme>().edge.color
    }

    var dataPort: InputPortNumber = InputPortNumber.NONE
        set(value) {
            if (field == value) {
                return
            }
            checkArgument(value.id <= model!!.chosenInputCount.count, "InputPortNumber must not be larger than InputCount")
            invalidate()
            field = value
            invalidate()
            update()
        }

    init {
        modelExchanged(null)
    }

    override fun modelExchanged(oldModel: AndGate?) {
        super.modelExchanged(oldModel)
        if (model != null) {
            dataPort = InputPortNumber.withId(Math.min(dataPort.id, model!!.chosenInputCount.count))
        }
    }

    override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
        currentSymbolStyle.symbolStyle.drawAndGate(this, context, foregroundColor, backgroundColor, stroke)
        if (dataPort != InputPortNumber.NONE) {
            drawDataFlow(context)
        } else {
            GateMnemonic.drawAnd(this, context, foregroundColor, backgroundColor)
        }
    }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        if (dataPort != InputPortNumber.NONE) {
            writer.writeInt("dataPort", dataPort.id)
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("dataPort")) {
            dataPort = InputPortNumber.withId(reader.readInt("dataPort"))
        }
    }

    /** ---- [AndGateView] */

    private fun drawDataFlow(context: DrawContext) {
        val dataPortView = getPortView(model!!.getInput<DigitalSignal>(dataPort.id))!!
        val outputPortView = getPortView(model!!.getOutput<DigitalSignal>())!!
        val appContext = context.castedAppContext<GraphApplicationContext>()!!

        if (ApplicationMode.EXECUTE == appContext.mode) {
            val controlState = model!!.calculate { it.portId != dataPort.id }
            if (controlState.isSet) {
                context.g.stroke = CLOSED_DATA_PATH_STROKE
            } else {
                context.g.stroke = OPEN_DATA_PATH_STROKE
            }

        } else {
            context.g.stroke = OPEN_DATA_PATH_STROKE
        }

        if (ApplicationMode.EXECUTE == appContext.mode && showNetState(appContext.systemSpeedCategory.systemSpeedCategory)) {
            context.g.color = model!!.getOutput<DigitalSignal>().getOutgoingSignal()!!.getColor().foregroundColor
        } else {
            context.g.color = DATA_PATH_COLOR.foregroundColor
        }

        context.g.drawLine(
            dataPortView.locationX, dataPortView.locationY,
            outputPortView.locationX, outputPortView.locationY)
    }

    // TODO Refactor (DRY): Same logic as in [AbstractNetViewElement]
    private fun showNetState(systemSpeedCategory: SystemSpeedCategory): Boolean {
        return systemSpeedCategory > SystemSpeedCategory.Use
    }
}