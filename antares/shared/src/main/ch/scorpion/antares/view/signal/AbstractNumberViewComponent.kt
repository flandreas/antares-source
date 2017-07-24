package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Abstract base class for [DigitalComponentView]s that display a [NumberView].
 */
abstract class AbstractNumberViewComponent<T : Vertice>(
    styleProvider: StyleProvider,
    baseResourceKey: String,
    model: T?,
    orientation: Direction,
    signalRepresentation: DigitalSignalRepresentation = DigitalSignalRepresentation.BINARY
) : DigitalComponentView<T>(styleProvider, baseResourceKey, model) {

    companion object {
        val DEFAULT_INSETS = Look.SCALE
    }

    override var orientation: Direction = orientation
        set(value) {
            if (value != field) {
                field = value
                updateView()
            }
        }

    var signalRepresentation: DigitalSignalRepresentation = signalRepresentation
        set(value) {
            if (value != field) {
                field = value
                updateView()
            }
        }

    /** Displays the current signal. Dynamically created and initialized.*/
    protected var numberView: NumberView? = null
        private set

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("representation", signalRepresentation.customName);
        writer.writeString("orientation", orientation.customName);
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        signalRepresentation = DigitalSignalRepresentation.withName(reader.readString("representation"))
        orientation = Direction.withName(reader.readString("orientation"))
    }

    /** ---- [Component] */

    override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
        get() = SelectionDrawingStrategy.REPLACE
        set(value) {throw UnsupportedOperationException()}

    override val rotatable: Boolean get() = false

    override fun handleStateChanged(event: GraphElementEvent) {
        numberView!!.setSignal(signal)
        super.handleStateChanged(event)
    }

    /** ---- [AbstractNumberViewComponent] */

    abstract var bitWidth: BitWidth

    abstract val signal: DigitalSignal

    abstract val upperLeftBoundsEdge: Point2D

    /** Returns the insets between the bounds and the contained [NumberView].*/
    protected open val insets: Int get() = DEFAULT_INSETS

    protected fun drawNumberView(context: DrawContext, isOn: Boolean) {
        numberView?.draw(context, isOn)
    }

    protected fun updateView() {
        invalidate()

        numberView = NumberView(signalRepresentation, bitWidth)
        numberView!!.setSignal(signal)

        val upperLeftBoundsEdge = upperLeftBoundsEdge
        setBounds(
                upperLeftBoundsEdge.x, upperLeftBoundsEdge.y,
                numberView!!.width + 2 * insets, numberView!!.height + 2 * insets)

        numberView!!.setBounds(
                xInt + insets, yInt + insets,
                numberView!!.widthInt, numberView!!.heigthInt)

        updateViewImpl()

        updateBoxes()

        invalidate()
        update()
        validate()
    }

    protected open fun updateViewImpl() {
        // empty
    }
}