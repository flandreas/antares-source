package ch.scorpion.antares.view.symbolstyle

import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.gate.BoxGateView
import ch.scorpion.antares.view.gate.CustomShapeContent
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.Style
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.style.GraphTheme

/**
 * [SymbolStyle] represents international standards for drawing digital gates.
 * The user can globally switch between the supported [SymbolStyle]s.
 */
enum class SymbolStyle(
	override val customName: String
) : EnumProperty<SymbolStyle> {

	EUROPEAN("IEC") {

		override val orShapeConnectedPortViewLength: Int get() = 0

		override fun drawAndGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawNandGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawOrGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawNorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
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

		override fun drawResistor(resistor: OrientableRectangularVerticeView<*>, isVariable: Boolean, context: DrawContext, foregroundColor: Paint, backgroundColor: Color, stroke: Stroke) {
			if (resistor.shadow) {
				DropShadow.draw(context, transparency = resistor.transparency) {
					context.g.fillRect(
						-LENGTH.toDouble() - RESISTOR_WIDTH, -RESISTER_HEIGHT_HALF,
						RESISTOR_WIDTH, 2 * RESISTER_HEIGHT_HALF
					)
				}
			}

			context.g.color = context.chooseBackground(if (Look.FILL_BASIC_COMPONENTS) backgroundColor else DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor)
			context.g.fillRect(
				-LENGTH.toDouble() - RESISTOR_WIDTH, -RESISTER_HEIGHT_HALF,
				RESISTOR_WIDTH, 2 * RESISTER_HEIGHT_HALF
			)

			context.g.paint = context.chooseForeground(resistor.foregroundColor)
			context.g.stroke = DrawStyleModule.styleProvider.getStyle(StyleType.FIGURE).stroke
			context.g.drawRect(
				-LENGTH.toDouble() - RESISTOR_WIDTH, -RESISTER_HEIGHT_HALF,
				RESISTOR_WIDTH, 2 * RESISTER_HEIGHT_HALF
			)

			if (isVariable) {
				drawVariableResistorArrow(context)
			}
		}

		override fun drawInductor(
			inductor: OrientableRectangularVerticeView<*>,
			up: Boolean,
			context: DrawContext,
			foregroundColor: Paint,
			backgroundColor: Color,
			stroke: Stroke
		) {
			if (inductor.shadow) {
				DropShadow.draw(context, transparency = inductor.transparency) {
					context.g.fillRect(
						LENGTH.toDouble(), -INDUCTOR_HEIGHT_HALF,
						INDUCTOR_WIDTH, 2 * INDUCTOR_HEIGHT_HALF
					)
				}
			}

			if (context.useContextColors) {
				context.g.color = context.chooseForeground(backgroundColor)
			} else {
				context.g.paint = foregroundColor
			}
			context.g.fillRect(
				LENGTH.toDouble(), -INDUCTOR_HEIGHT_HALF,
				INDUCTOR_WIDTH, 2 * INDUCTOR_HEIGHT_HALF
			)
		}

		override fun drawDiode(diode: OrientableRectangularVerticeView<*>, context: DrawContext) {
			Companion.drawDiode(diode,context, false)
		}
	},

	AMERICAN("ANSI") {

		/** Maps [Stroke] widths (from a client's [Style]) to the [Stroke] to be used the American resistor shape. */
		private val resistorStrokes = mutableMapOf<Float, Stroke>()

		private fun getResistorStroke(clientStroke: Stroke): Stroke =
			resistorStrokes.getOrPut(clientStroke.width) {
				Stroke(clientStroke.width, LineCap.BUTT, LineJoin.MITER)
			}

		private val inductorStrokes = mutableMapOf<Float, Stroke>()

		private fun getInductorStroke(clientStroke: Stroke): Stroke =
			inductorStrokes.getOrPut(clientStroke.width) {
				Stroke(clientStroke.width, LineCap.BUTT, LineJoin.MITER)
			}

		override fun drawAndGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, AND_PATH, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawNandGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, AND_PATH, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawOrGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, OR_PATH, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawNorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
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

		override fun drawResistor(resistor: OrientableRectangularVerticeView<*>, isVariable: Boolean, context: DrawContext, foregroundColor: Paint, backgroundColor: Color, stroke: Stroke) {
			context.g.paint = foregroundColor
			context.g.stroke = getResistorStroke(resistor.stroke)
			context.g.draw(RESISTOR_PATH)

			if (isVariable) {
				context.g.color = context.chooseForeground(resistor.foregroundColor)
				drawVariableResistorArrow(context)
			}
		}

		override fun drawInductor(
			inductor: OrientableRectangularVerticeView<*>,
			up: Boolean,
			context: DrawContext,
			foregroundColor: Paint,
			backgroundColor: Color,
			stroke: Stroke
		) {
			context.g.paint = foregroundColor
			context.g.stroke = getInductorStroke(inductor.stroke)
			context.g.draw(if (up) INDUCTOR_PATH_UP else INDUCTOR_PATH_DOWN)
		}

		override fun drawDiode(diode: OrientableRectangularVerticeView<*>, context: DrawContext) {
			Companion.drawDiode(diode, context, true)
		}

		override val orShapeConnectedPortViewLength: Int get() = (2 * SCALE * 0.35).toInt()
	},

	VERBOSE("Verbose") {

		private val andText ="AND"
		private val orText = "OR"
		private val notText = "NOT"
		private val nandText = "NAND"
		private val norText = "NOR"
		private val xorText = "XOR"
		private val xnorText = "XNOR"
		private val bufferText = "Same"

		override fun getFont(font: Font): Font = font.deriveFont((font.size * 0.6).toInt())

		override val orShapeConnectedPortViewLength: Int get() = 0

		override fun drawAndGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, andText)
		}

		override fun drawNandGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, nandText)
		}

		override fun drawOrGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, orText)
		}

		override fun drawNorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, norText)
		}

		override fun drawXorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, xorText)
		}

		override fun drawXnorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, xnorText)
		}

		override fun drawNotGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, notText)
		}

		override fun drawBufferGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, bufferText)
		}

		override fun drawResistor(resistor: OrientableRectangularVerticeView<*>, isVariable: Boolean, context: DrawContext, foregroundColor: Paint, backgroundColor: Color, stroke: Stroke) {
			EUROPEAN.drawResistor(resistor, isVariable, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawInductor(
			inductor: OrientableRectangularVerticeView<*>,
			up: Boolean,
			context: DrawContext,
			foregroundColor: Paint,
			backgroundColor: Color,
			stroke: Stroke
		) {
			EUROPEAN.drawInductor(inductor, true, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawDiode(
			diode: OrientableRectangularVerticeView<*>,
			context: DrawContext
		) {
			EUROPEAN.drawDiode(diode, context)
		}
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

		val XOR_PATH = System.createPath()
			.moveTo(-SCALE, SCALE)
			.lineTo(SCALE, SCALE)
			.quadTo(4 * SCALE, SCALE, 6 * SCALE, 4 * SCALE)
			.quadTo(4 * SCALE, 7 * SCALE, SCALE, 7 * SCALE)
			.lineTo(-SCALE, 7 * SCALE)
			.quadTo(0.5 * SCALE, 4.0 * SCALE, -SCALE.toDouble(), SCALE.toDouble())
			.close()
			.moveTo(-SCALE - EXCLUSIVE_OFFSET, 7 * SCALE.toDouble())
			.quadTo(0.5 * SCALE - EXCLUSIVE_OFFSET, 4.0 * SCALE, -SCALE - EXCLUSIVE_OFFSET, SCALE.toDouble())
			.quadTo(0.5 * SCALE - EXCLUSIVE_OFFSET, 4.0 * SCALE, -SCALE - EXCLUSIVE_OFFSET, 7 * SCALE.toDouble())

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
		private val RESISTOR_PATH = System.createPath()
			.moveTo(-LENGTH, 0)
			.lineTo(-LENGTH - 0.5 * SCALE, RESISTER_HEIGHT_HALF)
			.lineTo(-LENGTH - 1.5 * SCALE, -RESISTER_HEIGHT_HALF)
			.lineTo(-LENGTH - 2.5 * SCALE, RESISTER_HEIGHT_HALF)
			.lineTo(-LENGTH - 3.5 * SCALE, -RESISTER_HEIGHT_HALF)
			.lineTo(-LENGTH - 4.5 * SCALE, RESISTER_HEIGHT_HALF)
			.lineTo(-LENGTH - 5.5 * SCALE, -RESISTER_HEIGHT_HALF)
			.lineTo(-LENGTH - 6.0 * SCALE, 0.0)

		val RESISTOR_STROKE = Stroke(
			Themes.get<GraphTheme>().figure.stroke.width,
			cap = LineCap.BUTT,
			join = LineJoin.MITER
		)

		private val VARIABLE_RESISTOR_ARROW_PATH = System.createPath()
			.moveTo(0, -3 * SCALE)
			.lineTo(0.5 * SCALE, -2.0 * SCALE)
			.lineTo(-0.5 * SCALE, -2.0 * SCALE)
			.close()

		const val INDUCTOR_WIDTH = 6.0 * SCALE.toDouble()
		const val INDUCTOR_HEIGHT_HALF = SCALE.toDouble()
		val INDUCTOR_STROKE = RESISTOR_STROKE
		private val INDUCTOR_PATH_UP = createInductorPath(1.0)
		private val INDUCTOR_PATH_DOWN = createInductorPath(-1.0)

		private val DIODE_PATH = System.createPath()
			.moveTo(LENGTH + 3.5 * SCALE, 0.0)
			.lineTo(LENGTH + 1.0 * SCALE, -1.5 * SCALE)
			.lineTo(LENGTH + 1.0 * SCALE, 1.5 * SCALE)
			.close()

		private fun createInductorPath(yf: Double): Path =
			System.createPath()
				.moveTo(LENGTH, 0)
				.curveTo(LENGTH.toDouble(), -1.5 * yf * INDUCTOR_HEIGHT_HALF, LENGTH + 2.0 * SCALE, -1.5 * yf * INDUCTOR_HEIGHT_HALF, LENGTH + 2.0 * SCALE, 0.0)
				.curveTo(LENGTH.toDouble() + 2.0 * SCALE, -1.5 * yf * INDUCTOR_HEIGHT_HALF, LENGTH + 4.0 * SCALE, -1.5 * yf * INDUCTOR_HEIGHT_HALF, LENGTH + 4.0 * SCALE, 0.0)
				.curveTo(LENGTH.toDouble() + 4.0 * SCALE, -1.5 * yf * INDUCTOR_HEIGHT_HALF, LENGTH + 6.0 * SCALE, -1.5 * yf * INDUCTOR_HEIGHT_HALF, LENGTH + 6.0 * SCALE, 0.0)

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
				context.translated(-x, -(y + vOffset)) {
					comp.drawCustomShapeContent(it, foregroundColor, backgroundColor)
				}
			}

			context.g.color = foregroundColor
			context.g.stroke = stroke
			context.g.draw(path)

			if (exclusive) {
				context.g.draw(EXCLUSIVE_PATH)
			}

			context.g.translate(-x, -y - vOffset)
		}

		private fun drawEuropeanGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke, text: String? = null) {
			gate.drawBoxShape(context, foregroundColor, backgroundColor, stroke, text)
		}

		private fun drawVariableResistorArrow(context: DrawContext) {
			context.g.translate(-LENGTH - 3.0 * SCALE, 0.0)
			context.g.rotate(0.46)
			context.g.fill(VARIABLE_RESISTOR_ARROW_PATH)
			context.g.drawLine(0.0, -2.0 * SCALE, 0.0, 2.0 * SCALE)
			context.g.rotate(-0.46)
			context.g.translate(-(-LENGTH - 3.0 * SCALE), 0.0)
		}

		protected fun drawDiode(
			diode: OrientableRectangularVerticeView<*>,
			context: DrawContext,
			fill: Boolean
		) {
			// Anode
			(diode.getPortView(diode.model.getPort(1)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
			context.g.drawLine(LENGTH.toDouble(), 0.0, LENGTH + 3.5 * SCALE, 0.0)
			context.g.stroke = diode.stroke
			if (fill) {
				context.g.fill(DIODE_PATH)
			} else {
				context.g.draw(DIODE_PATH)
			}

			// Cathode
			(diode.getPortView(diode.model.getPort(2)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
			context.g.drawLine(LENGTH + 3.5 * SCALE, -1.5 * SCALE, LENGTH + 3.5 * SCALE, 1.5 * SCALE)
			context.g.drawLine(LENGTH + 3.5 * SCALE, 0.0, LENGTH + 4.0 * SCALE, 0.0)
		}
	}

	override fun toString(): String {
		return when (this) {
			EUROPEAN -> Translations.getString("antares.action.symbolStyle.european.name")
			AMERICAN -> Translations.getString("antares.action.symbolStyle.american.name")
			VERBOSE -> Translations.getString("antares.action.symbolStyle.verbose.name")
		}
	}

	open fun getFont(font: Font): Font = font

	/**
	 * Returns the length of OR-shaped input [PortView]s, which is used to adjust lines that lead to the [PortView] when using
	 * roundly shaped borders (especially with american symbol style). Applicable only for [AbstractOrLikeGateView]s with 2 inputs.
	 */
	abstract val orShapeConnectedPortViewLength: Int

	abstract fun drawAndGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawNandGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawOrGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawNorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawXorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawXnorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawNotGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawBufferGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawResistor(resistor: OrientableRectangularVerticeView<*>, isVariable: Boolean, context: DrawContext, foregroundColor: Paint, backgroundColor: Color, stroke: Stroke)

	abstract fun drawInductor(inductor: OrientableRectangularVerticeView<*>, up: Boolean, context: DrawContext, foregroundColor: Paint, backgroundColor: Color, stroke: Stroke)

	abstract fun drawDiode(diode: OrientableRectangularVerticeView<*>, context: DrawContext)

}