package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AbstractAnalogVertice
import ch.scorpion.antares.model.analog.AnalogDoubleThrowSwitch
import ch.scorpion.jabbah.edit.Look.SCALE
import ch.scorpion.antares.view.analog.engine.AnalogElement
import ch.scorpion.antares.view.analog.engine.AnalogElementProxy
import ch.scorpion.antares.view.input.AbstractSwitchView
import ch.scorpion.antares.view.input.DoubleThrowSwitchView.Companion.HEIGHT
import ch.scorpion.antares.view.input.DoubleThrowSwitchView.Companion.WIDTH
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class AnalogDoubleThrowSwitchView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: AnalogDoubleThrowSwitch = AnalogDoubleThrowSwitch(),
    private val analogElement: AnalogElementProxy = AnalogElementProxy()
) : AbstractSwitchView<AnalogDoubleThrowSwitch>(styleProvider, model),
    AnalogElement by analogElement
{
    companion object {
        const val PROP_ICON_PATH = "ch.scorpion.antares.AnalogDoubleThrowSwitchView.iconPath"
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