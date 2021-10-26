package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.TransistorType
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.Handedness.LEFT
import ch.scorpion.antares.view.Handedness.RIGHT
import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.LineCap
import ch.scorpion.jabbah.draw.graphics.LineJoin
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.graph.GraphApplicationContext

enum class TransistorViewRenderer {

	Custom {
		private val gateLineX = DigitalPortView.LENGTH + 2.0 * SCALE
		private val signalLineX = DigitalPortView.LENGTH + 3.0 * SCALE
		private val barStroke = Stroke(1.5f, LineCap.BUTT, LineJoin.ROUND)
		private val signalPortX = DigitalPortView.LENGTH + 4.0 * SCALE

		private val southSignalPath = System.createPath()
			.moveTo(signalLineX, -0.5 * SCALE)
			.lineTo(signalPortX, -0.5 * SCALE)
			.lineTo(signalPortX, 1.0 * SCALE)

		private val northSignalPath = System.createPath()
			.moveTo(signalLineX, -3.5 * SCALE)
			.lineTo(signalPortX, -3.5 * SCALE)
			.lineTo(signalPortX, -5.0 * SCALE)

		private val nArrowPath = System.createPath()
			.moveTo(signalLineX, -2.0 * SCALE)
			.lineTo(signalPortX, -2.4 * SCALE)
			.lineTo(signalPortX, -1.6 * SCALE)
			.close()

		private val pArrowPath = System.createPath()
			.moveTo(DigitalPortView.LENGTH + 5.0 * SCALE, -2.0 * SCALE)
			.lineTo(signalPortX, -2.4 * SCALE)
			.lineTo(signalPortX, -1.6 * SCALE)
			.close()

		override fun render(view: TransistorView, context: DrawContext) {
			drawGate(view, context)
			drawBulk(view, context)

			view.apply {
				when (handedness) {
					RIGHT -> {
						drawSouthSignalPort(view, context, getPortView(model.getSourcePort()) as DigitalPortView)
						drawNorthSignalPort(view, context, getPortView(model.getDrainPort()) as DigitalPortView)
					}
					LEFT -> {
						drawSouthSignalPort(view, context, getPortView(model.getDrainPort()) as DigitalPortView)
						drawNorthSignalPort(view, context, getPortView(model.getSourcePort()) as DigitalPortView)
					}
				}
			}
		}

		private fun drawGate(view: TransistorView, context: DrawContext) {
			view.apply {
				(getPortView(model.getGatePort()) as DigitalPortView).prepareConnectionDrawContext(context)

				// Gate connection
				val gateConnectionY = when(handedness) {
					RIGHT -> 0.0
					LEFT -> - 4.0 * SCALE
				}
				context.g.drawLine(
					DigitalPortView.LENGTH.toDouble(), gateConnectionY,
					gateLineX, gateConnectionY)

				// Gate bar
				context.g.stroke = barStroke
				context.g.drawLine(
					gateLineX, -4.0 * SCALE,
					gateLineX, 0.0)
			}
		}

		private fun drawSouthSignalPort(view: TransistorView, context: DrawContext, portView: DigitalPortView) {
			view.apply {
				portView.prepareConnectionDrawContext(context)

				// connection
				context.g.draw(southSignalPath)

				// bar
				context.g.stroke = barStroke
				context.g.drawLine(
					signalLineX, -1.0 * SCALE,
					signalLineX, 0.0)
			}
		}

		private fun drawNorthSignalPort(view: TransistorView, context: DrawContext, portView: DigitalPortView) {
			view.apply {
				portView.prepareConnectionDrawContext(context)

				// connection
				context.g.draw(northSignalPath)

				// bar
				context.g.stroke = barStroke
				context.g.drawLine(
					signalLineX, -4.0 * SCALE,
					signalLineX, -3.0 * SCALE
				)
			}
		}

		private fun drawBulk(view: TransistorView, context: DrawContext) {
			view.apply {
				(getPortView(model.getDrainPort()) as DigitalPortView).prepareConnectionDrawContext(context)

				val dx = if (!model.isOn && context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
					0.5 * SCALE
				} else {
					0.0
				}

				(getPortView(model.getDrainPort()) as DigitalPortView).prepareConnectionDrawContext(context)

				context.g.translate(dx, 0.0)

				// arrow
				drawBulkArrow(view, context)

				// bar
				context.g.stroke = barStroke
				context.g.drawLine(
					signalLineX, -2.5 * SCALE,
					signalLineX, -1.5 * SCALE
				)

				context.g.translate(-dx, 0.0)
			}
		}

		private fun drawBulkArrow(view: TransistorView, context: DrawContext) {
			view.apply {
				when (model.transistorType) {
					TransistorType.P -> drawPTypeBulkArrow(context)
					TransistorType.N -> drawNTypeBulkArrow(context)
				}
			}
		}

		private fun drawNTypeBulkArrow(context: DrawContext) {
			context.g.fill(nArrowPath)
			context.g.drawLine(
				signalPortX, -2.0 * SCALE,
				signalPortX + 1.0 * SCALE, -2.0 * SCALE
			)
		}

		private fun drawPTypeBulkArrow(context: DrawContext) {
			context.g.fill(pArrowPath)
			context.g.drawLine(
				signalPortX, -2.0 * SCALE,
				signalLineX, -2.0 * SCALE
			)
		}
	};

	abstract fun render(view: TransistorView, context: DrawContext)
}