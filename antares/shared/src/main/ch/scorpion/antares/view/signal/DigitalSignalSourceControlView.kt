package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.*
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Storable

class DigitalSignalSourceControlView<T : DigitalSignalSource>(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    override var controlId: String? = null,
    signalRepresentation: DigitalSignalRepresentation = DigitalSignalRepresentation.BINARY,
    model: T? = null,
    name: String? = null
) : AbstractNumberViewComponent<T>(styleProvider, "library.element.CircuitInOutControlView", model, Direction.EAST, signalRepresentation), ControlView<T> {

    init {
        modelExchanged(null)
    }

	/** The name used in the container editor to identify the model (which is not yet bound in the container editor)*/
	var name: String? = name
		private set

    override fun modelExchanged(oldModel: T?) {
        super.modelExchanged(oldModel)
        if (model != null) {
            updateView()
        }
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

    override fun bindToModel(model: T) {
        this.model = model
    }

    /** ---- [AbstractNumberViewComponent] */

    /** The [BitWidth] to be used as long as this [DigitalSignalSourceControlView] is still unbound. */
    private var _bitWidth: BitWidth = BitWidth.BW_1

    override var bitWidth: BitWidth
        get() {
            if (model == null) {
                return _bitWidth
            }
            return model!!.bitWidth
        }
        set(value) {
            if (model == null) {
                _bitWidth = value
            } else {
                model!!.bitWidth = value
            }
            updateView()
        }

    override val signal: DigitalSignal
        get() {
            if (model == null) {
                return Word.allOf(bitWidth, Bit.False)
            }
            return model!!.signal!!
        }

    override val upperLeftBoundsEdge: Point2D
        get() = Point2D(0.0, -numberView!!.height / 2 - insets)

    override val insets: Int get() = 4
}