package ch.scorpion.antares.view.signal

import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.Vertice
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Abstract base class for [DigitalComponentView]s that display a [NumberView]
 * in a non-interactive way.
 */
abstract class AbstractNumberViewComponent<T : Vertice>(
	styleProvider: StyleProvider,
	model: T,
	orientation: Direction,
	signalRepresentation: DigitalSignalRepresentation = DigitalSignalRepresentation.BINARY,
	private val drawDigitBorder: Boolean = true
) : DigitalComponentView<T>(styleProvider, model) {

	companion object {
		const val DEFAULT_INSETS = Look.SCALE
		private const val FIXED_POINT_WIDTH = 150.0
		private const val FIXED_POINT_HEIGHT = DigitView.HEIGHT
	}

	override var orientation: Direction = orientation
		set(value) {
			if (value != field) {
				field = value
				createInnerView()
				updateView()
			}
		}

	open var signalRepresentation: DigitalSignalRepresentation = signalRepresentation
		set(value) {
			if (value != field) {
				field = value
				createInnerView()
				updateView()
			}
		}

	/**
	 * Displays the current signal. Dynamically created and initialized. Ony set if [signalRepresentation]
	 * is NOT [DigitalSignalRepresentation.FIXED_POINT].
	 */
	private var numberView: NumberView? = null

	/** Displays the current signal for [DigitalSignalRepresentation.FIXED_POINT].*/
	private var fixedPointView: Label? = null

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("representation", signalRepresentation.customName)
		writer.writeString("orientation", orientation.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		signalRepresentation = DigitalSignalRepresentation.withName(reader.readString("representation"))
		orientation = Direction.withName(reader.readString("orientation"))
	}

	/** ---- [Component] */

	override var preferredSelectionDrawingStrategy: SelectionDrawingStrategy?
		get() = SelectionDrawingStrategy.REPLACE
		set(@Suppress("UNUSED_PARAMETER") value) {
			throw UnsupportedOperationException()
		}

	override val useRotation: Boolean get() = false

	override val useOrientation: Boolean get() = true

	override fun handleStateChanged(event: GraphElementEvent) {
		numberView?.setSignal(signal)
		super.handleStateChanged(event)
	}

	/** ---- [Transparent] */

	override var transparency: Int
		get() = super.transparency
		set(value) {
			super.transparency = value
			numberView?.transparency = value
		}

	/** ---- [AbstractNumberViewComponent] */

	abstract var bitWidth: BitWidth

	abstract val signal: DigitalSignal

	abstract val upperLeftBoundsEdge: Point2D

	/** Returns the insets between the bounds and the contained [NumberView].*/
	protected open val insets: Int get() = DEFAULT_INSETS

	protected val innerBounds: RectangularShape
		get() = if (signalRepresentation == DigitalSignalRepresentation.FIXED_POINT) {
			Rectangle2D(
				fixedPointView!!.location.x, fixedPointView!!.location.y - FIXED_POINT_HEIGHT / 2,
				FIXED_POINT_WIDTH, FIXED_POINT_HEIGHT.toDouble()
			)
		} else {
			numberView!!.bounds
		}

	protected fun drawInnerView(context: DrawContext, isOn: Boolean) {
		if (signalRepresentation == DigitalSignalRepresentation.FIXED_POINT) {
			fixedPointView?.draw(context)
		} else {
			numberView?.draw(context, isOn, inactive = model.inactive)
		}
	}

	protected fun createInnerView() {
		if (signalRepresentation != DigitalSignalRepresentation.FIXED_POINT) {
			fixedPointView = null
			numberView = NumberView(signalRepresentation, bitWidth, drawDigitBorder)
			numberView!!.setSignal(signal)

			val upperLeftBoundsEdge = upperLeftBoundsEdge
			setBounds(
				upperLeftBoundsEdge.x, upperLeftBoundsEdge.y,
				numberView!!.width + 2 * insets, numberView!!.height + 2 * insets)

			numberView!!.setBounds(
				xInt + insets, yInt + insets,
				numberView!!.widthInt, numberView!!.heightInt)
		} else {
			numberView = null
			fixedPointView = Label("", font, color.textColor, HorizontalAlignment.LEFT)
			fixedPointView!!.location = Point2D(upperLeftBoundsEdge.x + insets, upperLeftBoundsEdge.y + insets + FIXED_POINT_HEIGHT / 2)
			setBounds(
				upperLeftBoundsEdge.x, upperLeftBoundsEdge.y,
				FIXED_POINT_WIDTH + 2 * insets, FIXED_POINT_HEIGHT.toDouble() + 2 * insets)
		}
	}

	protected fun updateView() {
		updateViewImpl()
		updateBoxes()

		invalidate()
		update()
		validate()
	}

	protected open fun updateViewImpl() {
		// empty
	}

	protected fun clear() {
		numberView?.clear()
	}
}