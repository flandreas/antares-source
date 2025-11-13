package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.TransistorType.N
import ch.scorpion.antares.model.net.TransistorType.P
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.Handedness.LEFT
import ch.scorpion.antares.view.Handedness.RIGHT
import ch.scorpion.jabbah.edit.Look.SCALE
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.LineCap
import ch.scorpion.jabbah.draw.graphics.LineJoin
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.port.PortView

enum class TransistorViewSymbol(
	override val customName: String
) : EnumProperty<TransistorViewSymbol> {

	Bulk("bulk") {
		private val gateLineX = AbstractAntaresPortView.LENGTH + 2.0 * SCALE
		private val signalLineX = AbstractAntaresPortView.LENGTH + 3.0 * SCALE
		private val signalPortX = AbstractAntaresPortView.LENGTH + 4.0 * SCALE

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
			.moveTo(AbstractAntaresPortView.LENGTH + 5.0 * SCALE, -2.0 * SCALE)
			.lineTo(signalPortX, -2.4 * SCALE)
			.lineTo(signalPortX, -1.6 * SCALE)
			.close()

		override fun render(view: AbstractTransistorView<*>, context: DrawContext) {
			drawGate(view, context)
			drawBulk(view, context)

			drawNorthSignalPort(view, context, view.northPortView as AbstractAntaresPortView<*>)
			drawSouthSignalPort(view, context, view.southPortView as AbstractAntaresPortView<*>)
		}

		override fun getGatePositionY(view: AbstractTransistorView<*>): Int =
			when (view.model.transistorType) {
				N -> when (view.handedness) {
					RIGHT -> 0
					LEFT -> -4 * SCALE
				}
				P -> when (view.handedness) {
					RIGHT -> -4 * SCALE
					LEFT -> 0
				}
		}

		private fun drawGate(view: AbstractTransistorView<*>, context: DrawContext) {
			view.apply {
				(getPortView(model.gatePort) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)

				// Gate connection
				val gateConnectionY = getGatePositionY(view).toDouble()
				context.g.drawLine(
					AbstractAntaresPortView.LENGTH.toDouble(), gateConnectionY,
					gateLineX, gateConnectionY)

				// Gate bar
				context.g.stroke = BAR_STROKE
				context.g.drawLine(
					gateLineX, -4.0 * SCALE,
					gateLineX, 0.0)
			}
		}

		private fun drawSouthSignalPort(view: AbstractTransistorView<*>, context: DrawContext, portView: AbstractAntaresPortView<*>) {
			view.apply {
				portView.prepareConnectionDrawContext(context)
				prepareDrainExecutionDrawContext(view, context, portView)

				// connection
				context.g.draw(southSignalPath)

				// bar
				context.g.stroke = BAR_STROKE
				context.g.drawLine(
					signalLineX, -1.0 * SCALE,
					signalLineX, 0.0)
			}
		}

		private fun drawNorthSignalPort(view: AbstractTransistorView<*>, context: DrawContext, portView: AbstractAntaresPortView<*>) {
			view.apply {
				portView.prepareConnectionDrawContext(context)
				prepareDrainExecutionDrawContext(view, context, portView)

				// connection
				context.g.draw(northSignalPath)

				// bar
				context.g.stroke = BAR_STROKE
				context.g.drawLine(
					signalLineX, -4.0 * SCALE,
					signalLineX, -3.0 * SCALE
				)
			}
		}

		private fun drawBulk(view: AbstractTransistorView<*>, context: DrawContext) {
			view.apply {
				val dx = if (view.drawOnOff && !model.isOn && context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
					view.switchOffDisplacement
				} else {
					0.0
				}

				(getPortView(model.drainPort) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)

				context.translated(dx, 0.0) {
					// arrow
					drawBulkArrow(view, it)

					// bar
					it.g.stroke = BAR_STROKE
					it.g.drawLine(
						signalLineX, -2.5 * SCALE,
						signalLineX, -1.5 * SCALE
					)
				}
			}
		}

		private fun drawBulkArrow(view: AbstractTransistorView<*>, context: DrawContext) {
			view.apply {
				when (model.transistorType) {
					P -> drawPTypeBulkArrow(context)
					N -> drawNTypeBulkArrow(context)
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
	},

	InverterCircle("inverterCircle") {
		private val gateLineX = AbstractAntaresPortView.LENGTH + 2.0 * SCALE
		private val signalLineX = AbstractAntaresPortView.LENGTH + 2.5 * SCALE
		private val signalPortX = AbstractAntaresPortView.LENGTH + 4.0 * SCALE
		private val circleSize = SCALE.toDouble()

		private val pArrowPath = System.createPath()
			.moveTo(0, 0)
			.lineTo(1.0 * SCALE, -0.4 * SCALE)
			.lineTo(1.0 * SCALE, 0.4 * SCALE)
			.close()

		private val nArrowPath = System.createPath()
			.moveTo(0, 0)
			.lineTo(-1.0 * SCALE, -0.4 * SCALE)
			.lineTo(-1.0 * SCALE, 0.4 * SCALE)
			.close()

		override fun getGatePositionY(view: AbstractTransistorView<*>): Int = -2 * SCALE

		override fun render(view: AbstractTransistorView<*>, context: DrawContext) {
			drawGate(view, context)

			view.northPortView.apply {
				drawNorthSignalPort(view, context, this, this.port === view.model.sourcePort)
			}
			view.southPortView.apply {
				drawSouthSignalPort(view, context, this, this.port === view.model.sourcePort)
			}
		}

		private fun drawGate(view: AbstractTransistorView<*>, context: DrawContext) {
			val gatePositionY = getGatePositionY(view).toDouble()
			view.apply {
				(getPortView(model.gatePort) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
				when (model.transistorType) {
					P -> {
						context.g.drawOval(AbstractAntaresPortView.LENGTH.toDouble() + SCALE, getGatePositionY(this) - 0.5 * SCALE, circleSize, circleSize)
						context.g.drawLine(AbstractAntaresPortView.LENGTH.toDouble(), gatePositionY, AbstractAntaresPortView.LENGTH.toDouble() + SCALE, gatePositionY)
					}
					N -> {
						context.g.drawLine(AbstractAntaresPortView.LENGTH.toDouble(), gatePositionY, AbstractAntaresPortView.LENGTH.toDouble() + 2 * SCALE, gatePositionY)
					}
				}
				// Gate bar
				context.g.drawLine(gateLineX, -3.5 * SCALE, gateLineX, -0.5 * SCALE)
			}
		}

		private fun drawNorthSignalPort(view: AbstractTransistorView<*>, context: DrawContext, portView: PortView<*>, isSource: Boolean) {
			portView.prepareConnectionDrawContext(context)
			prepareDrainExecutionDrawContext(view, context, portView)
			context.g.apply {
				// Connection
				drawLine(signalPortX, -5.0 * SCALE, signalPortX, -3.5 * SCALE)
				drawLine(signalPortX, -3.5 * SCALE, signalLineX, -3.5 * SCALE)
				// Arrow
				if (isSource) {
					// Bar
					drawLine(signalLineX, -4.0 * SCALE, signalLineX, -2.0 * SCALE)
					when (view.transistorType) {
						P -> context.translated(signalLineX, -3.5 * SCALE) { fill(pArrowPath) }
						N -> context.translated(signalPortX, -3.5 * SCALE) { fill(nArrowPath) }
					}
				} else {
					// Bar
					if (view.drawOnOff && context.castedAppContext<GraphApplicationContext>()!!.isExecute && !view.model.isOn) {
						drawLine(signalLineX, -4.0 * SCALE, signalLineX, -3.5 * SCALE)
						drawLine(signalLineX, -3.5 * SCALE, signalLineX + view.switchOffDisplacement, -2.0 * SCALE)

					} else {
						drawLine(signalLineX, -4.0 * SCALE, signalLineX, -2.0 * SCALE)
					}
				}
			}
		}

		private fun drawSouthSignalPort(view: AbstractTransistorView<*>, context: DrawContext, portView: PortView<*>, isSource: Boolean) {
			portView.prepareConnectionDrawContext(context)
			prepareDrainExecutionDrawContext(view, context, portView)
			context.g.apply {
				// Connection
				drawLine(signalPortX, 1.0 * SCALE, signalPortX, -0.5 * SCALE)
				drawLine(signalPortX, -0.5 * SCALE, signalLineX, -0.5 * SCALE)

				// Arrow
				if (isSource) {
					// Bar
					drawLine(signalLineX, 0.0, signalLineX, -2.0 * SCALE)
					when (view.transistorType) {
						P -> context.translated(signalLineX, -0.5 * SCALE) { fill(pArrowPath) }
						N -> context.translated(signalPortX, -0.5 * SCALE) { fill(nArrowPath) }
					}
				} else {
					// Bar
					if (view.drawOnOff && context.castedAppContext<GraphApplicationContext>()!!.isExecute && !view.model.isOn) {
						drawLine(signalLineX, 0.0, signalLineX, -0.5 * SCALE)
						drawLine(signalLineX, -0.5 * SCALE, signalLineX + view.switchOffDisplacement, -2.0 * SCALE)
					} else {
						drawLine(signalLineX, 0.0, signalLineX, -2.0 * SCALE)
					}
				}
			}
		}
	};

	companion object {
		const val PROP_TRANSISTOR_SYMBOL = "antares.view.transistorSymbol"
		val BAR_STROKE = Stroke(1.5f, LineCap.BUTT, LineJoin.ROUND)

		fun withName(customName: String): TransistorViewSymbol =
			values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown TransistorViewSymbol '$customName'")

		val configured: TransistorViewSymbol get() =
			withName(BaseModule.properties.getString(PROP_TRANSISTOR_SYMBOL))

		fun prepareDrainExecutionDrawContext(view: AbstractTransistorView<*>, context: DrawContext, portView: PortView<*>) {
			if (context.castedAppContext<GraphApplicationContext>()!!.showNetState
				&& portView.port == view.model.drainPort
				&& portView.port.isConnected
			) {
				val signal = portView.port.net!!.signal!!
				if (signal is DigitalSignal) {
					context.g.color = signal.color.foregroundColor
				}
			}
		}
	}


	abstract fun render(view: AbstractTransistorView<*>, context: DrawContext)

	abstract fun getGatePositionY(view: AbstractTransistorView<*>): Int

	override fun toString(): String {
		return when (this) {
			Bulk -> Translations.getString("antares.preference.transistorSymbol.bulk.name")
			InverterCircle -> Translations.getString("antares.preference.transistorSymbol.inverterSymbol.name")
		}
	}
}