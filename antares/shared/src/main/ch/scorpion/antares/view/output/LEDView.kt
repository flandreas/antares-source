package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.LED
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A view of an [LED].
 */
class LEDView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: LED = LED(),
    lightColor: LightColor = DEFAULT_LIGHT_COLOR,
    square: Boolean = false,
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLEDView<LED>(styleProvider, model, square, eventBus), LightEmitter, ControlView<LED>, ControlViewSource<LED> {

    companion object {
        const val PROP_ICON_PATH = "ch.scorpion.antares.view.output.LEDView.iconPath"
        private val DEFAULT_LIGHT_COLOR = LightColor.RED
    }

	/** ---- [LightEmitter]  */

    override var lightColor: LightColor = lightColor
        set(value) {
	        if (field != value) {
		        invalidate()
		        field = value
		        postControlViewSourceChangeEvent(eventBus)
		        validate()
	        }
        }

    /** ---- [ControlView] */

	override val controlId: String
        get() {
            // Don't use GraphElementView#getId() as part of the controlId, because that one might be changed
            // when ControlViews (event as part of a wrapping Component) are added to a Drawing
            return "led:${model.id}"
        }

	override fun sourcePropertiesChanged(source: ControlViewSource<LED>) {
		super.sourcePropertiesChanged(source)
		if (source is LEDView) {
			lightColor = source.lightColor
		}
	}

	/** ---- [ControlViewSource] */

    override fun createControlView(): ControlView<LED> {
        val clone = LEDView(styleProvider, model, lightColor, square)
        clone.isShowPortViews = false
        clone.location = Point2D(0, 0)
		copyControlViewProperties(this, clone)
        return clone
    }

    override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)


    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("lightColor", lightColor.customName)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        lightColor = LightColor.withName(reader.readString("lightColor"))
    }

    /** ---- [LEDView] */


    override fun getBulbColor(): Color {
        if (model.isOn) {
            return lightColor.onColor
        }
        return lightColor.offColor
    }
}
