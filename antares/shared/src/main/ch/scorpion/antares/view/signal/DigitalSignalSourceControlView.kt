package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.vertice.AbstractVertice
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class DigitalSignalSourceControlView<T : DigitalSignalSource>(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    override var controlId: String? = null,
    signalRepresentation: DigitalSignalRepresentation = DigitalSignalRepresentation.BINARY,
    model: T = DummySignalSource() as T,
    name: String? = null
) : AbstractNumberViewComponent<T>(styleProvider, model, Direction.EAST, signalRepresentation), ControlView<T> {

    init {
        modelExchanged(null)
    }

	/** The name used in the container editor to identify the model (which is not yet bound in the container editor)*/
	var name: String? = name
		private set

    override fun modelExchanged(oldModel: T?) {
        super.modelExchanged(oldModel)
        updateView()
    }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        if (controlId != null) {
            writer.writeString("controlId", controlId!!)
        }
        writer.writeInt("bitWidth", bitWidth.width)
	    if (name != null) {
		    writer.writeString("name", name!!)
	    }
    }

    override fun read(reader: StoreReader) {
        if (reader.hasAttribute("controlId")) {
            controlId = reader.readString("controlId")
        }
        bitWidth = BitWidth.of(reader.readInt("bitWidth"))
	    if (reader.hasAttribute("name")) {
		    name = reader.readString("name")
	    }
        super.read(reader)
    }

    /** ---- [AbstractVerticeView] */

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)
        drawNumberView(context, context.castedAppContext<GraphApplicationContext>()!!.isExecute)
    }

    /** ---- [ControlView] interface */

    override var isActiveControlView: Boolean = false

	override val mirrorWidth: Double get() = width

    override val controlName: String
	    get() {
		    if (StringUtils.isEmpty(model.name)) {
			    return "${model.type} ($id)"
		    }
		    return "${model.type} \"${model.name}\""
	    }

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, model: T) {
        this.model = model
    }

	override fun sourcePropertiesChanged(source: ControlViewSource<T>) {
		if (source is AbstractNumberViewComponent<*>) {
			copyControlViewProperties(source, this)
		}
	}

	override fun writeModelProperties(writer: StoreWriter) {
		if (StringUtils.isNotEmpty(name)) {
			writer.writeString("name", name!!)
		}
	}

	override fun readModelProperties(reader: StoreReader) {
		if (reader.hasAttribute("name")) {
			name = reader.readString("name")
		}
	}

	private fun copyControlViewProperties(source: AbstractNumberViewComponent<*>, dest: DigitalSignalSourceControlView<*>) {
		dest.name = source.model.name
		dest.bitWidth = source.bitWidth
		dest.signalRepresentation = source.signalRepresentation
	}

    /** ---- [AbstractNumberViewComponent] */

    override var bitWidth: BitWidth
        get() = model.bitWidth
        set(value) {
            model.bitWidth = value
            updateView()
        }

    override val signal: DigitalSignal get() = model.signal!!

    override val upperLeftBoundsEdge: Point2D
        get() = Point2D(0.0, -numberView.height / 2 - insets)

    override val insets: Int get() = 4

	/** ---- [DigitalSignalSourceControlView] */

	/** Used as placeholder of the mandatory [DigitalSignalSource] until it is set by [bindControlView].*/
	private class DummySignalSource : AbstractVertice(), DigitalSignalSource {

		companion object {
			private const val BASE_RESOURCE_KEY = "library.element.SignalSource"
			private val TYPE get() = Translations.getString("$BASE_RESOURCE_KEY.name")
			private val TYPE_DESC get() = Translations.getOptionalString("$BASE_RESOURCE_KEY.desc")
		}

		override val type: String get() = TYPE
		override val typeDesc: String? get() = TYPE_DESC

		override var bitWidth: BitWidth = BitWidth.BW_1
		override var signal: DigitalSignal? get() = DigitalSignalFactory.falseValue(bitWidth)
			set(value) {
				// empty
			}
	}

}