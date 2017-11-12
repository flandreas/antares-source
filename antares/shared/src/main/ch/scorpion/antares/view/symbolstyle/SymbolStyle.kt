package ch.scorpion.antares.view.symbolstyle

import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.gate.BoxGateView
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Math

enum class SymbolStyle(val customName: String) {
    EUROPEAN("IEC") {
        override fun drawAndGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
            drawEuropean(gate, context, foregroundColor, backgroundColor)
        }

        override fun drawOrGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
            drawEuropean(gate, context, foregroundColor, backgroundColor)
        }

        override fun drawXorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
            drawEuropean(gate, context, foregroundColor, backgroundColor)
        }

        override fun drawXnorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
            drawEuropean(gate, context, foregroundColor, backgroundColor)
        }

        override fun drawNotGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
            drawEuropean(gate, context, foregroundColor, backgroundColor)
        }

        override fun drawBufferGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
            drawEuropean(gate, context, foregroundColor, backgroundColor)
        }

        override fun getOrShapeConnectedPortViewLength(gate: BoxGateView<*>, index: Int): Int {
            return 0
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
            drawAmerican(gate.x, gate.y, gate.bounds.height, OR_PATH, context, foregroundColor, backgroundColor, stroke, true)
        }

        override fun drawXnorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
            drawAmerican(gate.x, gate.y, gate.bounds.height, OR_PATH, context, foregroundColor, backgroundColor, stroke, true)
        }

        override fun drawNotGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
            drawAmerican(gate, NOT_PATH, context, foregroundColor, backgroundColor, stroke)
        }

        override fun drawBufferGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
            drawAmerican(gate, NOT_PATH, context, foregroundColor, backgroundColor, stroke)
        }

        override fun getOrShapeConnectedPortViewLength(gate: BoxGateView<*>, index: Int): Int {
            // Heuristic to adapt to the curved shape of OR-like shapes

			val inputCount = gate.model!!.inputCount
			if (inputCount == 2) {
				return (2 * Look.SCALE * 0.35).toInt()
			}
			val distanceFromMiddle = Math.abs((inputCount - 1) / 2.0 - index)
			if (distanceFromMiddle == 0.0) {
				return (2 * Look.SCALE * 0.15).toInt()
			} else if (distanceFromMiddle <= 0.5) {
				return (2 * Look.SCALE * 0.2).toInt()
			} else if (distanceFromMiddle == 1.0) {
				return (2 * Look.SCALE * 0.35).toInt()
			} else if (distanceFromMiddle == 1.5) {
				return (2 * Look.SCALE * 0.5).toInt()
			} else {
				return 0
			}
        }
    };

    companion object {

        /** The name of the [String] property in [Properties] designating the [SymbolStyle]'s name. */
        val PROP_SYMBOL_STYLE = "ch.scorpion.antares.view.symbolStyle"

        val EXCLUSIVE_OFFSET = 6.0

        fun withName(customName: String): SymbolStyle {
            for (symbolStyle in SymbolStyle.values()) {
                if (symbolStyle.customName == customName) {
                    return symbolStyle
                }
            }
            throw IllegalArgumentException("Unknown SymbolStyle $customName")
        }

        val AND_PATH = System.get().createPath()
                .moveTo(0, Look.SCALE)
                .lineTo(3 * Look.SCALE, Look.SCALE)
                .quadTo(6 * Look.SCALE, Look.SCALE, 6 * Look.SCALE, 4 * Look.SCALE)
                .quadTo(6 * Look.SCALE, 7 * Look.SCALE, 3 * Look.SCALE, 7 * Look.SCALE)
                .lineTo(0, 7 * Look.SCALE)
                .close()

        val OR_PATH = System.get().createPath()
                .moveTo(-Look.SCALE, Look.SCALE)
                .lineTo(Look.SCALE, Look.SCALE)
                .quadTo(4 * Look.SCALE, Look.SCALE, 6 * Look.SCALE, 4 * Look.SCALE)
                .quadTo(4 * Look.SCALE, 7 * Look.SCALE, Look.SCALE, 7 * Look.SCALE)
                .lineTo(-Look.SCALE, 7 * Look.SCALE)
                .quadTo(0.5 * Look.SCALE, 4.0 * Look.SCALE, -Look.SCALE.toDouble(), Look.SCALE.toDouble())
                .close()

        val EXCLUSIV_PATH = System.get().createPath()
                .moveTo(-Look.SCALE - EXCLUSIVE_OFFSET, 7 * Look.SCALE.toDouble())
                .quadTo(0.5 * Look.SCALE - EXCLUSIVE_OFFSET, 4.0 * Look.SCALE, -Look.SCALE - EXCLUSIVE_OFFSET, Look.SCALE.toDouble())

        val NOT_PATH = System.get().createPath()
                .moveTo(0, Look.SCALE)
                .lineTo(6 * Look.SCALE, 4 * Look.SCALE)
                .lineTo(0, 7 * Look.SCALE)
                .lineTo(0, Look.SCALE)
                .close()

        private fun drawEuropean(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color) {
            gate.drawEuropeanShape(context, foregroundColor, backgroundColor)
        }

        fun drawAmerican(gate: BoxGateView<*>, path: Path, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke) {
            drawAmerican(gate.x, gate.y, gate.bounds.height, path, context, foregroundColor, backgroundColor, stroke, false)
        }

        fun drawAmerican(x: Double, y: Double, height: Double, path: Path, context: DrawContext, foregroundColor: Color,
                         backgroundColor: Color, stroke: Stroke, exclusiv: Boolean) {

            val vOffset = (height - 2 * Look.SCALE - path.boundingBox.height) / 2

            if (vOffset > 0) {
                context.g.color = foregroundColor
                context.g.drawLine(
                        x.toInt(), (y + Look.SCALE).toInt(),
                        x.toInt(), (y + height - Look.SCALE).toInt())
            }

            context.g.translate(x, y + vOffset)
            context.g.color = backgroundColor
            context.g.fill(path)
            context.g.color = foregroundColor
            context.g.stroke = stroke
            context.g.draw(path)

            if (exclusiv) {
                context.g.draw(EXCLUSIV_PATH)
            }

            context.g.translate(-x, -y - vOffset)
        }
    }

    abstract fun drawAndGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

    abstract fun drawOrGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

    abstract fun drawXorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

    abstract fun drawXnorGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

    abstract fun drawNotGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

    abstract fun drawBufferGate(gate: BoxGateView<*>, context: DrawContext, foregroundColor: Color, backgroundColor: Color, stroke: Stroke)

    abstract fun getOrShapeConnectedPortViewLength(gate: BoxGateView<*>, index: Int): Int

}