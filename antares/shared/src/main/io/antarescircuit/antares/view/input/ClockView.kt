package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.model.input.Clock
import io.antarescircuit.antares.model.input.PeriodOrFrequencyParser
import io.antarescircuit.antares.view.gate.BoxGateView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rotation.*
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.graphics.Cursor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.graph.ui.knob.KnobLauncherImpl
import io.antarescircuit.jabbah.graph.ui.knob.KnobView
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.vertice.VerticeViewActorInteractionHandler
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.math.max

/**
 * A view representation of a [Clock].
 */
class ClockView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Clock = Clock()
) : BoxGateView<Clock>(styleProvider, "", model), ControlViewSource<Clock> {

	companion object {

		const val PROP_ICON_PATH = "io.antarescircuit.antares.view.input.ClockView.iconPath"

		private const val SEG_X = Look.SCALE.toDouble()
		private const val SEG_Y_HALF = SEG_X * 3 / 4
		private val ANNOTATION_PATH = System.createPath()
			.moveTo(-SEG_X * 1.5, SEG_Y_HALF)
			.lineTo(-SEG_X * 0.6, SEG_Y_HALF)
			.lineTo(-SEG_X * 0.6, -SEG_Y_HALF)
			.lineTo(SEG_X * 0.6, -SEG_Y_HALF)
			.lineTo(SEG_X * 0.6, SEG_Y_HALF)
			.lineTo(SEG_X * 1.5, SEG_Y_HALF)
	}

	private val actorInteractionHandler = ClockViewActorInteractionHandler()

	init {
		initExternalLabel(Direction.WEST)
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: Clock?) {
		super.modelExchanged(oldModel)
		addPortView(createOutputPortView(model.getOutput(), PortLabelPosition.HIDE))
		updateLayout()
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(-AbstractAntaresPortView.LENGTH - width - LABEL_DIST, 0.0)

	/** ---- UI Properties */

	@Suppress("unused") // Reflection
	var periodOrFrequency: String
		get() = model.periodOrFrequency.toString()
		set(value) {
			model.periodOrFrequency = PeriodOrFrequencyParser.parse(value)
		}

	@Suppress("unused") // Reflection
	var offPercentage: Double
		get() = model.offPercentage
		set(value) {
			model.offPercentage = value
		}

	var isEnabled: Boolean
		get() = model.isEnabled
		set(value) {
			model.isEnabled = value
		}

	/** Determines whether the [KnobView] can be displayed during simulation for changing the propagation delay.*/
	var isKnobEnabled: Boolean = true

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeBoolean("knobEnabled", isKnobEnabled)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("knobEnabled")) {
			isKnobEnabled = reader.readBoolean("knobEnabled")
		}
	}

	/** ---- [Drawable] */

	override fun drawImpl(context: DrawContext) {
		val oldColor = context.g.color
		super.drawImpl(context)

		context.g.color = getApplicableForegroundColor(context)
		context.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke

		drawIconPath(context)

		context.g.color = oldColor
	}

	private fun drawIconPath(context: DrawContext) {
		val dx = when (rotation) {
			R0, R180 -> bounds.centerX
			R90 -> bounds.centerX + bounds.width / 5
			R270 -> bounds.centerX - bounds.width / 5
		}

		val dy = when (rotation) {
			R0 -> bounds.centerY - bounds.height / 5
			R90, R270 -> bounds.centerY
			R180 -> bounds.centerY + bounds.height / 5
		}

		context.translatedAndRotated(dx, dy, rotation.inverse().angle) {
			it.g.draw(ANNOTATION_PATH)
		}
	}

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		VerticeViewActorInteractionHandler.getInactiveInstance(this)
		return actorInteractionHandler
	}

	/** ---- [ActorView] */

	override val executionTooltipSubtext: String get() {
		val durationMillis = System.currentTimeMillis() - model.cycleCountStartTime
		val frequency = model.cycleCount / max(1, durationMillis / 1_000) / 2
		val frequencyText = Translations.getString("antares.clock.frequency.text", StringUtils.formatLong(frequency, '\'')) + " Hz"
		val subtext = super.executionTooltipSubtext
		if (StringUtils.isNotEmpty(subtext)) {
			return "$subtext\n$frequencyText"
		}
		return frequencyText
	}

	override fun <T: InputEventContext> getExecutionTooltip(context: T): Tooltip? {
		executionTooltip.reset()
		return super<BoxGateView>.getExecutionTooltip(context)
	}

	/** ---- [ControlViewSource] */

	override val controlId: String get() = "clock:${model.id}"

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<Clock> = ClockControlView(model)

	/** ---- [ClockView] */

	private inner class ClockViewActorInteractionHandler : VerticeViewActorInteractionHandler() {

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {

			if (!isEnabled) {
				context.view.setCursor(Cursor.CLICK)
				return null
			}

			if (!isKnobEnabled) {
				return null
			}

			return KnobLauncherImpl.launchAfterDelay(
				initialValue = model.propagationDelay.value / 1_000,
				location = boundingBox.center,
				unit = "µs",
				mouseMovedCondition = { contains(it.x, it.y) },
				valueChangeHandler = {
					if (it < Long.MAX_VALUE / 1_000) {
						model.propagationDelay = LongValueImpl(it * 1_000)
					}
				},
				signalHandler = context.signalHandler
			)
		}

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {

			if (!isEnabled) {
				model.toggle(context.signalHandler)
				return null
			}

			if (!isKnobEnabled) {
				return null
			}

			return KnobLauncherImpl.launchImmediately(
				view = context.view as DrawingView<*>,
				initialValue = model.propagationDelay.value / 1_000,
				location = boundingBox.center,
				unit = "µs",
				mouseMovedCondition = { contains(it.x, it.y) },
				valueChangeHandler = {
					if (it < Long.MAX_VALUE / 1_000) {
						model.propagationDelay = LongValueImpl(it * 1_000)
					}
				},
				signalHandler = context.signalHandler
			)
		}
	}
}