package io.antarescircuit.antares.view.signal

import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.antares.model.signal.DigitalSignalRepresenter
import io.antarescircuit.antares.model.signal.DigitalSignalSource
import io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangle
import io.antarescircuit.jabbah.draw.drawable.Transparent
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.TextRenderInfoFactory
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.math.max

/**
 * Abstract base class for [OrientableRectangularVerticeView]s that display a [NumberView]
 * in a non-interactive way.
 */
abstract class AbstractNumberViewComponent<T : Vertice>(
	styleProvider: StyleProvider,
	model: T,
	orientation: Direction,
	signalRepresentation: DigitalSignalRepresentation = DigitalSignalRepresentation.BINARY,
	private val drawDigitBorder: Boolean = true
) : OrientableRectangularVerticeView<T>(styleProvider, model), DigitalSignalRepresenter {

	companion object {
		const val DEFAULT_INSETS = Look.SCALE
		private const val MIN_FIXED_POINT_WIDTH = 40.0
		private const val FIXED_POINT_HEIGHT = DigitView.HEIGHT.toDouble()
	}

	override var orientation: Direction = orientation
		set(value) {
			if (value != field) {
				field = value
				createInnerView()
				updateView()
			}
		}

	override var signalRepresentation: DigitalSignalRepresentation = signalRepresentation
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
	// Visible for testing
	var numberView: NumberView? = null
		private set

	/** Displays the current signal for [DigitalSignalRepresentation.FIXED_POINT].*/
	private var fixedPointView: FixedPointView? = null

	private var fixedPointViewWidth: Double? = null

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
		if (signalRepresentation == DigitalSignalRepresentation.FIXED_POINT) {
			fixedPointView?.label?.text = signalRepresentation.represent(signal, (model as DigitalSignalSource).fixedPointConfig)
		} else {
			numberView?.setSignal(signal)
		}
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
				fixedPointViewWidth!!, FIXED_POINT_HEIGHT
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
			fixedPointViewWidth = null
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
			fixedPointView = FixedPointView(this is ControlView<*>)
			updateFixedPointViewWidth()
			fixedPointView!!.adjustBounds(
				Rectangle2D(
					upperLeftBoundsEdge.x + insets, upperLeftBoundsEdge.y + insets,
					fixedPointViewWidth!!, FIXED_POINT_HEIGHT
				)
			)
			setBounds(
				upperLeftBoundsEdge.x, upperLeftBoundsEdge.y,
				fixedPointViewWidth!! + 2 * insets, FIXED_POINT_HEIGHT + 2 * insets)
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

	private fun updateFixedPointViewWidth() {
		fixedPointViewWidth = calculateFixedPointViewWidth()
	}

	private fun calculateFixedPointViewWidth(): Double {
		var width = MIN_FIXED_POINT_WIDTH

		if ((model as DigitalSignalSource).fixedPointConfig != null) {
			val config = (model as DigitalSignalSource).fixedPointConfig!!
			val sampleValue = "0".repeat(config.decimalDigitCount(bitWidth))
			width = max(width, TextRenderInfoFactory.measureSingleLineText(sampleValue, font).textBounds.width)
		}

		return width
	}

	private inner class FixedPointView(private val drawEditingBox: Boolean) : AbstractRectangle() {

		val label = Label(
			"",
			font,
			textColor,
			HorizontalAlignment.LEFT)

		fun adjustBounds(rect: Rectangle2D) {
			bounds.setFrame(rect)
			label.location = Point2D(rect.minX + 5, rect.minY + rect.height / 2)
		}

		override fun draw(context: DrawContext) {
			if (context.useContextColors) {
				drawImpl(context, context.color!!.foregroundColor, context.color!!.textColor)
			} else {
				drawImpl(context, transparent.applyTo(foregroundColor), transparent.applyTo(textColor))
			}
		}

		private fun drawImpl(context: DrawContext, strokeColor: Color, textColor: Color) {
			if (drawEditingBox && !context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				context.g.color = strokeColor
				context.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke
				context.g.draw(bounds)
			}
			context.g.color = textColor
			label.draw(context)
		}

		override val lineWidth: Double get() = 1.0
	}
}