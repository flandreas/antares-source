package ch.scorpion.antares.view.symbolstyle

import ch.scorpion.antares.model.analog.AnalogPort
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.OrientableLabeledRectangularVerticeView
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.analog.AnalogLEDView
import ch.scorpion.antares.view.analog.AnalogLEDView.Companion.GRADIENT_RADIUS
import ch.scorpion.antares.view.gate.*
import ch.scorpion.antares.view.gate.LogicGateSize.LARGE
import ch.scorpion.antares.view.gate.LogicGateSize.MEDIUM
import ch.scorpion.antares.view.gate.LogicGateSize.SMALL
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.*
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.Style
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.jabbah.edit.Look.SCALE
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import kotlin.math.PI
import kotlin.math.abs

/**
 * [SymbolStyle] represents international standards for drawing digital gates.
 * The user can globally switch between the supported [SymbolStyle]s.
 */
enum class SymbolStyle(
	override val customName: String
) : EnumProperty<SymbolStyle> {

	EUROPEAN("IEC") {

		override val orShapeConnectedPortViewLength: Int get() = 0

		override fun drawAndGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawNandGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawOrGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawNorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawXorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawXnorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawNotGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawBufferGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawTriStateBufferGate(gate: TriStateBufferGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			if (triStateAlwaysTriangle) {
				AMERICAN.drawTriStateBufferGate(gate, context, foregroundColor, backgroundColor, stroke)
				return
			}

			// TODO Adjust to size

			val h = gate.heightInt - 1.5 * SCALE

			if (gate.shadow) {
				DropShadow.draw(context, gate.transparency) {
					context.g.fillRect(gate.xInt, gate.yInt, gate.widthInt, h.toInt())
				}
			}

			context.g.color = backgroundColor
			context.g.fillRect(gate.x, gate.y, gate.bounds.width, h)

			context.g.color = foregroundColor
			context.g.stroke = stroke
			context.g.drawRect(gate.x, gate.y, gate.bounds.width, h)

			gate.drawLabelText(context)
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
			Companion.drawDiode(diode, context, false)
		}

		override fun drawAnalogLED(led: AnalogLEDView, context: DrawContext) {
			Companion.drawAnalogLED(led, context, fill = false)
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

		override fun drawAndGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, gate.size, AND_PATHS[gate.size]!!, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawNandGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, gate.size, AND_PATHS[gate.size]!!, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawOrGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, gate.size, OR_PATHS[gate.size]!!, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawNorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, gate.size, OR_PATHS[gate.size]!!, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawXorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, gate.size, gate.x, gate.y, gate.bounds.height, OR_PATHS[gate.size]!!, context,
				foregroundColor, backgroundColor, stroke, true, gate.transparency)
		}

		override fun drawXnorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, gate.size, gate.x, gate.y, gate.bounds.height, OR_PATHS[gate.size]!!, context,
				foregroundColor, backgroundColor, stroke, true, gate.transparency)
		}

		override fun drawNotGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, gate.size, NOT_PATHS[gate.size]!!, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawBufferGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawAmericanGate(gate, gate.size, NOT_PATHS[gate.size]!!, context, foregroundColor, backgroundColor, stroke)
		}

		override fun drawTriStateBufferGate(gate: TriStateBufferGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			val bounds = SymbolStyle.NOT_PATHS[gate.size]!!.boundingBox
			drawAmericanGate(gate, gate.size, gate.x, gate.y + SCALE, bounds.height, SymbolStyle.NOT_PATHS[gate.size]!!, context,
				foregroundColor, backgroundColor, stroke, false, gate.transparency)
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

		override fun drawAnalogLED(led: AnalogLEDView, context: DrawContext) {
			Companion.drawAnalogLED(led, context, fill = true)
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

		override fun drawAndGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, andText)
		}

		override fun drawNandGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, nandText)
		}

		override fun drawOrGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, orText)
		}

		override fun drawNorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, norText)
		}

		override fun drawXorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, xorText)
		}

		override fun drawXnorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, xnorText)
		}

		override fun drawNotGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, notText)
		}

		override fun drawBufferGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			drawEuropeanGate(gate, context, foregroundColor, backgroundColor, stroke, bufferText)
		}

		override fun drawTriStateBufferGate(gate: TriStateBufferGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
			AMERICAN.drawTriStateBufferGate(gate, context, foregroundColor, backgroundColor, stroke)
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

		override fun drawAnalogLED(led: AnalogLEDView, context: DrawContext) {
			EUROPEAN.drawAnalogLED(led, context)
		}
	};

	companion object {

		/** The name of the [String] property in [Properties] designating the [SymbolStyle]'s name. */
		const val PROP_SYMBOL_STYLE = "ch.scorpion.antares.view.symbolStyle"

		/** The name of the [Boolean] property in [Properties] controlling whether [TriStateBufferGateView] is always drawn as triangle shape*/
		const val PROP_TRI_STATE_ALWAYS_TRIANGLE = "ch.scorpion.antares.view.triStateAlwaysTriangle"

		private const val EXCLUSIVE_OFFSET = 6.0

		fun withName(customName: String): SymbolStyle {
			for (symbolStyle in values()) {
				if (symbolStyle.customName == customName) {
					return symbolStyle
				}
			}
			throw IllegalArgumentException("Unknown SymbolStyle $customName")
		}

		val triStateAlwaysTriangle: Boolean by lazy {
			BaseModule.properties.getBoolean(PROP_TRI_STATE_ALWAYS_TRIANGLE)
		}

		val AND_PATHS = mapOf(
			SMALL to createAndPath(SMALL.factor),
			MEDIUM to createAndPath(MEDIUM.factor),
			LARGE to createAndPath(LARGE.factor)
		)

		private fun createAndPath(f: Float): Path {
			return System.createPath()
				.moveTo(0f, f * SCALE)
				.lineTo(3f * f * SCALE, f * SCALE)
				.quadTo(6f * f * SCALE, f * SCALE, 6 * f * SCALE, 4 * f * SCALE)
				.quadTo(6 * f * SCALE, 7 * f * SCALE, 3 * f * SCALE, 7 * f * SCALE)
				.lineTo(0f, 7 * f * SCALE)
				.close()
		}

		val OR_PATHS = mapOf(
			SMALL to createOrPath(SMALL.factor),
			MEDIUM to createOrPath(MEDIUM.factor),
			LARGE to createOrPath(LARGE.factor)
		)

		private fun createOrPath(f: Float): Path {
			return System.createPath()
				.moveTo(-f * SCALE, f * SCALE)
				.lineTo(f * SCALE, f * SCALE)
				.quadTo(4 * f * SCALE, f * SCALE, 6 * f * SCALE, 4 * f * SCALE)
				.quadTo(4 * f * SCALE, 7 * f * SCALE, f * SCALE, 7 * f * SCALE)
				.lineTo(-f * SCALE, 7 * f * SCALE)
				.quadTo(0.5 * f * SCALE, 4.0 * f * SCALE, -f * SCALE.toDouble(), f * SCALE.toDouble())
				.close()
		}

		val XOR_PATHS = mapOf(
			SMALL to createXorPath(SMALL.factor),
			MEDIUM to createXorPath(MEDIUM.factor),
			LARGE to createXorPath(LARGE.factor)
		)

		private fun createXorPath(f: Float): Path {
			return System.createPath()
				.moveTo(-f * SCALE, f * SCALE)
				.lineTo(f * SCALE, f *SCALE)
				.quadTo(4 * f * SCALE, f * SCALE, 6 * f * SCALE, 4 * f * SCALE)
				.quadTo(4 * f * SCALE, 7 * f * SCALE, f * SCALE, 7 * f * SCALE)
				.lineTo(-f * SCALE, 7 * f * SCALE)
				.quadTo(0.5 * f * SCALE, 4.0 * f * SCALE, -f * SCALE.toDouble(), f * SCALE.toDouble())
				.close()
				.moveTo(-f * SCALE - f * EXCLUSIVE_OFFSET, 7 * f * SCALE.toDouble())
				.quadTo(0.5 * f * SCALE - f * EXCLUSIVE_OFFSET, 4.0 * f * SCALE, -f * SCALE - f * EXCLUSIVE_OFFSET, f * SCALE.toDouble())
				.quadTo(0.5 * f * SCALE - f * EXCLUSIVE_OFFSET, 4.0 * f * SCALE, -f * SCALE - f * EXCLUSIVE_OFFSET, 7 * f * SCALE.toDouble())
		}

		private val EXCLUSIVE_PATHS = mapOf(
			SMALL to createExclusivePath(SMALL.factor),
			MEDIUM to createExclusivePath(MEDIUM.factor),
			LARGE to createExclusivePath(LARGE.factor)
		)

		private fun createExclusivePath(f: Float): Path {
			return System.createPath()
				.moveTo(-f * SCALE - f * EXCLUSIVE_OFFSET, 7 * f * SCALE.toDouble())
				.quadTo(0.5 * f * SCALE - f * EXCLUSIVE_OFFSET, 4.0 * f * SCALE, -f * SCALE - f * EXCLUSIVE_OFFSET, f * SCALE.toDouble())
		}

		val NOT_PATHS = mapOf(
			SMALL to createNotPath(SMALL.factor),
			MEDIUM to createNotPath(MEDIUM.factor),
			LARGE to createNotPath(LARGE.factor)
		)

        private fun createNotPath(f: Float): Path =
			System.createPath()
				.moveTo(0f, f * SCALE)
				.lineTo(f * 6 * SCALE, f * 4 * SCALE)
				.lineTo(0f, f * 7 * SCALE)
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

		private val LED_ARROW_PATH = System.createPath()
			.moveTo(1.5 * SCALE, 0.0)
			.lineTo(1.5 * SCALE, -0.35 * SCALE)
			.lineTo(2.25 * SCALE, 0.0)
			.lineTo(1.5 * SCALE, 0.35 * SCALE)
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

		private fun drawAmericanGate(
			gate: OrientableLabeledRectangularVerticeView<*>,
			size: LogicGateSize,
			path: Path,
			context: DrawContext,
			foregroundColor: Color,
			backgroundColor: Color,
			stroke: Stroke
		) {
			drawAmericanGate(gate, size, gate.x, gate.y, gate.bounds.height, path, context,
				foregroundColor, backgroundColor, stroke, false, gate.transparency)
		}

		private fun drawAmericanGate(
			comp: OrientableLabeledRectangularVerticeView<*>,
			size: LogicGateSize,
			x: Double,
			y: Double,
			height: Double,
			path: Path,
			context: DrawContext,
			foregroundColor: Color,
			 backgroundColor: Color,
			stroke: Stroke,
			exclusive: Boolean,
			transparency: Int
		) {

			val vOffset = (height - 2 * comp.scale * SCALE - path.boundingBox.height) / 2

			if (vOffset > 0) {
				// Draw the extension line at the input's side
				// TODO: Only necessary with more than 3 Inputs.
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
				context.g.draw(EXCLUSIVE_PATHS[size]!!)
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
			if (diode.shadow) {
				DropShadow.draw(context, diode.transparency) {
					context.g.fill(DIODE_PATH)
				}
			}

			// Anode
			(diode.getPortView(diode.model.getPort(1)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
			context.g.drawLine(LENGTH.toDouble(), 0.0, LENGTH + 3.5 * SCALE, 0.0)
			context.g.stroke = diode.stroke
			if (fill) {
				context.g.color = context.chooseForeground(diode.color.foregroundColor)
				context.g.fill(DIODE_PATH)
			} else {
				context.g.color = context.chooseBackground(DrawStyleModule.styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor)
				context.g.fill(DIODE_PATH)
				context.g.color = context.chooseForeground(diode.color.foregroundColor)
				context.g.draw(DIODE_PATH)
			}

			// Cathode
			context.g.stroke = diode.stroke
			context.g.drawLine(LENGTH + 3.5 * SCALE, -1.5 * SCALE, LENGTH + 3.5 * SCALE, 1.5 * SCALE)
			(diode.getPortView(diode.model.getPort(2)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
			context.g.drawLine(LENGTH + 3.5 * SCALE, 0.0, LENGTH + 4.0 * SCALE, 0.0)
		}

		protected fun drawAnalogLED(
			led: AnalogLEDView,
			context: DrawContext,
			fill: Boolean
		) {
			// Anode
			context.g.color = context.choose(led.style.color).foregroundColor
			context.g.drawLine(LENGTH.toDouble(), 0.0, LENGTH + 3.5 * SCALE, 0.0)
			context.g.stroke = led.stroke

			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				context.g.color = led.executionLEDColor
				context.g.fill(DIODE_PATH)
				context.g.color = led.foregroundColor
				context.g.draw(DIODE_PATH)
			} else {
				if (fill) {
					context.g.fill(DIODE_PATH)
				} else {
					context.g.draw(DIODE_PATH)
				}
			}

			// Cathode
			context.g.stroke = led.stroke
			context.g.drawLine(LENGTH + 3.5 * SCALE, -1.5 * SCALE, LENGTH + 3.5 * SCALE, 1.5 * SCALE)
			(led.getPortView(led.model.getPort(2)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
			context.g.drawLine(LENGTH + 3.5 * SCALE, 0.0, LENGTH + 4.0 * SCALE, 0.0)

			// Arrows
			context.g.color = context.choose(led.style.color).foregroundColor
			drawLEDArrow(context, LENGTH + 2.5 * SCALE, -1.25 * SCALE)
			drawLEDArrow(context, LENGTH + 1.5 * SCALE, -2.0 * SCALE)

			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				// Halo
				if (abs((led.model.getPort<AnalogSignal>() as AnalogPort).current) >= led.minCurrent
					&& BaseModule.properties.getBoolean(AnalogLEDView.PROP_DRAW_HALO)
				) {
					context.g.paint = led.haloPaint
					context.g.fillCircle(LENGTH + 2.0 * SCALE, 0.0, GRADIENT_RADIUS.toDouble())
				}
			}
		}

		private fun drawLEDArrow(context: DrawContext, x: Double, y: Double) {
			context.g.stroke = DrawStyleModule.styleProvider.getStyle(StyleType.ANNOTATION).stroke
			context.translated(x, y) {
				context.g.rotate(-PI * 5 / 16)
				context.g.drawLine(0.0, 0.0, 1.5 * SCALE, 0.0)
				context.g.fill(LED_ARROW_PATH)
				context.g.rotate(PI * 5 / 16)
			}
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

	abstract fun drawAndGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawNandGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawOrGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawNorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawXorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawXnorGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawNotGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawBufferGate(gate: LogicGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawTriStateBufferGate(gate: TriStateBufferGateView, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

	abstract fun drawResistor(resistor: OrientableRectangularVerticeView<*>, isVariable: Boolean, context: DrawContext, foregroundColor: Paint, backgroundColor: Color, stroke: Stroke)

	abstract fun drawInductor(inductor: OrientableRectangularVerticeView<*>, up: Boolean, context: DrawContext, foregroundColor: Paint, backgroundColor: Color, stroke: Stroke)

	abstract fun drawDiode(diode: OrientableRectangularVerticeView<*>, context: DrawContext)

	abstract fun drawAnalogLED(led: AnalogLEDView, context: DrawContext)

}