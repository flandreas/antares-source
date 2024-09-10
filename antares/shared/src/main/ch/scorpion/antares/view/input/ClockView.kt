package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Clock
import ch.scorpion.antares.model.input.PeriodOrFrequencyParser
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.gate.BoxGateView
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.geom.Rotation.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Rotatable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.Alignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.Labeled
import ch.scorpion.jabbah.edit.model.text.RotationDisplayStrategy
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.ui.KnobLauncherImpl
import ch.scorpion.jabbah.graph.ui.KnobView
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
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
) : BoxGateView<Clock>(styleProvider, "", model), ControlViewSource<Clock>, Labeled {

	companion object {

		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.ClockView.iconPath"

		private const val LABEL_DIST = Look.SCALE

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

	override val label = Label(
		font = font,
		text = model.name,
		rotationDisplayStrategy = RotationDisplayStrategy.KEEP_HORIZONTAL)

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: Clock?) {
		super.modelExchanged(oldModel)
		addPortView(createOutputPortView(model.getOutput()))
		updateLayout()
		updateLabel()
	}

	/** ---- UI Properties */

	var name: String?
		get() = model.name
		set(value) {
			model.name = value
		}

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

	/** ---- [Rotatable] */

	override var rotation: Rotation
		get() = super.rotation
		set(value) {
			if (value != super.rotation) {
				super.rotation = value
				invalidate()
				label.ownerRotation = value
				invalidate()
				validate()
			}
		}

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

		context.g.color = context.choose(color).textColor
		label.draw(context)

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

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		if (label.text != model.name) {
			invalidate()
			label.text = StringUtils.orEmpty(model.name)
		}
		super.handleStateChanged(event)
	}

	/** ---- [AbstractVerticeView] */

	override fun getBoundingBoxImpl(): Rectangle2D {
		val bb = super.getBoundingBoxImpl()
		if (StringUtils.isNotEmpty(label.text)) {
			val lbb = label.boundingBox.moveBy(location)
			bb.add(lbb)
		}
		return bb
	}

	/** ---- [ClockView] */

	/**
	 * Updates the text, the location and the alignments of the external [Label] depending
	 * on the orientation of this [ClockView].
	 */
	private fun updateLabel() {
		label.text = StringUtils.orEmpty(model.name)
		label.alignment = Alignment.forOrientation(orientation)
		label.location = when (orientation) {
			Direction.EAST -> Point2D(-getOutput().length - bounds.width - LABEL_DIST, 0.0)
			Direction.NORTH -> Point2D(0.0, getOutput().length + bounds.height + LABEL_DIST)
			Direction.WEST -> Point2D(getOutput().length + bounds.width + LABEL_DIST, 0.0)
			Direction.SOUTH -> Point2D(0.0, -getOutput().length - bounds.height - LABEL_DIST)
		}
	}

	private inner class ClockViewActorInteractionHandler : VerticeViewActorInteractionHandler() {

		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {

			if (!isEnabled || !isKnobEnabled) {
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
				}
			)
		}

		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {

			if (!isEnabled || !isKnobEnabled) {
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
				}
			)
		}
	}
}