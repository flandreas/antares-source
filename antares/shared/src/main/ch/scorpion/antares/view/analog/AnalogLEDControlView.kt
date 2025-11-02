package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogPort
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.antares.model.analog.Diode
import ch.scorpion.antares.view.output.AbstractLEDView
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Draws the [ControlView] of a [AnalogLEDView] as a round LED bulb (without radial color gradient halo).
 */
class AnalogLEDControlView(
    styleProvider: StyleProvider =  DrawStyleModule.styleProvider,
    model: Diode = Diode(),
    lightColor: LightColor = LightColor.RED,
    minCurrent: Double = AnalogLEDView.DEF_MIN_GLOW_CURRENT,
    maxCurrent: Double = AnalogLEDView.DEF_MAX_GLOW_CURRENT
) : AbstractLEDView<Diode>(styleProvider, model) {

    private var lightColor: LightColor = lightColor

    private var minCurrent: Double = minCurrent

    private var maxCurrent: Double = maxCurrent

    init {
        isShowPortViews = false
    }

    override fun handleStateChanged(event: GraphElementEvent) {
        if (event.reason == Diode.REASON_CURRENT) {
            invalidate()
            validate()
        } else {
            super.handleStateChanged(event)
        }
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

    override fun createControlView(): ControlView<Diode> {
        throw UnsupportedOperationException()
    }

    /** ---- [AbstractLEDView] */

    override fun getBulbExecuteColor(): Color =
        lightColor.gradient.at(
            LightBulbView.getExecutionLightFactor(
                (model.getPort<AnalogSignal>() as AnalogPort).current,
                minCurrent,
                maxCurrent)
        )

    override fun sourcePropertiesChanged(source: ControlViewSource<Diode>) {
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