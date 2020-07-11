package ch.scorpion.antares.view.symbolstyle

import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.gate.BoxGateView
import ch.scorpion.antares.view.gate.AbstractOrLikeGateView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
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
			drawEuropean(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawOrGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropean(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawXorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropean(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawXnorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropean(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawNotGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropean(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawBufferGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropean(gate, context, foregroundColor, backgroundColor, stroke)
		}
	},

	AMERICAN("ANSI") {
		override fun drawAndGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmerican(gate, AND_PATH, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawOrGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmerican(gate, OR_PATH, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawXorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmerican(gate, gate.x, gate.y, gate.bounds.height, OR_PATH, context, foregroundColor, backgroundColor, stroke, true, gate.transparency)
		}

		override fun drawXnorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmerican(gate, gate.x, gate.y, gate.bounds.height, OR_PATH, context, foregroundColor, backgroundColor, stroke, true, gate.transparency)
		}

		override fun drawNotGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmerican(gate, NOT_PATH, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawBufferGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmerican(gate, NOT_PATH, context, foregroundColor, backgroundColor, stroke)
		}

		override val orShapeConnectedPortViewLength: Int get() = (2 * Look.SCALE * 0.35).toInt()
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
			.moveTo(0, Look.SCALE)
			.lineTo(3 * Look.SCALE, Look.SCALE)
			.quadTo(6 * Look.SCALE, Look.SCALE, 6 * Look.SCALE, 4 * Look.SCALE)
			.quadTo(6 * Look.SCALE, 7 * Look.SCALE, 3 * Look.SCALE, 7 * Look.SCALE)
			.lineTo(0, 7 * Look.SCALE)
			.close()

		val OR_PATH = System.createPath()
			.moveTo(-Look.SCALE, Look.SCALE)
			.lineTo(Look.SCALE, Look.SCALE)
			.quadTo(4 * Look.SCALE, Look.SCALE, 6 * Look.SCALE, 4 * Look.SCALE)
			.quadTo(4 * Look.SCALE, 7 * Look.SCALE, Look.SCALE, 7 * Look.SCALE)
			.lineTo(-Look.SCALE, 7 * Look.SCALE)
			.quadTo(0.5 * Look.SCALE, 4.0 * Look.SCALE, -Look.SCALE.toDouble(), Look.SCALE.toDouble())
			.close()

		private val EXCLUSIVE_PATH = System.createPath()
			.moveTo(-Look.SCALE - EXCLUSIVE_OFFSET, 7 * Look.SCALE.toDouble())
			.quadTo(0.5 * Look.SCALE - EXCLUSIVE_OFFSET, 4.0 * Look.SCALE, -Look.SCALE - EXCLUSIVE_OFFSET, Look.SCALE.toDouble())

		val NOT_PATH = System.createPath()
			.moveTo(0, Look.SCALE)
			.lineTo(6 * Look.SCALE, 4 * Look.SCALE)
			.lineTo(0, 7 * Look.SCALE)
			.lineTo(0, Look.SCALE)
			.close()

		private fun drawEuropean(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			gate.drawEuropeanShape(context, foregroundColor, backgroundColor, stroke)
		}

		fun drawAmerican(gate: BoxGateView<*>, path: Path, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmerican(gate, gate.x, gate.y, gate.bounds.height, path, context, foregroundColor, backgroundColor, stroke, false, gate.transparency)
		}

		fun drawAmerican(comp: Component, x: Double, y: Double, height: Double, path: Path, context: DrawContext, foregroundColor: Color,
		                 backgroundColor: Color, stroke: Stroke, exclusive: Boolean, transparency: Int) {

			val vOffset = (height - 2 * Look.SCALE - path.boundingBox.height) / 2

			if (vOffset > 0) {
				context.g.color = foregroundColor
				context.g.drawLine(
					x.toInt(), (y + Look.SCALE).toInt(),
					x.toInt(), (y + height - Look.SCALE).toInt())
			}

			context.g.translate(x, y + vOffset)

			if (comp.shadow) {
				DropShadow.draw(context, transparency) {
					context.g.fill(path)
				}
			}

			context.g.color = if (Look.FILL_BASIC_COMPONENTS) backgroundColor else DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
			context.g.fill(path)
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

}