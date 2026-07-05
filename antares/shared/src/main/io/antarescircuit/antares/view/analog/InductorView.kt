package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.Inductor
import io.antarescircuit.antares.view.module.AntaresViewModule
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle.Companion.INDUCTOR_HEIGHT_HALF
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle.Companion.INDUCTOR_WIDTH
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
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView.Companion

class InductorView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Inductor = Inductor()
) : AbstractAnalogVerticeView<Inductor>(
    styleProvider,
    model,
    Direction.NORTH,
    Rectangle2D(
        // Provide 10 more vertical space for the "variable" arrow
        LENGTH.toDouble(), -INDUCTOR_HEIGHT_HALF - 10,
        LENGTH.toDouble() + INDUCTOR_WIDTH, 2 * INDUCTOR_HEIGHT_HALF + 10
    )
) {

    @Suppress("unused") // Reflection
    var inductance: MagnitudeValue
        get() = model.inductance
        set(value) {
            model.inductance = value
        }

    @Suppress("unused") // Reflective bean property
    var variable: Boolean
        get() = model.variable
        set(value) {
            model.variable = value
        }

    override val relativeExternalLabelLocation: Point2D get() = Point2D(bounds.centerX, bounds.minY - LABEL_DIST)

    private val actorInteractionHandler by lazy { InductorViewInteractionHandler() }

    /** ---- [AbstractVerticeView] */

    override fun modelExchanged(oldModel: Inductor?) {
        super.modelExchanged(oldModel)
        addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + INDUCTOR_WIDTH.toInt(), 0, Direction.EAST))
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        val applicableForegroundColor = if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
            getColorGradient(context, 2, 1) ?: styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor
        } else {
            context.chooseForeground(foregroundColor)
        }

        AntaresViewModule.currentSymbolStyle.symbolStyle.drawInductor(
            this,
            true,
            context,
            applicableForegroundColor,
            context.chooseBackground(backgroundColor),
            SymbolStyle.INDUCTOR_STROKE)

        if (variable) {
            context.g.stroke = this@InductorView.stroke
            context.translated(x, y + INDUCTOR_HEIGHT_HALF + 9) {
                SymbolStyle.drawVariableArrow(context, INDUCTOR_WIDTH / 2.0, 0.46)
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

    override val mainPropertyValue: String get() = inductance.toString()

    /** ---- [InductorView] */

    private inner class InductorViewInteractionHandler : Companion.CannotOpenActorClickHandler() {

        init {
            component = this@InductorView
        }

        override fun mouseMoved(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext>? {
            return KnobLauncherImpl.launchAfterDelay(
                initialValue = model.inductance,
                location = boundingBox.center,
                mouseMovedCondition = { contains(it.x, it.y) },
                valueChangeHandler = { model.setState(it, context.signalHandler, (context.view as DrawingView<*,*>).drawing as AnalogGraphView) },
                signalHandler = context.signalHandler
            )
        }

        override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
            return KnobLauncherImpl.launchImmediately(
                view = context.view as DrawingView<*,*>,
                initialValue = model.inductance,
                location = boundingBox.center,
                mouseMovedCondition = { contains(it.x, it.y) },
                valueChangeHandler = { model.setState(it, context.signalHandler, (context.view as DrawingView<*,*>).drawing as AnalogGraphView) },
                signalHandler = context.signalHandler
            )
        }
    }
}