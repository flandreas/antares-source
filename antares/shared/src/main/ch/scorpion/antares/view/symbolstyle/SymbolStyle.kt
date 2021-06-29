package ch.scorpion.antares.view.symbolstyle

import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.gate.BoxGateView
import ch.scorpion.antares.view.gate.AbstractOrLikeGateView
import ch.scorpion.antares.view.gate.CustomShapeContent
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.Paint
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component

/**
 * [SymbolStyle] represents international standards for drawing digital gates.
 * The user can globally switch between the supported [SymbolStyle]s.
 */
enum class SymbolStyle(val customName: String) {

	EUROPEAN("IEC") {

		override val orShapeConnectedPortViewLength: Int get() = 0

		override fun drawAndGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawOrGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawXorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawXnorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawNotGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawBufferGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawResistor(resistor: DigitalComponentView<*>, context: DrawContext, foregroundColor: Paint, backgroundColor: Color, stroke: Stroke) {
			if (resistor.shadow) {
				DropShadow.draw(context, transparency = resistor.transparency) {
					context.g.fillRect(
						-DigitalPortView.LENGTH.toDouble() - RESISTOR_WIDTH, -RESISTER_HEIGHT_HALF,
						RESISTOR_WIDTH, 2 * RESISTER_HEIGHT_HALF
					)
				}
			}

			context.g.color = if (Look.FILL_BASIC_COMPONENTS) backgroundColor else DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
			context.g.fillRect(
				-DigitalPortView.LENGTH.toDouble() - RESISTOR_WIDTH, -RESISTER_HEIGHT_HALF,
				RESISTOR_WIDTH, 2 * RESISTER_HEIGHT_HALF
			)

			context.g.paint = foregroundColor
			context.g.stroke = stroke
			context.g.drawRect(
				-DigitalPortView.LENGTH.toDouble() - RESISTOR_WIDTH, -RESISTER_HEIGHT_HALF,
				RESISTOR_WIDTH, 2 * RESISTER_HEIGHT_HALF
			)
		}

		private fun drawEuropeanGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			gate.drawBoxShape(context, foregroundColor, backgroundColor, stroke)
		}
	},

	AMERICAN("ANSI") {
		override fun drawAndGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, AND_PATH, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawOrGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, OR_PATH, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawXorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, gate.x, gate.y, gate.bounds.height, OR_PATH, context, foregroundColor, backgroundColor, stroke, true, gate.transparency)
		}

		override fun drawXnorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, gate.x, gate.y, gate.bounds.height, OR_PATH, context, foregroundColor, backgroundColor, stroke, true, gate.transparency)
		}

		override fun drawNotGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, NOT_PATH, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawBufferGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, NOT_PATH, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawResistor(resistor: DigitalComponentView<*>, context: DrawContext, foregroundColor: Paint, backgroundColor: Color, stroke: Stroke) {
			context.g.paint = foregroundColor
			context.g.stroke = stroke
			context.g.draw(RESISTOR_PATH)
		}

		override val orShapeConnectedPortViewLength: Int get() = (2 * SCALE * 0.35).toInt()
	};

	companion object {

		/** The name of the [String] property in [Properties] designating the [SymbolStyle]'s name. */
		const val PROP_SYMBOL_STYLE = "ch.scorpion.antares.view.symbolStyle"

		private const val EXCLUSIVE_OFFSET = 6.0

		fun withName(customName: String): SymbolStyle {
			for (symbolStyle in values()) {
				if (symbolStyle.customName == customName) {
					return symbolStyle
				}
			}
			throw IllegalArgumentException("Unknown SymbolStyle $customName")
		}

		val AND_PATH = System.createPath()
			.moveTo(0, SCALE)
			.lineTo(3 * SCALE, SCALE)
			.quadTo(6 * SCALE, SCALE, 6 * SCALE, 4 * SCALE)
			.quadTo(6 * SCALE, 7 * SCALE, 3 * SCALE, 7 * SCALE)
			.lineTo(0, 7 * SCALE)
			.close()

		val OR_PATH = System.createPath()
			.moveTo(-SCALE, SCALE)
			.lineTo(SCALE, SCALE)
			.quadTo(4 * SCALE, SCALE, 6 * SCALE, 4 * SCALE)
			.quadTo(4 * SCALE, 7 * SCALE, SCALE, 7 * SCALE)
			.lineTo(-SCALE, 7 * SCALE)
			.quadTo(0.5 * SCALE, 4.0 * SCALE, -SCALE.toDouble(), SCALE.toDouble())
			.close()

		private val EXCLUSIVE_PATH = System.createPath()
			.moveTo(-SCALE - EXCLUSIVE_OFFSET, 7 * SCALE.toDouble())
			.quadTo(0.5 * SCALE - EXCLUSIVE_OFFSET, 4.0 * SCALE, -SCALE - EXCLUSIVE_OFFSET, SCALE.toDouble())

		val NOT_PATH = System.createPath()
			.moveTo(0, SCALE)
			.lineTo(6 * SCALE, 4 * SCALE)
			.lineTo(0, 7 * SCALE)
			.lineTo(0, SCALE)
			.close()

		const val RESISTOR_WIDTH = 6.0 * SCALE.toDouble()
		const val RESISTER_HEIGHT_HALF = SCALE.toDouble()
		private val RESISTOR_PATH= System.createPath()
			.moveTo(-DigitalPortView.LENGTH, 0)
			.lineTo(-DigitalPortView.LENGTH - 0.5 * SCALE, RESISTER_HEIGHT_HALF)
			.lineTo(-DigitalPortView.LENGTH - 1.5 * SCALE, -RESISTER_HEIGHT_HALF)
			.lineTo(-DigitalPortView.LENGTH - 2.5 * SCALE, RESISTER_HEIGHT_HALF)
			.lineTo(-DigitalPortView.LENGTH - 3.5 * SCALE, -RESISTER_HEIGHT_HALF)
			.lineTo(-DigitalPortView.LENGTH - 4.5 * SCALE, RESISTER_HEIGHT_HALF)
			.lineTo(-DigitalPortView.LENGTH - 5.5 * SCALE, -RESISTER_HEIGHT_HALF)
			.lineTo(-DigitalPortView.LENGTH - 6.0 * SCALE, 0.0)

		fun drawAmericanGate(gate: BoxGateView<*>, path: Path, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, gate.x, gate.y, gate.bounds.height, path, context, foregroundColor, backgroundColor, stroke, false, gate.transparency)
		}

		fun drawAmericanGate(comp: Component, x: Double, y: Double, height: Double, path: Path, context: DrawContext, foregroundColor: Color,
		                     backgroundColor: Color, stroke: Stroke, exclusive: Boolean, transparency: Int) {

			val vOffset = (height - 2 * SCALE - path.boundingBox.height) / 2

			if (vOffset > 0) {
				context.g.color = foregroundColor
				context.g.drawLine(
					x.toInt(), (y + SCALE).toInt(),
					x.toInt(), (y + height - SCALE).toInt())
			}

			context.g.translate(x, y + vOffset)

			if (comp.shadow) {
				DropShadow.draw(context, transparency) {
					context.g.fill(path)
				}
			}

			context.g.color = if (Look.FILL_BASIC_COMPONENTS) backgroundColor else DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
			context.g.fill(path)

			if (comp is CustomShapeContent) {
				context.g.translate(-x, -(y + vOffset))
				comp.drawCustomShapeContent(context, foregroundColor, backgroundColor)
				context.g.translate(x, y + vOffset)
			}

			context.g.color = foregroundColor
			context.g.stroke = stroke
			context.g.draw(path)

			if (exclusive) {
				context.g.draw(EXCLUSIVE_PATH)
			}

			context.g.translate(-x, -y - vOffset)
		}
	}

	/**
	 * Returns the length of OR-shaped input [PortView]s, which is used to adjust lines that lead to the [PortView] when using
	 * roundly shaped borders (especially with american symbol style). Applicable only for [AbstractOrLikeGateView]s with 2 inputs.
	 */
	abstract val orShapeConnectedPortViewLength: Int

	abstract fun drawAndGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawOrGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawXorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawXnorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawNotGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawBufferGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawResistor(resistor: DigitalComponentView<*>, context: DrawContext, foregroundColor: Paint, backgroundColor: Color, stroke: Stroke)

}