package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.AnalogLED
import io.antarescircuit.antares.model.analog.AnalogPort
import io.antarescircuit.antares.model.analog.AnalogSignal
import io.antarescircuit.antares.model.output.LightEmitterModel
import io.antarescircuit.antares.view.module.AntaresViewModule
import io.antarescircuit.antares.view.output.LightColor
import io.antarescircuit.antares.view.output.LightColorExpression
import io.antarescircuit.antares.view.output.LightColorRadialGradientCache
import io.antarescircuit.antares.view.output.LightEmitter
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.Paint
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.view.*
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

class AnalogLEDView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: AnalogLED = AnalogLED(),
    lightColor: LightColor = DEFAULT_LIGHT_COLOR,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractDiodeView<AnalogLED>(styleProvider, model, Rectangle2D(LENGTH, -NEGATIVE_HEIGHT, SIZE, SIZE / 2 + NEGATIVE_HEIGHT)),
    LightEmitter,
    ControlViewSource<AnalogLED>
{

    companion object {

        const val PROP_ICON_PATH = "io.antarescircuit.antares.view.analog.AnalogLEDView.iconPath"

        /** The name of the [Boolean] property in [Properties] that determines whether the halo around the LED bulb is drawn. */
        const val PROP_DRAW_HALO = "antares.view.AnalogLEDView.drawHalo"

        private val NEGATIVE_HEIGHT = hInt(4)

        private val DEFAULT_LIGHT_COLOR = LightColor.RED

        /** The current (A) at which the [AnalogLEDView] starts glowing. */
        const val DEF_MIN_GLOW_CURRENT = 0.005

        /** The current (A) at which the [AnalogLEDView] reaches its maximum brightness. */
        const val DEF_MAX_GLOW_CURRENT = 0.02

        /** The radius of the color gradient drawn as halo during simulation.*/
        val GRADIENT_RADIUS = (3.0 * SIZE / 4.0).toInt()

        private val GRADIENT_CACHE = LightColorRadialGradientCache(
            Point2D(4.0 * Look.SCALE, 0.0),
            GRADIENT_RADIUS)
    }

    @Suppress("MemberVisibilityCanBePrivate") // Bean Reflection
    var minCurrent: Double = DEF_MIN_GLOW_CURRENT
        set(value) {
            require(value in 0.0..maxCurrent) { Translations.getString("library.element.LightBulb.minCurrent.error") }
            field = value
            postControlViewSourceChangeEvent()
        }

    @Suppress("MemberVisibilityCanBePrivate") // Bean Reflection
    var maxCurrent: Double = DEF_MAX_GLOW_CURRENT
        set(value) {
            require(value > minCurrent) { Translations.getString("library.element.LightBulb.maxCurrent.error") }
            field = value
            postControlViewSourceChangeEvent()
        }

    val executionLEDColor: Color get() = lightColor.gradient.at(
        LightBulbView.getExecutionLightFactor(
            (model.getPort<AnalogSignal>() as AnalogPort).current,
            minCurrent,
            maxCurrent))

    override val relativeExternalLabelLocation: Point2D get() =
        Point2D(LENGTH + SIZE / 2, -NEGATIVE_HEIGHT - LABEL_DIST)

    /** ---- [AbstractVerticeView] */

    /**
     * The [Paint] for drawing a halo around this [AnalogLEDView]. This is typically some
     * gradient [Paint] based on the current [executionLEDColor].
     */
    val haloPaint: Paint get() {
        val factor = LightBulbView.getExecutionLightFactor(
            (model.getPort<AnalogSignal>() as AnalogPort).current,
            minCurrent,
            maxCurrent
        )
        return GRADIENT_CACHE.forLightColor(lightColor).forFactoredColorGradient(lightColor.gradient, factor)
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)
        AntaresViewModule.currentSymbolStyle.symbolStyle.drawAnalogLED(this, context)
    }

    /** ---- [Storable] */

    override fun read(reader: StoreReader) {
        super.read(reader)
        lightColor = LightColor.read("lightColor", reader)
        if (reader.hasAttribute("minCurrent")) {
            minCurrent = reader.readDouble("minCurrent")
        }
        if (reader.hasAttribute("maxCurrent")) {
            maxCurrent = reader.readDouble("maxCurrent")
        }
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        lightColor.write("lightColor", writer)
        writer.writeDouble("minCurrent", minCurrent)
        writer.writeDouble("maxCurrent", maxCurrent)
    }

    /** ---- [ControlViewSource] */

    override val controlId: String get() = "analogLED:${model.id}"

    override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

    override fun createControlView(): ControlView<AnalogLED> =
        AnalogLEDControlView(styleProvider, model, lightColor, minCurrent, maxCurrent)

    /** ---- [LightEmitter]  */

    override var lightColor: LightColor by ControlViewSourceProperty(lightColor, eventBus)

    override val hasGraphParameter: Boolean get() = lightColor is LightColorExpression

    /** ---- [AbstractGraphElementView] */

    override fun bind(graphView: GraphView, deep: Boolean) {
        super.bind(graphView, deep)
        graphParamsChanged(graphView.graph!!)
    }

    override fun handleStateChanged(event: GraphElementEvent) {
        super.handleStateChanged(event)
        if (event.signalHandler == null) {
            updateMainPropertyLabel()
        }
        if (event.reason == LightEmitterModel.REASON_GRAPH_PARAM_CHANGED && event.argument is Graph) {
            graphParamsChanged(event.argument as Graph)
        }
    }

    override fun graphParamsChanged(graph: Graph) {
        (lightColor as? LightColorExpression)?.let { it.evaluateIn(graph)?.let { lc -> lightColor = lc } }
    }
}