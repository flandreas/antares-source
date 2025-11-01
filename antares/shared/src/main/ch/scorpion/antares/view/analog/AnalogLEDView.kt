package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogPort
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.analog.Diode
import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightColorExpression
import ch.scorpion.antares.view.output.LightEmitter
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.ControlViewSourceProperty
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class AnalogLEDView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Diode = Diode(),
    lightColor: LightColor = DEFAULT_LIGHT_COLOR,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractDiodeView(styleProvider, model),
    LightEmitter,
    ControlViewSource<Diode>,
    ControlView<Diode>
{

    companion object {
        const val PROP_ICON_PATH = "ch.scorpion.antares.view.analog.AnalogLEDView.iconPath"

        private val NEGATIVE_HEIGHT = hInt(4)

        private val DEFAULT_LIGHT_COLOR = LightColor.RED

        /** The current (A) at which the [AnalogLEDView] starts glowing. */
        private const val DEF_MIN_GLOW_CURRENT = 0.0

        /** The current (A) at which the [AnalogLEDView] reaches its maximum brightness. */
        private const val DEF_MAX_GLOW_CURRENT = 0.1
    }

    @Suppress("MemberVisibilityCanBePrivate") // Bean Reflection
    var minCurrent: Double = DEF_MIN_GLOW_CURRENT
        set(value) {
            require(value in 0.0..maxCurrent) { Translations.getString("library.element.LightBulb.minCurrent.error") }
            field = value
        }

    @Suppress("MemberVisibilityCanBePrivate") // Bean Reflection
    var maxCurrent: Double = DEF_MAX_GLOW_CURRENT
        set(value) {
            require(value > minCurrent) { Translations.getString("library.element.LightBulb.maxCurrent.error") }
            field = value
        }

    val executionLEDColor: Color get() = lightColor.gradient.at(
        LightBulbView.getExecutionLightFactor(
            (model.getPort<AnalogSignal>() as AnalogPort).current,
            minCurrent,
            maxCurrent))

    override fun modelExchanged(oldModel: Diode?) {
        super.modelExchanged(oldModel)
        // Overwrite bounds to incorporate LED arrows
        setBounds(LENGTH, -NEGATIVE_HEIGHT, SIZE, SIZE / 2 + NEGATIVE_HEIGHT)
    }

    /** ---- [AbstractVerticeView] */

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

    override val controlName: String get() = super.controlName

    override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

    override fun createControlView(): ControlView<Diode> {
        val clone = AnalogLEDView(styleProvider, model, lightColor)
        clone.isShowPortViews = false
        clone.location = Point2D.ZERO
        copyControlViewProperties(this, clone)
        return clone
    }

    /** --- [ControlView] */

    override var isActiveControlView: Boolean = false

    override fun writeModelProperties(writer: StoreWriter) { }

    override fun readModelProperties(reader: StoreReader) { }

    override fun sourcePropertiesChanged(source: ControlViewSource<Diode>) {
        if (source is AnalogLEDView) {
            copyControlViewProperties(source, this)
        }
    }

    override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
        this.model = link.getLinkedObject(startGraph) as Diode
    }

    private fun copyControlViewProperties(source: AnalogLEDView, dest: AnalogLEDView) {
        dest.lightColor = source.lightColor
        dest.orientation = source.orientation
    }

    /** ---- [LightEmitter]  */

    override var lightColor: LightColor by ControlViewSourceProperty(lightColor, eventBus)

    override val hasGraphParameter: Boolean get() = lightColor is LightColorExpression
}