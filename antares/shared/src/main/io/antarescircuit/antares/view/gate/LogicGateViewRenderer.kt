package io.antarescircuit.antares.view.gate

import io.antarescircuit.antares.model.InputPortNumber
import io.antarescircuit.antares.model.gate.NonUnaryLogicGate
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.module.AntaresViewModule.currentSymbolStyle
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle
import io.antarescircuit.jabbah.base.ui.UI
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.execution.speed.SystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.view.style.GraphTheme

interface LogicGateViewRenderer {

	/** The text to display inside the box with [SymbolStyle.EUROPEAN]. */
	val text: String

	fun drawShape(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	fun drawMnemonics(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color)
}

enum class LogicGateViewRenderers(
	override val text: String
): LogicGateViewRenderer {

	And("&") {
		override fun drawShape(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			currentSymbolStyle.symbolStyle.drawAndGate(logicGateView, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawMnemonics(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
			if (logicGateView.dataPort != InputPortNumber.NONE && LogicGateView.isDataFlowEnabled) {
				drawDataFlow(logicGateView, context)
			} else {
				GateMnemonic.drawAnd(logicGateView, context, foregroundColor, backgroundColor)
			}
		}

		private fun drawDataFlow(logicGateView: LogicGateView, context: DrawContext) {
			val dataPortView = logicGateView.getPortView(logicGateView.model.getInput<DigitalSignal>(logicGateView.dataPort.id))!!
			val outputPortView = logicGateView.getPortView(logicGateView.model.getOutput<DigitalSignal>())!!
			val appContext = context.castedAppContext<GraphApplicationContext>()!!

			if (appContext.mode.isExecute()) {
				val controlState = (logicGateView.model as NonUnaryLogicGate).calculate { it != logicGateView.dataPort.id }
				if (controlState.isAllOf(Bit.True)) {
					context.g.stroke = CLOSED_DATA_PATH_STROKE
				} else {
					context.g.stroke = OPEN_DATA_PATH_STROKE
				}
			} else {
				context.g.stroke = OPEN_DATA_PATH_STROKE
			}

			if (appContext.isExecute && showNetState(appContext.systemSpeedCategory.systemSpeedCategory)) {
				val signal = logicGateView.model.getOutput<DigitalSignal>().getOutgoingSignal()!!
				if (UI.isDark && signal.isAllOf(Bit.False)) {
					// The "false" signal color on dark background doesn't allow to distinguish data path state
					context.g.color = context.choose(Themes.get<GraphTheme>().edge.color).foregroundColor
				} else {
					context.g.color = signal.color.foregroundColor
				}
			} else {
				context.g.color = context.choose(Themes.get<GraphTheme>().edge.color).foregroundColor
			}

			context.g.drawLine(
				dataPortView.locationX, dataPortView.locationY,
				outputPortView.locationX, outputPortView.locationY)
		}
	},

	Nand("&") {
		override fun drawShape(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			currentSymbolStyle.symbolStyle.drawNandGate(logicGateView, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawMnemonics(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
			GateMnemonic.drawNand(logicGateView, context, foregroundColor, backgroundColor)
		}
	},

	Or("≥1") {
		override fun drawShape(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			currentSymbolStyle.symbolStyle.drawOrGate(logicGateView, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawMnemonics(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
			GateMnemonic.drawOr(logicGateView, context, foregroundColor, backgroundColor, -currentSymbolStyle.symbolStyle.orShapeConnectedPortViewLength)
		}
	},

	Nor("≥1") {
		override fun drawShape(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			currentSymbolStyle.symbolStyle.drawNorGate(logicGateView, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawMnemonics(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
			GateMnemonic.drawNor(logicGateView, context, foregroundColor, backgroundColor)
		}
	},

	Xor("=1") {
		override fun drawShape(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			currentSymbolStyle.symbolStyle.drawXorGate(logicGateView, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawMnemonics(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
			GateMnemonic.drawXor(logicGateView, context, foregroundColor, backgroundColor, -currentSymbolStyle.symbolStyle.orShapeConnectedPortViewLength)
		}
	},

	Xnor("=1") {
		override fun drawShape(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			currentSymbolStyle.symbolStyle.drawXnorGate(logicGateView, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawMnemonics(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
			GateMnemonic.drawXnor(logicGateView, context, foregroundColor, backgroundColor, -currentSymbolStyle.symbolStyle.orShapeConnectedPortViewLength)
		}
	},

	Not("1") {
		override fun drawShape(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			currentSymbolStyle.symbolStyle.drawNotGate(logicGateView, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawMnemonics(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
			GateMnemonic.drawNot(logicGateView, context, foregroundColor, backgroundColor)
		}
	},

	Buffer("1") {
		override fun drawShape(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			currentSymbolStyle.symbolStyle.drawBufferGate(logicGateView, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawMnemonics(logicGateView: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
			GateMnemonic.drawBuffer(logicGateView, context, foregroundColor, backgroundColor)
		}
	};

	companion object {

		private val OPEN_DATA_PATH_STROKE = Stroke(width = Themes.get<GraphTheme>().edge.stroke.width, dash = floatArrayOf(5.0f), dashPhase = 0f)

		private val CLOSED_DATA_PATH_STROKE = Stroke(width = Themes.get<GraphTheme>().edge.stroke.width)

		private fun showNetState(systemSpeedCategory: SystemSpeedCategory): Boolean =
			systemSpeedCategory > SystemSpeedCategory.Use
	}
}