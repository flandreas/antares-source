package ch.scorpion.antares.view.container

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.model.Trigger
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.container.PortViewComponent
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * TODO Invalidation and validation logic shouldn't be here. Instead, Ports should issue events
 * to which PortViews should listen and invalidate themselves accordingly.
 */
class DigitalPortViewComponent(
    styleProvider: StyleProvider,
    portView: DigitalPortView?
) : PortViewComponent<DigitalSignal>(styleProvider, portView) {

    constructor(styleProvider: StyleProvider): this(styleProvider, null)
    @Suppress("unused") constructor(): this(DrawStyleModule.styleProvider)

    private val digitalPort: DigitalPort get() = port as DigitalPort
    private val digitalPortView: DigitalPortView get() = portView as DigitalPortView

    var logic: Logic
        get() = digitalPort.logic
        set(value) {
            portView!!.invalidate()
            digitalPort.logic = value
            portView!!.invalidate()
            portView!!.validate()
        }

    var trigger: Trigger
        get() = digitalPort.trigger
        set(value) {
            portView!!.invalidate()
            digitalPort.trigger = value
            portView!!.invalidate()
            portView!!.validate()
        }

    var showBitWidthAnnotation: Boolean
        get() = digitalPortView.showBitWidthAnnotation
        set(value) {
            portView!!.invalidate()
            digitalPortView.showBitWidthAnnotation = value
            portView!!.invalidate()
            portView!!.validate()
        }

    var portLabelPosition: PortLabelPosition
        get() = digitalPortView.portLabelPosition
        set(value) {
            portView!!.invalidate()
            digitalPortView.portLabelPosition = value
            portView!!.invalidate()
            portView!!.validate()
        }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeBoolean("showBitWidthAnnotation", showBitWidthAnnotation)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        showBitWidthAnnotation = reader.readBoolean("showBitWidthAnnotation")
    }
}