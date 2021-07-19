package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.InputPortNumber
import ch.scorpion.antares.model.gate.AndGate
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.symbolstyle.CurrentSymbolStyle
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.min

/** A view of an [AndGate]. */
class AndGateView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	currentSymbolStyle: CurrentSymbolStyle = AntaresViewModule.currentSymbolStyle,
	andGate: AndGate = AndGate()
) : AbstractAndLikeGateView<AndGate>(styleProvider, currentSymbolStyle, "&", andGate) {

	companion object {

		/** The name of the [Boolean] property in [Properties] defining whether the data flow feature is enabled.*/
		const val PROP_DATA_FLOW_ENABLED = "antares.andGate.dataFlow"
	}

	var dataPort: InputPortNumber = InputPortNumber.NONE
		set(value) {
			if (field == value) {
				return
			}
			checkArgument(value.id <= model.chosenInputCount.count, "InputPortNumber must not be larger than InputCount")
			invalidate()
			field = value
			labelStyle = if (dataPort == InputPortNumber.NONE) LabelStyle.LARGE_CENTERED else LabelStyle.SMALL_UPPER_LEFT
			invalidate()
			update()
		}

	private val isDataFlowEnabled: Boolean get() = BaseModule.properties.getBoolean(PROP_DATA_FLOW_ENABLED)

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: AndGate?) {
		super.modelExchanged(oldModel)
		dataPort = InputPortNumber.withId(min(dataPort.id, model.chosenInputCount.count))
	}

	override fun drawShape(context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
		currentSymbolStyle.symbolStyle.drawAndGate(this, context, foregroundColor, backgroundColor, stroke)
	}

	override fun drawMnemonics(context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
		if (dataPort != InputPortNumber.NONE && isDataFlowEnabled) {
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
		val dataPortView = getPortView(model.getInput<DigitalSignal>(dataPort.id))!!
		val outputPortView = getPortView(model.getOutput<DigitalSignal>())!!
		val appContext = context.castedAppContext<GraphApplicationContext>()!!

		if (appContext.mode.isExecute()) {
			val controlState = model.calculate { it != dataPort.id }
			if (controlState.isAllOf(Bit.True)) {
				context.g.stroke = createClosedDataPathStroke()
			} else {
				context.g.stroke = createOpenDataPathStroke()
			}

		} else {
			context.g.stroke = createOpenDataPathStroke()
		}

		if (appContext.isExecute && showNetState(appContext.systemSpeedCategory.systemSpeedCategory)) {
			context.g.color = model.getOutput<DigitalSignal>().getOutgoingSignal()!!.color.foregroundColor
		} else {
			context.g.color = context.choose(Themes.get<GraphTheme>().edge.color).foregroundColor
		}

		context.g.drawLine(
			dataPortView.locationX, dataPortView.locationY,
			outputPortView.locationX, outputPortView.locationY)
	}

	// TODO Refactor: Use [StyleProvider] instead of [Themes] to access style information

	private fun createOpenDataPathStroke() = Stroke(width = Themes.get<GraphTheme>().edge.stroke.width, dash = floatArrayOf(5.0f), dashPhase = 0f)

	private fun createClosedDataPathStroke() = Stroke(width = Themes.get<GraphTheme>().edge.stroke.width)

	// TODO Refactor (DRY): Same logic as in [AbstractNetViewElement]
	private fun showNetState(systemSpeedCategory: SystemSpeedCategory): Boolean {
		return systemSpeedCategory > SystemSpeedCategory.Use
	}
}