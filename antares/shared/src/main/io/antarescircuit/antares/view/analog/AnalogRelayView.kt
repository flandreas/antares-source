package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogRelay
import io.antarescircuit.antares.model.input.SwitchConfiguration
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.antares.view.input.AbstractSwitchView
import io.antarescircuit.antares.view.input.AbstractSwitchView.Companion.DEF_CIRCLE_RADIUS
import io.antarescircuit.antares.view.module.AntaresViewModule
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle.Companion.INDUCTOR_HEIGHT_HALF
import io.antarescircuit.antares.view.symbolstyle.SymbolStyle.Companion.INDUCTOR_WIDTH
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView

class AnalogRelayView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: AnalogRelay = AnalogRelay()
) : AbstractAnalogVerticeView<AnalogRelay>(styleProvider, model, Direction.NORTH, Rectangle2D()) {

    @Suppress("unused") // Reflection
    var switchConfiguration: SwitchConfiguration
        get() = model.switchConfiguration
        set(value) {
            if (value != model.switchConfiguration) {
                invalidate()
                model.switchConfiguration = value
                modelExchanged(model)
                invalidate()
                update()
            }
        }

    @Suppress("unused") // Reflection
    var inductance: Double
        get() = model.inductance
        set(value) {
            model.inductance = value
        }

    @Suppress("unused") // Reflection
    var onCurrent: Double
        get() = model.onCurrent
        set(value) {
            model.onCurrent = value
        }

    @Suppress("unused") // Reflection
    var normallyOn: Boolean
        get() = model.normallyOn
        set(value) {
            if (value != model.normallyOn) {
                invalidate()
                model.normallyOn = value
                updateSPSTGeometry()
                invalidate()
                validate()
                update()
            }
        }

    override val relativeExternalLabelLocation: Point2D
        get() = Point2D(bounds.centerX, bounds.minY - LABEL_DIST)

    /** ---- [AbstractVerticeView] */

    override fun modelExchanged(oldModel: AnalogRelay?) {
        super.modelExchanged(oldModel)

        var portId = 1

        when (model.switchConfiguration) {
            SwitchConfiguration.SPST -> {
                // Single-throw switch
                addPortView(AnalogPortView(styleProvider, model.getPort(portId++), LENGTH, 4 * Look.SCALE, Direction.WEST))
                addPortView(AnalogPortView(styleProvider, model.getPort(portId++), LENGTH + INDUCTOR_WIDTH.toInt(), 4 * Look.SCALE, Direction.EAST))
                updateSPSTGeometry()
                setBounds(LENGTH.toDouble(), -INDUCTOR_HEIGHT_HALF, LENGTH + INDUCTOR_WIDTH, 8.0 * Look.SCALE)
            }
            SwitchConfiguration.SPDT -> {
                // Double-throw switch
                addPortView(AnalogPortView(styleProvider, model.getPort(portId++), LENGTH, 5 * Look.SCALE, Direction.WEST))
                addPortView(AnalogPortView(styleProvider, model.getPort(portId++), LENGTH + INDUCTOR_WIDTH.toInt(), 3 * Look.SCALE, Direction.EAST))
                addPortView(AnalogPortView(styleProvider, model.getPort(portId++), LENGTH + INDUCTOR_WIDTH.toInt(), 7 * Look.SCALE, Direction.EAST))
                setBounds(LENGTH.toDouble(), -INDUCTOR_HEIGHT_HALF, LENGTH + INDUCTOR_WIDTH, 9.0 * Look.SCALE)
            }
        }

        // Coil
        addPortView(AnalogPortView(styleProvider, model.getPort(portId++), LENGTH, 0, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(portId), LENGTH + INDUCTOR_WIDTH.toInt(), 0, Direction.EAST))

        updateGeometry()
        updateMainPropertyLabel()
    }

    private fun updateSPSTGeometry() {
        if (model.switchConfiguration != SwitchConfiguration.SPST) {
            return
        }
        getPortView(model.getPort(2))!!.location = if (normallyOn) {
            Point2D(LENGTH + INDUCTOR_WIDTH.toInt(), 6 * Look.SCALE)
        } else {
            Point2D(LENGTH + INDUCTOR_WIDTH.toInt(), 4 * Look.SCALE)
        }
        updateGeometry()
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        val applicableForegroundColor = if (context.castedAppContext<GraphApplicationContext>()!!.showNetState) {
            getColorGradient(context, model.coilPortIdBase + 1, model.coilPortIdBase) ?: styleProvider.getStyle(GraphStyleType.EDGE).color.foregroundColor
        } else {
            context.chooseForeground(foregroundColor)
        }

        AntaresViewModule.currentSymbolStyle.symbolStyle.drawInductor(
            this,
            false,
            context,
            applicableForegroundColor,
            context.chooseBackground(backgroundColor),
            SymbolStyle.INDUCTOR_STROKE
        )

        when (model.switchConfiguration) {
            SwitchConfiguration.SPST -> {
                context.translated(0.0, 4.0 * Look.SCALE) {
                    if (normallyOn) {
                        AbstractSwitchView.drawTwoPortRealSwitchNonColinearShape(this, 1, model.isOn, context, bounds.minX, DEF_CIRCLE_RADIUS)
                    } else {
                        AbstractSwitchView.drawTwoPortRealSwitchShape(this, 1, model.isOn, context, bounds.minX, DEF_CIRCLE_RADIUS, false, leftHanded = false)
                    }
                }
            }
            SwitchConfiguration.SPDT -> {
                context.translated(0.0, 5.0 * Look.SCALE) {
                    AbstractSwitchView.drawThreePortRealSwitchShape(this, 1, model.isOn, context, bounds.minX, DEF_CIRCLE_RADIUS, false)
                }
            }
        }

        // Draw iron core
        context.g.color = context.chooseForeground(foregroundColor)
        context.g.fillRect(LENGTH.toDouble(), 1.7 * Look.SCALE, INDUCTOR_WIDTH, 0.4 *Look.SCALE)
    }
}