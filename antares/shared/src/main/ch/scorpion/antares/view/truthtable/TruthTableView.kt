package ch.scorpion.antares.view.truthtable

import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.truthtable.TruthTableModel
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.*

/**
 * A [Drawable] view of a [TruthTableModel].
 *
 * @property model the [TruthTableModel] defining the input and output associations
 * @property vertice the [Vertice] to be used for highlighting the actual signal values in the rendered view,
 * and for determining column names according to the [Vertice]' port names
 * @property vertice the [Vertice] whose [Port] values are used to determine the current row.
 * @property passive if `false`, this [TruthTableView] doesn't try to follow signal changes during simulation
 */
class TruthTableView(
	private val model: TruthTableModel,
	private val vertice: Vertice,
	private val passive: Boolean = false,
	private val styleType: StyleType = StyleType.TOOLTIP,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : AbstractRectangle() {

	companion object {
		private const val COL_WIDTH = 25
	}

	/** Listens for state changes of [vertice] and invalidates this [TruthTableView] to react to signal changes.*/
	private val verticeListener: GraphElementListener = object : GraphElementAdapter() {
		override fun stateChanged(e: GraphElementEvent) {
			invalidate()
			validate()
		}
	}

	init {
		build()
		if (!passive) {
			vertice.addGraphElementListener(verticeListener)
		}
	}

	private val stroke: Stroke = Stroke(0.5f)

	private val font: Font get() = styleProvider.getStyle(styleType).font

	private val textColor: Color get() = styleProvider.getStyle(styleType).color.textColor

	private val textColorCurrent: Color get() = styleProvider.getStyle(styleType).color.backgroundColor

	private val highlightColor: Color get() = styleProvider.getStyle(styleType).color.foregroundColor

	private val rowHeight: Int get() = font.size + 5

	/** A flyweight used to draw the texts of the individual table cells. */
	private val label = Label(
		text = "",
		font = font,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.CENTER
	)

	/** ---- [Drawable] interface */

	/**
	 * Called by clients when this [TruthTableView] is not actively used any more.
	 * De-registers itself from listening to model updates, but keeps its [TruthTableModel], because this
	 * [TruthTableView] might be reused later for a different [Vertice].
	 * */
	override fun dispose() {
		vertice.removeGraphElementListener(verticeListener)
	}

	override fun draw(context: DrawContext) {
		val currentRow = if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) getCurrentRowIndex() else -1
		if (currentRow >= 0) {
			context.g.color = highlightColor
			context.g.fillRect(location.x, location.y + (currentRow + 1) * rowHeight, width, rowHeight.toDouble())
		}

		drawColumnHeaders(context)

		var y: Int = (rowHeight * 1.5).toInt()
		model.rows.forEachIndexed { index, row ->
			drawRow(row, index == currentRow, y, context)
			y += rowHeight
		}

		context.g.color = textColor
		context.g.stroke = stroke

		// Horizontal line separating headers and rows
		context.g.drawLine(location.x, location.y + rowHeight, location.x + widthInt, location.y + rowHeight)

		// Vertical line separating inputs and outputs
		context.g.drawLine(
			location.x + model.inputCount * COL_WIDTH, location.y,
			location.x + model.inputCount * COL_WIDTH, location.y + heightInt
		)
	}

	private fun drawColumnHeaders(context: DrawContext) {
		context.g.color = textColor
		var x: Int = COL_WIDTH / 2
		model.inputColumns.reversed().forEach {
			drawColumnHeader(it.name, x, context)
			x += COL_WIDTH
		}
		model.outputColumnNames.reversed().forEach {
			drawColumnHeader(it, x, context)
			x += COL_WIDTH
		}
	}

	private fun drawColumnHeader(name: String, x: Int, context: DrawContext) {
		label.text = name
		label.location = Point2D(location.x + x, location.y + rowHeight / 2)
		label.draw(context)
	}

	private fun drawRow(row: TruthTableModel.Row, isCurrent: Boolean, y: Int, context: DrawContext) {
		// The first bit in the input and output arrays is the one with the lowest priority.
		// Since we are drawing from left to write, we reverse the arrays to start with the highest priority bit
		var x: Int = COL_WIDTH / 2
		row.input.reversed().forEachIndexed { _, bit ->
			drawBit(bit, isCurrent, x, y, context)
			x += COL_WIDTH
		}
		row.output.reversed().forEachIndexed { _, bit ->
			drawBit(bit, isCurrent, x, y, context)
			x += COL_WIDTH
		}
	}

	private fun drawBit(bit: Bit, isCurrent: Boolean, x: Int, y: Int, context: DrawContext) {
		context.g.color = if (isCurrent) textColorCurrent else textColor
		label.text = bit.toBinaryString()
		label.location = Point2D(location.x + x, location.y + y)
		label.draw(context)
	}

	/**
	 * Returns the index of the [TruthTableModel] row whose input values reflect the current input
	 * values of the [vertice].
	 */
	private fun getCurrentRowIndex(): Int {
		if (passive) {
			return -1
		}
		val signals = mutableListOf<Bit>()
		vertice.getInputs()
			.map { it as InputPort<DigitalSignal> }
			.forEach { signals.add(it.getIncomingSignal()!!.bitAt(0)) }
		return model.rowIndex(Array(signals.size) { signals[it] })
	}

	/** ---- [RectangularDrawable] */

	override val lineWidth: Double get() = 0.0

	/** ---- [TruthTableView] */

	private fun build() {
		setBounds(0, 0, (model.inputCount + model.outputCount) * COL_WIDTH, (model.rows.size + 1) * rowHeight)
	}
}