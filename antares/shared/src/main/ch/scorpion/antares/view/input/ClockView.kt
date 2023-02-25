package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Clock
import ch.scorpion.antares.model.input.PeriodOrFrequencyParser
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.gate.BoxGateView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Tooltip
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Rotation.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.ui.KnobLauncherImpl
import ch.scorpion.jabbah.graph.ui.KnobView
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.VerticeViewActorInteractionHandler
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.max

/**
 * A view representation of a [Clock].
 */
class ClockView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Clock = Clock()
) : BoxGateView<Clock>(styleProvider, "", model), ControlViewSource<Clock> {

	companion object {

		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.ClockView.iconPath"

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
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: Clock?) {
		super.modelExchanged(oldModel)
		addPortView(createOutputPortView(model.getOutput()))
		updateLayout()
	}

	/** ---- UI Properties */

	@Suppress("unused") // Reflection
	var periodOrFrequency: String
		get() = model.periodOrFrequency.toString()
		set(value) {
			model.periodOrFrequency = PeriodOrFrequencyParser.parse(value)
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

		context.g.translate(dx, dy)
		context.g.rotate(rotation.inverse().angle)
		context.g.draw(ANNOTATION_PATH)
		context.g.rotate(-rotation.inverse().angle)
		context.g.translate(-dx, -dy)
	}

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		VerticeViewActorInteractionHandler.getInactiveInstance(this)
		return actorInteractionHandler
	}

	/** ---- [ActorView] */

	override val executionTooltipSubtext: String get() {
		val durationMillis = System.currentTimeMillis() - model.realStartTime
		val frequency = model.cycleCount / max(1, durationMillis / 1_000) / 2
		val frequencyText = Translations.getString("antares.clock.frequency.text", StringUtils.formatLong(frequency, '\'')) + " Hz"
		val subtext = super.executionTooltipSubtext
		if (StringUtils.isNotEmpty(subtext)) {
			return "$subtext\n$frequencyText"
		}
		return frequencyText
	}

	override fun getExecutionTooltip(x: Double, y: Double): Tooltip? {
		executionTooltip.reset()
		return super<BoxGateView>.getExecutionTooltip(x, y)
	}

	/** ---- [ControlViewSource] */

	override val controlId: String get() = "clock:${model.id}"

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<Clock> = ClockControlView(model)

	/** ---- [ClockView] */

	private inner class ClockViewActorInteractionHandler : VerticeViewActorInteractionHandler() {

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {

			if (!isEnabled || !isKnobEnabled) {
				return null
			}

			return KnobLauncherImpl.launchAfterDelay(
				initialValue = model.propagationDelay / 1_000,
				location = boundingBox.center,
				unit = "µs",
				mouseMovedCondition = { contains(it.x, it.y) },
				valueChangeHandler = { model.propagationDelay = it * 1_000 }
			)
		}

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {

			if (!isEnabled || !isKnobEnabled) {
				return null
			}

			return KnobLauncherImpl.launchImmediately(
				view = context.view as DrawingView<*>,
				initialValue = model.propagationDelay / 1_000,
				location = boundingBox.center,
				unit = "µs",
				mouseMovedCondition = { contains(it.x, it.y) },
				valueChangeHandler = { model.propagationDelay = it * 1_000 }
			)
		}
	}
}