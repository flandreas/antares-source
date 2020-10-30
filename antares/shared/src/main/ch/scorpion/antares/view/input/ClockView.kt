package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Clock
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Rotation.*
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.ui.KnobLauncher
import ch.scorpion.jabbah.graph.ui.KnobView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A view representation of a [Clock].
 */
class ClockView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Clock = Clock()
) : AbstractDigitalGateView<Clock>(styleProvider, "", model) {

	companion object {
		private const val SEG_X = Look.SCALE.toDouble()
		private const val SEG_Y_HALF = SEG_X * 3 / 4
		private val ICON_PATH = createIconPath()

		private fun createIconPath(): Path {
			return System.createPath()
				.moveTo(-SEG_X * 1.5, SEG_Y_HALF)
				.lineTo(-SEG_X * 0.6, SEG_Y_HALF)
				.lineTo(-SEG_X * 0.6, -SEG_Y_HALF)
				.lineTo(SEG_X * 0.6, -SEG_Y_HALF)
				.lineTo(SEG_X * 0.6, SEG_Y_HALF)
				.lineTo(SEG_X * 1.5, SEG_Y_HALF)
		}
	}

	private val actorInteractionHandler = ClockViewActorInteractionHandler()

	init {
		modelExchanged(null)
	}

	/** ---- UI Properties */

	/** Contains the period of this [ClockView] in microseconds.*/
	var period: Long
		get() = model.propagationDelay / 1_000
		set(value) {
			model.propagationDelay = value * 1_000
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

		if (context.useContextColors) {
			context.g.color = context.color!!.foregroundColor
		} else {
			context.g.color = foregroundColor
		}
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
		context.g.draw(ICON_PATH)
		context.g.rotate(-rotation.inverse().angle)
		context.g.translate(-dx, -dy)
	}

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		return actorInteractionHandler
	}

	/** ---- [ClockView] */

	private inner class ClockViewActorInteractionHandler : AbstractVerticeView.Companion.CannotOpenActorClickHandler() {
		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {

			if (!isEnabled || !isKnobEnabled) {
				return null
			}

			return KnobLauncher.launchAfterDelay(
				initialValue = model.propagationDelay / 1_000,
				location = boundingBox.center,
				unit = "µs",
				mouseMovedCondition = { contains(it.x, it.y) },
				valueChangeHandler = { model.propagationDelay = it * 1_000 }
			)
		}
	}
}