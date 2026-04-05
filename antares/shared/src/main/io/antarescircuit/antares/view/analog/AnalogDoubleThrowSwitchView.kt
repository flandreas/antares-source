package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AbstractAnalogVertice
import io.antarescircuit.antares.model.analog.AnalogDoubleThrowSwitch
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.antares.view.analog.engine.AnalogElement
import io.antarescircuit.antares.view.analog.engine.AnalogElementProxy
import io.antarescircuit.antares.view.input.AbstractSwitchView
import io.antarescircuit.antares.view.input.DoubleThrowSwitchView.Companion.HEIGHT
import io.antarescircuit.antares.view.input.DoubleThrowSwitchView.Companion.WIDTH
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

class AnalogDoubleThrowSwitchView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: AnalogDoubleThrowSwitch = AnalogDoubleThrowSwitch(),
    private val analogElement: AnalogElementProxy = AnalogElementProxy()
) : AbstractSwitchView<AnalogDoubleThrowSwitch>(styleProvider, model),
    AnalogElement by analogElement
{
    companion object {
        const val PROP_ICON_PATH = "io.antarescircuit.antares.AnalogDoubleThrowSwitchView.iconPath"
    }

    init {
        initExternalLabel(Direction.NORTH)
        isFocusable = true
        modelExchanged(null)
        setBounds(LENGTH, h(-3.5).toInt(), WIDTH, HEIGHT)
    }

    override val relativeExternalLabelLocation: Point2D get() =
        Point2D(LENGTH + REAL_SWITCH_WIDTH / 2, -REAL_SWITCH_HEIGHT_ABOVE - LABEL_DIST)

    override fun modelExchanged(oldModel: AnalogDoubleThrowSwitch?) {
        super.modelExchanged(oldModel)
        analogElement.bind(model)

        addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + WIDTH, -2 * SCALE, Direction.EAST))
        addPortView(AnalogPortView(styleProvider, model.getPort(3), LENGTH + WIDTH, 2 * SCALE, Direction.EAST))
    }

    /** ---- [AbstractVerticeView] */

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)
        drawThreePortRealSwitchShape(context)
    }

    override fun getEditPortViewColor(styleProvider: StyleProvider): CompositeColor =
        customColor?.color ?: super.getEditPortViewColor(styleProvider)

    /** ---- [AnalogSwitchView] */

    override fun handleStateChanged(event: GraphElementEvent) {
        super.handleStateChanged(event)
        if (event.reason == AbstractAnalogVertice.REQUEST_REANALYZE) {
            if (event.signalHandler != null && parent is AnalogGraphView) {
                (parent as AnalogGraphView).recalculate(event.signalHandler!!, true)
            }
        }
    }
}