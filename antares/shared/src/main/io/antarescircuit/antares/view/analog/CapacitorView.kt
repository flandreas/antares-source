package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.Capacitor
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.InputEventHandler
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.ui.knob.KnobLauncherImpl
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

class CapacitorView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Capacitor = Capacitor()
) : AbstractAnalogVerticeView<Capacitor>(styleProvider, model, Direction.NORTH, Rectangle2D(LENGTH, -SIZE / 2, SIZE, SIZE)
) {

    companion object {
        private val SIZE = wInt(4)
        private val BAR_WIDTH = w(0.4)
        private val BAR_HEIGHT = h(4)
    }

    @Suppress("unused") // Reflective bean property
    var capacitance: MagnitudeValue
        get() = model.capacitance
        set(value) {
            model.capacitance = value
        }

    @Suppress("unused") // Reflective bean property
    var variable: Boolean
        get() = model.variable
        set(value) {
            model.variable = value
        }

    override val relativeExternalLabelLocation: Point2D get() = Point2D(bounds.centerX, bounds.minY - LABEL_DIST)

    private val actorInteractionHandler by lazy { CapacitorViewInteractionHandler() }

    /** ---- [AbstractVerticeView] */

    override fun modelExchanged(oldModel: Capacitor?) {
        super.modelExchanged(oldModel)
        addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + SIZE, 0, Direction.EAST))
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        with (context.g) {
            stroke = styleProvider.getStyle(GraphStyleType.EDGE).stroke

            // Left side (port 1)
            // Don't draw shadow, doesn't look good
            (getPortView(model.getPort(1)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
            drawLine(x, y + SIZE / 2, x + w(1.25), y + SIZE / 2)
            if (!context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
                context.g.color = context.chooseForeground(this@CapacitorView.foregroundColor)
            }
            fillRect(x + w(1.25), y + SIZE / 2 - BAR_HEIGHT / 2, BAR_WIDTH, BAR_HEIGHT)

            // Right side (port 2)
            // Don't draw shadow, doesn't look good
            (getPortView(model.getPort(2)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
            drawLine(x + SIZE, y + SIZE / 2, x + SIZE - w(1.25), y + SIZE / 2)
            if (!context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
                context.g.color = context.chooseForeground(this@CapacitorView.foregroundColor)
            }
            fillRect(x + SIZE - w(1.25) - BAR_WIDTH, y + SIZE / 2 - BAR_HEIGHT / 2, BAR_WIDTH, BAR_HEIGHT)

            if (variable) {
                context.g.stroke = this@CapacitorView.stroke
                context.translated(x, y + SIZE / 2) {
                    SymbolStyle.drawVariableArrow(context, SIZE / 2.0, 0.8)
                }
            }
        }
    }

    override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
        if (variable) {
            actorInteractionHandler
        } else {
            super.getActorInteractionHandler(context)
        }

    /** ---- [AbstractAnalogVerticeView] */

    override val mainPropertyValue: String get() = capacitance.toString()

    /** ---- [CapacitorView] */

    private inner class CapacitorViewInteractionHandler : Companion.CannotOpenActorClickHandler() {

        init {
            component = this@CapacitorView
        }

        override fun mouseMoved(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
            return KnobLauncherImpl.launchAfterDelay(
                initialValue = model.capacitance,
                location = boundingBox.center,
                mouseMovedCondition = { contains(it.x, it.y) },
                valueChangeHandler = { model.setState(it, context.signalHandler, (context.view as DrawingView<*,*>).drawing as AnalogGraphView) },
                signalHandler = context.signalHandler
            )
        }

        override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
            return KnobLauncherImpl.launchImmediately(
                view = context.view as DrawingView<*,*>,
                initialValue = model.capacitance,
                location = boundingBox.center,
                mouseMovedCondition = { contains(it.x, it.y) },
                valueChangeHandler = { model.setState(it, context.signalHandler, (context.view as DrawingView<*,*>).drawing as AnalogGraphView) },
                signalHandler = context.signalHandler
            )
        }
    }
}