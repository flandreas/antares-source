package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogLED
import ch.scorpion.antares.model.analog.AnalogPort
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightColorExpression
import ch.scorpion.antares.view.output.LightColorRadialGradientCache
import ch.scorpion.antares.view.output.LightEmitter
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Paint
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.ControlViewSourceProperty
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class AnalogLEDView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: AnalogLED = AnalogLED(),
    lightColor: LightColor = DEFAULT_LIGHT_COLOR,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractDiodeView<AnalogLED>(styleProvider, model),
    LightEmitter,
    ControlViewSource<AnalogLED>
{

    companion object {

        const val PROP_ICON_PATH = "ch.scorpion.antares.view.analog.AnalogLEDView.iconPath"

        /** The name of the [Boolean] property in [Properties] that determines whether the halo around the LED bulb is drawn. */
        const val PROP_DRAW_HALO = "antares.view.AnalogLEDView.drawHalo"

        private val NEGATIVE_HEIGHT = hInt(4)

        private val DEFAULT_LIGHT_COLOR = LightColor.RED

        /** The current (A) at which the [AnalogLEDView] starts glowing. */
        const val DEF_MIN_GLOW_CURRENT = 0.005

        /** The current (A) at which the [AnalogLEDView] reaches its maximum brightness. */
        const val DEF_MAX_GLOW_CURRENT = 0.02

        /** The radius of the color gradient drawn as halo during simulation.*/
        val GRADIENT_RADIUS = 3.0 * SIZE / 4.0

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

    override fun modelExchanged(oldModel: AnalogLED?) {
        super.modelExchanged(oldModel)
        // Overwrite bounds to incorporate LED arrows
        setBounds(LENGTH, -NEGATIVE_HEIGHT, SIZE, SIZE / 2 + NEGATIVE_HEIGHT)
    }

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
}