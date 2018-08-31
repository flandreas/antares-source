package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Clock
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.gate.AbstractDigitalGateView
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A view representation of a [Clock].
 */
class ClockView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Clock = Clock()
) : AbstractDigitalGateView<Clock>(styleProvider, "", "library.element.Clock", model) {

    companion object {
        private const val SEG_X = Look.SCALE
        private const val SEG_Y = SEG_X * 3 / 2
        private val ICON_PATH = createIconPath()

        private fun createIconPath(): Path {
            return System.get().createPath()
                .moveTo(0, 0)
                .lineTo(SEG_X, 0)
                .lineTo(SEG_X, -SEG_Y)
                .lineTo(2 * SEG_X, -SEG_Y)
                .lineTo(2 * SEG_X, 0)
                .lineTo(3 * SEG_X, 0)
        }
    }

	private val actorInteractionHandler = ClockViewActorInteractionHandler()

	/** The [KnobView] for changing the propagation delay during execution.*/
	private var propagationDelayKnob: KnobView? = null

	init {
        modelExchanged(null)
    }

    /** ---- UI Properties */

    /** Contains the period of this [ClockView] in microseconds.*/
    var period: Long
        get() = model!!.propagationDelay / 1_000
        set(value) {
            model!!.propagationDelay = value * 1_000
        }

    var isEnabled: Boolean
        get() = model!!.isEnabled
        set(value) {
            model!!.isEnabled = value
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
        context.g.stroke = styleProvider.getStyle(GraphStyleType.ANNOTATION).stroke

        val dx = bounds.centerX - 3 * SEG_X / 2
        val dy = bounds.centerY - SEG_Y / 2

        context.g.translate(dx, dy)
        context.g.draw(ICON_PATH)
        context.g.translate(-dx, -dy)

        context.g.color = oldColor
    }

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler? {
		return actorInteractionHandler
	}

	private fun showPropagationDelayKnob(view: DrawingView<*>) {
		view.content.animationContainer.add(propagationDelayKnob!!)
		view.content.animationContainer.validate()
	}

	/** ---- [ClockView] */

	private inner class ClockViewActorInteractionHandler : DefaultActionInteractionHandler() {
		override fun mouseMoved(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!isKnobEnabled) {
				return null
			}

			if (propagationDelayKnob == null) {
				propagationDelayKnob = KnobView(unit = "µs", valueChangeHandler = { model!!.propagationDelay = it * 1000})
				propagationDelayKnob!!.location = Point2D(boundingBox.center.subtract(Point2D(KnobView.OUTER_SIZE / 2, KnobView.OUTER_SIZE / 2)))
			}
			propagationDelayKnob!!.value = model!!.propagationDelay / 1000
			showPropagationDelayKnob(context.view as DrawingView<*>)
			return null
		}
	}
}