package io.antarescircuit.antares.view.signal

import io.antarescircuit.antares.model.signal.*
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.vertice.AbstractVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeLink
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

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
	    createInnerView()
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
        drawInnerView(context, context.castedAppContext<GraphApplicationContext>()!!.isExecute)
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

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		this.model = link.getLinkedObject(startGraph) as T
	}

	override fun sourcePropertiesChanged(source: ControlViewSource<T>) {
		if (source is AbstractNumberViewComponent<*>) {
			copyControlViewProperties(source, this)
		} else if (source is DigitalCircuitInOutView) {
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

	private fun copyControlViewProperties(source: DigitalCircuitInOutView, dest: DigitalSignalSourceControlView<*>) {
		dest.bitWidth = source.bitWidth
		dest.signalRepresentation = source.signalRepresentation
	}

    /** ---- [AbstractNumberViewComponent] */

    override var bitWidth: BitWidth
        get() = model.bitWidth
        set(value) {
			clear()
			model.bitWidth = value
			createInnerView()
			updateView()
        }

    override val signal: DigitalSignal get() = model.signal!!

    override val upperLeftBoundsEdge: Point2D
        get() = Point2D(0.0, -innerBounds.height / 2 - insets)

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

		override var fixedPointConfig: FixedPointConfig? = null
		override var bitWidth: BitWidth = BitWidth.BW_1

		@Suppress("UNUSED_PARAMETER")
		override var signal: DigitalSignal? get() = DigitalSignalFactory.falseValue(bitWidth)
			set(value) {
				// empty
			}

		override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean {
			return false
		}
	}
}