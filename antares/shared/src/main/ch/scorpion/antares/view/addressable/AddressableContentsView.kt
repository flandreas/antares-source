package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.GraphApplicationContext
import kotlin.math.max
import kotlin.math.min

/**
 * A [Drawable] that displays the contents of a [Addressable].
 *
 * @property addressable the [Addressable] whose contents are displayed
 * @property rowsCount the number of rows to display
 * @property columnsCount the number of columns to display
 * @property showDisassembler determines whether each data cell should be disassembled
 */
class AddressableContentsView(
	private val addressable: Addressable,
	rowsCount: Int = DEFAULT_ROWS_COUNT,
	columnsCount: Int = DEFAULT_COLUMNS_COUNT,
	showDisassembler: Boolean = DEFAULT_SHOW_DISASSEMBLER,
	highlightCurrentCellWhenNotSelected: Boolean = DEFAULT_HIGHLIGHT_CURRENT_CELL_WHEN_NOT_CS,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangle() {

	companion object {
		private const val DEFAULT_ROWS_COUNT = 4
		private const val DEFAULT_COLUMNS_COUNT = 1
		private const val DEFAULT_SHOW_DISASSEMBLER = false
		private const val DEFAULT_HIGHLIGHT_CURRENT_CELL_WHEN_NOT_CS = false
		private const val MAX_ROWS_COUNT = 10
		private const val MAX_COLUMNS_COUNT = 16
		private const val HORIZONTAL_INSET = 5
		private const val COL_DIST = 5
		private val STROKE: Stroke = Stroke(0.5f)
	}

	private val font: Font = Look.ADDRESSABLE_CONTENTS_FONT

	/** A flyweight used to draw the address.*/
	private val addressLabel = Label(
		text = "",
		font = font,
		horizontalAlignment = HorizontalAlignment.LEFT,
		verticalAlignment = VerticalAlignment.CENTER)

	/** A flyweight used to draw the data.*/
	private val dataLabel = Label(
		text = "",
		font = font,
		horizontalAlignment = HorizontalAlignment.LEFT,
		verticalAlignment = VerticalAlignment.CENTER)

	/** The width of the address column in view coordinates. Determined in [updateGeometry].*/
	private var addressColumnWidth: Int = 0

	/** The width of the data column in view coordinates. Determined in [updateGeometry].*/
	private var dataColumnWidth: Int = 0

	/** The width of the disassembly column in view coordinates. Determined in [updateGeometry].*/
	private var disassemblyColumnWidth: Int = 0

	/** The height of a row in view coordinates. Determined in [updateGeometry].*/
	private var rowHeight: Int = 0

	/**
	 * Contains the first address to display in the sliding display window.
	 *
	 * Maintained in order to implement a strategy for sliding the display window only if the
	 * new current address wouldn't be visible anymore.
	 */
	private var firstAddress = 0

	/** The number of rows to display.*/
	var rowsCount: Int = rowsCount
		set(value) {
			if (field != value) {
				require(value in 1..MAX_ROWS_COUNT)
				invalidate()
				field = value
				updateGeometry()
			}
		}

	/** Contains the number of data columns to display.*/
	var columnsCount: Int = columnsCount
		set(value) {
			if (field != value) {
				require(value in 1..MAX_COLUMNS_COUNT)
				invalidate()
				field = value
				updateGeometry()
			}
		}

	/**
	 * Determines whether the disassembly information should be displayed.
	 * If set to `true`, the property [columnsCount] is implicitly overridden to a value of 1, because disassembly
	 * information is displayed as an additional column for a one and only data column
	 */
	var showDisassembler: Boolean = showDisassembler
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateGeometry()
			}
		}

	var highlightCurrentCellWhenNotSelected: Boolean = highlightCurrentCellWhenNotSelected
		set(value) {
			if (field != value) {
				field = value
				invalidate()
			}
		}

	init {
		updateGeometry()
	}

	/** ---- [Drawable] interface */

	override fun draw(context: DrawContext) {
		var y: Double = location.y + 0.5 * rowHeight
		(firstAddress..min(firstAddress + rowsCount * effectiveColumnCount - 1, addressable.maxAddress) step effectiveColumnCount).forEach {
			drawRow(it, y, context)
			y += rowHeight
		}

		context.g.stroke = STROKE
		context.g.color = context.choose(context.styleColor(styleProvider.getStyle(StyleType.ANNOTATION).color)).foregroundColor
		context.g.drawRect(
			bounds.x + HORIZONTAL_INSET + addressColumnWidth + COL_DIST / 2.0,
			bounds.y,
			effectiveDataAreaWidth.toDouble(),
			height
		)
	}

	/** ---- [RectangularDrawable] interface */

	override val lineWidth: Double get() = STROKE.width.toDouble()

	/** ---- [AddressableContentsView] */

	private val addressDigitCount get() = max(1, addressable.addressWidth.width / 4)

	private val dataDigitCount get() = max(1, addressable.dataWidth.width / 4)

	private val effectiveColumnCount: Int get() = if (showDisassembler) 1 else columnsCount

	private val effectiveDataAreaWidth: Int
		get() = if (showDisassembler) {
			COL_DIST + dataColumnWidth + COL_DIST + disassemblyColumnWidth
		} else {
			columnsCount * (COL_DIST + dataColumnWidth)
		}

	fun updateGeometry() {
		addressColumnWidth = TextRenderInfoFactory.measureSingleLineText("0".repeat(addressDigitCount), font).textBounds.width.toInt()
		dataColumnWidth = TextRenderInfoFactory.measureSingleLineText("0".repeat(dataDigitCount), font).textBounds.width.toInt()
		rowHeight = TextRenderInfoFactory.measureSingleLineText("0", font).textBounds.height.toInt() + 5
		disassemblyColumnWidth = if (showDisassembler) {
			TextRenderInfoFactory.measureSingleLineText("0".repeat(addressable.disassemblyWidth), font).textBounds.width.toInt()
		} else {
			0
		}

		setBounds(
			0,
			0,
			HORIZONTAL_INSET + addressColumnWidth + effectiveDataAreaWidth + HORIZONTAL_INSET,
			+rowsCount * rowHeight)
	}

	/** Called by clients of this class when the current address of [Addressable] has changed.*/
	fun handleCurrentAddressChanged() {
		if (!isAddressVisible(addressable.currentAddress)) {
			firstAddress = calculateFirstAddress()
		}
		invalidate()
		validate()
	}

	private fun drawRow(address: Int, y: Double, context: DrawContext) {
		var x = location.x + HORIZONTAL_INSET
		context.g.font = font

		// Draw address
		addressLabel.color = context.choose(context.styleColor(styleProvider.getStyle(StyleType.ANNOTATION).color)).disabledTextColor
		addressLabel.text = BitOperation.longToHexPadded(address.toULong(), addressable.addressWidth)
		addressLabel.location = Point2D(x, y)
		addressLabel.draw(context)

		// Draw data cells
		x += addressColumnWidth + COL_DIST
		(address until address + effectiveColumnCount).forEach { cellAddress ->
			val isCurrent = cellAddress == addressable.currentAddress && context.castedAppContext<GraphApplicationContext>()!!.isExecute
			if (isCurrent && (addressable.isSelected || highlightCurrentCellWhenNotSelected)) {
				context.g.color = context.choose(context.styleColor(styleProvider.getStyle(StyleType.ANNOTATION).color)).foregroundColor
				context.g.fillRect(
					x - COL_DIST / 2 - 0.5,
					y - rowHeight / 2,
					dataColumnWidth.toDouble() + COL_DIST,
					rowHeight.toDouble())
				dataLabel.color = context.choose(context.styleColor(styleProvider.getStyle(StyleType.ANNOTATION).color)).backgroundColor
			} else {
				dataLabel.color = context.choose(context.styleColor(styleProvider.getStyle(StyleType.ANNOTATION).color)).textColor
			}

			dataLabel.text = BitOperation.longToHexPadded(addressable.dataAt(cellAddress), addressable.dataWidth)
			dataLabel.location = Point2D(x, y)
			dataLabel.draw(context)

			x += dataColumnWidth + COL_DIST
		}

		if (showDisassembler) {
			dataLabel.color = context.choose(context.styleColor(styleProvider.getStyle(StyleType.ANNOTATION).color)).textColor
			dataLabel.text = addressable.disassemblyAt(address)
			dataLabel.location = Point2D(x, y)
			dataLabel.draw(context)
		}
	}

	private fun isAddressVisible(address: Int): Boolean = address >= firstAddress && address - firstAddress < rowsCount * columnsCount

	/** Calculates the first address to display.*/
	private fun calculateFirstAddress(): Int {
		val blockSize = rowsCount * columnsCount
		val currentBlock = addressable.currentAddress / blockSize
		return max(0, currentBlock * blockSize)
	}
}