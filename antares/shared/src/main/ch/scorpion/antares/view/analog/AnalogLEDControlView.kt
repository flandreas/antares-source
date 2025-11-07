package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogLED
import ch.scorpion.antares.model.analog.AnalogPort
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.output.LightEmitterModel
import ch.scorpion.antares.view.output.AbstractLEDView
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightColorExpression
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

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