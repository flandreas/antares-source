package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.analog.falstad.FalstadAnalogCircuitAnalysis
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.Connection
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl
import kotlin.math.abs

/**
 * During simulation, [AnalogEdgeView] is treated as resistor with a very small resistance.
 */
class AnalogEdgeView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	net: AnalogNet = AnalogNet()
) : EdgeViewImpl<AnalogSignal>(
		styleProvider,
		DEF_EDGE_TO_PORT_CONNECTOR_SUPPLIER,
		DEF_ORIG_ENDPOINT_CONNECTOR_SUPPLIER,
		DEF_DEST_ENDPOINT_CONNECTOR_SUPPLIER,
		net
	),
	AnalogElement
{

	companion object {

		private val LOG by logger(AnalogEdgeView::class)

		/** Determines the speed of the current flow animation by multiplication with [SystemSpeed].*/
		private const val FACTOR = 0.3

		/** Limits the effective speed of the current flow animation.*/
		private const val MAX_DELTA = 2.7

		private const val DEF_RESISTANCE = 1E-06
	}

	/**
	 * The electrical current (in A) flowing through this [AnalogEdgeView] during simulation.
	 * Positive values indicate current flowing from [origin] to [destination].
	 * Can't be modelled on model [Net] because [Net] doesn't contain nodes, which
	 * is required for modelling Kirchhoff's "Current Law".
	 */
	var current: Double = 0.0
		set(value) {
			LOG.trace("Set current=$value on EdgeView $id")
			field = value
			(origin?.portView as AnalogPortView?)?.current = value
			(destination?.portView as AnalogPortView?)?.current = value
		}

	/**
	 * Periodically updated by the current flow animation task of [AnalogGraphView].
	 * Represents the "timing tick" that drives the animation. Repeats within the range
	 * from 0 to [CurrentFlowVisualization.DISTANCE].
	 * All [AnalogEdgeView]s in a [AnalogGraphView] experience the same [currentFlowAnimationTick] tick.
	 */
	var animationOffset: Double = 0.0
		private set

	/** Repeatedly called by [AnalogGraphView] to drive the current flow animation. */
	fun currentFlowAnimationTick(systemSpeed: SystemSpeed) {
		val factor = systemSpeed.speed * FACTOR
		val delta = abs(current * factor).coerceAtMost(MAX_DELTA)
		val newOffset = animationOffset + delta
		animationOffset = if (newOffset >= CurrentFlowVisualization.DISTANCE) {
			0.0
		} else {
			animationOffset + delta
		}
	}

	override fun draw(context: DrawContext) {
		val graphAppContext = context.castedAppContext<GraphApplicationContext>()!!

		context.color =
			if (graphAppContext.showNetState) {
				if (model.isError) {
					Themes.get<AntaresTheme>().error
				} else {
					AnalogSignalColor.ofVoltage(getNodeVoltage(0))
				}
			} else {
				context.choose(color)
			}

		context.g.stroke = stroke

		super.draw(context)

		if (graphAppContext.showNetState) {
			CurrentFlowVisualization.draw(this, context)
		} else {
			//drawBeginConnectionAnnotation(context)
		}
	}

	override fun executionStarted(signalHandler: SignalHandler) {
		super.executionStarted(signalHandler)
		current = 0.0
		animationOffset = 0.0
	}

	override fun getExecutionTooltipContent(): String =
		Translations.getString("antares.analogEdgeView.simTooltipContent", model.signal!!.voltage, abs(current))

	/** ---- [AnalogElement] */

	private lateinit var nodes: Array<Int>

	private lateinit var volts: Array<Double>

	override val isNonLinear: Boolean get() = false

	override val voltageSourceCount: Int get() = 0

	private var voltageSource: Int = 0

	override val postCount: Int get() = 2

	override fun allocateNodes() {
		nodes = Array(postCount) { 0 }
		volts = Array(postCount) { 0.0 }
	}

	override fun setNode(postId: Int, nodeId: Int) {
		nodes[postId] = nodeId
	}

	override fun setNodeVoltage(postId: Int, voltage: Double) {
		volts[postId] = voltage
		calculateCurrent()
	}

	override fun getNodeVoltage(postId: Int): Double = volts[postId]

	override fun setCurrent(index: Int, current: Double) {
		this.current = current
	}

	override fun getPost(elem: GraphElementView<*>, postId: Int): Connection<*>? = null

	override fun setVoltageSource(index: Int, sourceId: Int) {
		voltageSource = sourceId
	}

	override fun stamp(analysis: FalstadAnalogCircuitAnalysis) {
		analysis.stampResistor(nodes[0], nodes[1], DEF_RESISTANCE)
	}

	override fun calculateCurrent() {
		current = (volts[0] - volts[1]) / DEF_RESISTANCE
	}
}