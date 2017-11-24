package ch.scorpion.antares.view.memory

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.antares.model.memory.Addressable
import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.view.Look
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.checkArgument
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 * A [Drawable] that displays the contents of a [Addressable].
 *
 * @property addressable the [Addressable] whose contents are displayed
 * @property rowsCount the number of rows to display
 */
class AddressableContentsView(
        private val addressable: Addressable,
        rowsCount: Int = DEFAULT_ROWS_COUNT,
        columnsCount: Int = DEFAULT_COLUMNS_COUNT,
        private val styleType: StyleType = GraphStyleType.ANNOTATION,
        private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangle() {

    companion object {
        private val DEFAULT_ROWS_COUNT = 4
        private val DEFAULT_COLUMNS_COUNT = 1
        private val MAX_ROWS_COUNT = 10
        private val MAX_COLUMNS_COUNT = 16
        private val HORIZONTAL_INSET = 5
        private val COL_DIST = 5
        private val STROKE: Stroke = Stroke(0.5f)
    }

    private val font: Font = Look.ADDRESSABLE_CONTENTS_FONT

    private val dataTextColorCurrent: Color get() = styleProvider.getStyle(styleType).color.backgroundColor

    private val backgroundColorCurrent: Color get() = styleProvider.getStyle(styleType).color.foregroundColor

    private val borderColor: CompositeColor get() = styleProvider.getStyle(styleType).color

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
                checkArgument(value in 1..MAX_ROWS_COUNT)
                invalidate()
                field = value
                updateGeometry()
            }
        }

    var columnsCount: Int = columnsCount
        set(value) {
            if (field != value) {
                checkArgument(value in 1..MAX_COLUMNS_COUNT)
                invalidate()
                field = value
                updateGeometry()
            }
        }

    init {
        updateGeometry()
    }

    /** ---- [Drawable] interface */

    override fun draw(context: DrawContext) {
        var y: Double = location.y + 0.5 * rowHeight
        (firstAddress .. Math.min(firstAddress + rowsCount * columnsCount - 1, addressable.maxAddress) step columnsCount).forEach {
            drawRow(it, y, context)
            y += rowHeight
        }

        context.g.stroke = STROKE
        context.g.color = context.choose(borderColor).foregroundColor
        context.g.drawRect(
                bounds.x + HORIZONTAL_INSET + addressColumnWidth + COL_DIST / 2.0,
                bounds.y,
                columnsCount * (dataColumnWidth + COL_DIST).toDouble(),
                height
        )
    }

    /** ---- [RectangularDrawable] interface */

    override val lineWidth: Double get() = STROKE.width.toDouble()

    /** ---- [AddressableContentsView] */

    private val addressDigitCount = Math.max(1, addressable.addressWidth.width / 4)

    private val dataDigitCount = Math.max(1, addressable.dataWidth.width / 4)

    fun updateGeometry() {
        addressColumnWidth = DrawModule.textRenderInfoFactory.measureSingleLineText("0".repeat(addressDigitCount), font).textBounds.width.toInt()
        dataColumnWidth = DrawModule.textRenderInfoFactory.measureSingleLineText("0".repeat(dataDigitCount), font).textBounds.width.toInt()
        rowHeight = DrawModule.textRenderInfoFactory.measureSingleLineText("0", font).textBounds.height.toInt() + 5

        setBounds(
                0,
                0,
                HORIZONTAL_INSET + addressColumnWidth + columnsCount * (COL_DIST + dataColumnWidth) + HORIZONTAL_INSET,
                + rowsCount * rowHeight)
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
        context.g.color = context.choose(styleProvider.getStyle(styleType).color).disabledTextColor
        addressLabel.text = BitOperation.longToHex(address.toLong()).padStart(addressDigitCount, '0')
        addressLabel.location = Point2D(x, y)
        addressLabel.draw(context)

        // Draw data cells
        x += addressColumnWidth + COL_DIST
        (address until address + columnsCount).forEach { cellAddress ->
            val isCurrent = cellAddress == addressable.currentAddress && context.castedAppContext<GraphApplicationContext>()!!.isExecute
            if (isCurrent) {
                context.g.color = backgroundColorCurrent
                context.g.fillRect(
                        x - COL_DIST / 2 - 0.5,
                        y - rowHeight / 2,
                        dataColumnWidth.toDouble() + COL_DIST,
                        rowHeight.toDouble())
                context.g.color = dataTextColorCurrent
            } else {
                context.g.color = context.choose(styleProvider.getStyle(styleType).color).textColor
            }

            dataLabel.text = BitOperation.longToHex(addressable.dataAt(cellAddress)).padStart(dataDigitCount, '0')
            dataLabel.location = Point2D(x, y)
            dataLabel.draw(context)

            x += dataColumnWidth + COL_DIST
        }
    }

    private fun isAddressVisible(address: Int): Boolean = address >= firstAddress && address - firstAddress < rowsCount * columnsCount

    /** Calculates the first address to display.*/
    private fun calculateFirstAddress(): Int {
        val blockSize = rowsCount * columnsCount
        val currentBlock = addressable.currentAddress / blockSize
        return Math.max(0, currentBlock * blockSize)
    }
}