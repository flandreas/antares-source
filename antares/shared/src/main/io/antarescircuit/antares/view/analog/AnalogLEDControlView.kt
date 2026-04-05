package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogLED
import io.antarescircuit.antares.model.analog.AnalogPort
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.model.output.LightEmitterModel
import io.antarescircuit.antares.view.output.AbstractLEDView
import io.antarescircuit.antares.view.output.LightColor
import io.antarescircuit.antares.view.output.LightColorExpression
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.graph.view.AbstractGraphElementView
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.port.PortView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * Draws the [ControlView] of a [AnalogLEDView] as a round LED bulb (without radial color gradient halo).
 */
class AnalogLEDControlView(
    styleProvider: StyleProvider =  DrawStyleModule.styleProvider,
    model: AnalogLED = AnalogLED(),
    private var lightColor: LightColor = LightColor.RED,
    private var minCurrent: Double = AnalogLEDView.DEF_MIN_GLOW_CURRENT,
    private var maxCurrent: Double = AnalogLEDView.DEF_MAX_GLOW_CURRENT
) : AbstractLEDView<AnalogLED>(styleProvider, model) {

    init {
        isShowPortViews = false
    }

    /** ---- [Storable] */

    override fun read(reader: StoreReader) {
        super.read(reader)
        lightColor = LightColor.read("lightColor", reader)
        minCurrent = reader.readDouble("minCurrent")
        maxCurrent = reader.readDouble("maxCurrent")
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        lightColor.write("lightColor", writer)
        writer.writeDouble("minCurrent", minCurrent)
        writer.writeDouble("maxCurrent", maxCurrent)
    }

    /** ---- [ControlView] */

    override val controlName: String get() = super.controlName

    /** ---- [ControlViewSource] */

    override val controlId: String get() = "analogLED:${model.id}"

    override val iconPath: String get() = BaseModule.properties.getString(AnalogLEDView.PROP_ICON_PATH)

    override fun createControlView(): ControlView<AnalogLED> {
        throw UnsupportedOperationException()
    }

    /** ---- [AbstractGraphElementView] */

    override fun bind(graphView: GraphView, deep: Boolean) {
        super.bind(graphView, deep)
        graphParamsChanged(graphView.graph!!)
    }

    override fun graphParamsChanged(graph: Graph) {
        (lightColor as? LightColorExpression)?.let { it.evaluateIn(graph)?.let { lc -> lightColor = lc } }
    }

    /** ---- [AbstractVerticeView] */

    override fun handleStateChanged(event: GraphElementEvent) {
        if (event.reason == AnalogLED.REASON_CURRENT) {
            invalidate()
            validate()
        } else if (event.reason == LightEmitterModel.REASON_GRAPH_PARAM_CHANGED && event.argument is Graph) {
            graphParamsChanged(event.argument as Graph)
        } else {
            super.handleStateChanged(event)
        }
    }

    /** ---- [AbstractLEDView] */

    override fun getBulbExecuteColor(): Color =
        lightColor.gradient.at(
            LightBulbView.getExecutionLightFactor(
                (model.getPort<AnalogSignal>() as AnalogPort).current,
                minCurrent,
                maxCurrent)
        )

    override fun sourcePropertiesChanged(source: ControlViewSource<AnalogLED>) {
        super.sourcePropertiesChanged(source)
        if (source is AnalogLEDView) {
            copyControlViewProperties(source, this)
        }
    }

    override fun createPortView(): PortView<*> =
        AnalogPortView(
            styleProvider,
            port = model.getInput(),
            direction = Direction.WEST)

    private fun copyControlViewProperties(source: AnalogLEDView, dest: AnalogLEDControlView) {
        dest.lightColor = source.lightColor
        dest.minCurrent = source.minCurrent
        dest.maxCurrent = source.maxCurrent
    }
}