package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogNet
import io.antarescircuit.antares.model.analog.AnalogPort
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.view.analog.engine.AnalogCircuitAnalysis
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.PreferencesChangedEvent
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.view.Connection
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewImpl
import io.antarescircuit.jabbah.graph.view.style.EdgeStyle
import kotlin.math.abs

/**
 * During simulation, [AnalogEdgeView] is treated as resistor with a very small resistance.
 */
class AnalogEdgeView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	net: AnalogNet = AnalogNet()
) : EdgeViewImpl<AnalogSignal>(styleProvider, net), AnalogElement {

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
		val factor = if (systemSpeed.isPaused) 0.0F else animationSpeedFactor * CurrentFlowAnimationSpeed.speed

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

		context.g.stroke = if (graphAppContext.isExecute) {
			(style as EdgeStyle).executionStroke
		} else {
			style.stroke
		}

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
			AnalogSignal.roundVoltage(model.signal!!.voltage),
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

	override fun getNode(postId: Int): Int = analogNet.getNode(id, postId)

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

	override fun getVoltageSource(index: Int): Int = 0

	override fun stamp(analysis: AnalogCircuitAnalysis) {
		analysis.stampResistor(
			analogNet.getNode(id, 0), analogNet.getNode(id, 1), DEF_RESISTANCE
		)
	}

	override fun calculateCurrent() {
		current = (analogNet.getNodeVoltage(id, 0) - analogNet.getNodeVoltage(id, 1)) / DEF_RESISTANCE
	}
}