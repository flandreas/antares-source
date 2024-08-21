package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.model.analog.AnalogPort
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.analog.engine.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
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

		/** The name of the [Int] preference in [Properties] holding the current flow animation speed factor. */
		const val PREF_SPEED = "antares.analog.currentFlowAnimationSpeed"
		const val MIN_SPEED = 1
		const val DEF_SPEED = 5
		const val MAX_SPEED = 10

		/**
		 * Determines the speed of the current flow animation by multiplication with [SystemSpeed].
		 * Cannot be initialized from [Properties] on JS platform.
		 */
		private var _animationSpeedFactor: Float? = null
		private val animationSpeedFactor: Float get() {
			if (_animationSpeedFactor == null) {
				_animationSpeedFactor = BaseModule.properties.getInt(PREF_SPEED) / 5.0F
			}
			return _animationSpeedFactor!!
		}

		/** Limits the effective speed of the current flow animation.*/
		private const val MAX_DELTA = 2.7

		private const val DEF_RESISTANCE = 1E-06

		init {
			BaseModule.eventBus.register(PreferencesChangedEvent::class) {
				_animationSpeedFactor = BaseModule.properties.getInt(PREF_SPEED) / 5.0F
			}
		}
	}

	private val analogNet: AnalogNet get() = model as AnalogNet

	/**
	 * The electrical current (in A) flowing through this [AnalogEdgeView] during simulation.
	 * Positive values indicate current flowing from [origin] to [destination].
	 * Can't be modelled on model [Net] because [Net] doesn't contain nodes, which
	 * is required for modelling Kirchhoff's "Current Law".
	 */
	var current: Double
		get() = analogNet.getCurrent(id)
		set(value) {
			analogNet.setCurrent(id, value)
			(origin?.port as AnalogPort?)?.current = value
			(destination?.port as AnalogPort?)?.current = value
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
		// Before #802, current animation speed was also depending on the SystemSpeed.
		// We got rid of that, and 50 is just the middle of the SystemSpeed range.
		val factor = if (systemSpeed.isPaused) 0.0F else animationSpeedFactor * 50

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
		animationOffset = 0.0
	}

	override fun executionStopped(signalHandler: SignalHandler) {
		super.executionStopped(signalHandler)
		current = 0.0
		animationOffset = 0.0
	}

	override fun getExecutionTooltipContent(): String =
		Translations.getString("antares.analogEdgeView.simTooltipContent",
			model.signal!!.roundedDesc(100),
			AnalogSignal.roundAbsCurrent(current))

	/** ---- [AnalogElement] */

	override val isNonLinear: Boolean get() = false

	override val voltageSourceCount: Int get() = 0

	override val postCount: Int get() = 2

	override fun reset() { }

	override fun allocateNodes() {
		// not needed, managed in AnalogNet
	}

	override fun setNode(postId: Int, nodeId: Int) {
		analogNet.setNode(id, postId, nodeId)
	}

	override fun setNodeVoltage(postId: Int, voltage: Double) {
		analogNet.setNodeVoltage(id, postId, voltage)
		calculateCurrent()
	}

	override fun getNodeVoltage(postId: Int): Double =
		analogNet.getNodeVoltage(id, postId)

	override fun setInternalCurrent(index: Int, current: Double) { }

	override fun getInternalCurrent(): Double = 0.0

	override fun getPost(elem: GraphElementView<*>, postId: Int): Connection<*>? = null

	override fun setVoltageSource(index: Int, sourceId: Int) {
		// voltageSourceCount is 0, so this is not needed
	}

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		analysis.stampResistor(
			analogNet.getNode(id, 0), analogNet.getNode(id, 1), DEF_RESISTANCE
		)
	}

	override fun calculateCurrent() {
		current = (analogNet.getNodeVoltage(id, 0) - analogNet.getNodeVoltage(id, 1)) / DEF_RESISTANCE
	}
}