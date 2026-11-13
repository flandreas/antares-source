package io.antarescircuit.antares.view.truthtable

import io.antarescircuit.antares.model.expression.BooleanExpressionNotation
import io.antarescircuit.antares.model.expression.StandardBooleanExpressionWriter
import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.antares.model.quinemccluskey.Literal
import io.antarescircuit.antares.model.signal.Bit
import io.antarescircuit.antares.model.truthtable.KarnaughCell
import io.antarescircuit.antares.model.truthtable.KarnaughMap
import io.antarescircuit.antares.model.truthtable.KarnaughRectangle
import io.antarescircuit.antares.model.truthtable.TruthTableCellChange
import io.antarescircuit.antares.model.truthtable.TruthTableCellsCommand
import io.antarescircuit.antares.model.truthtable.TruthTableListener
import io.antarescircuit.antares.model.truthtable.TruthTableReference
import io.antarescircuit.antares.model.truthtable.TruthTableService
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.swing.ColorIcon
import io.antarescircuit.jabbah.base.ui.UI
import io.antarescircuit.jabbah.edit.CommandManager
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import javax.swing.*

/**
 * Shows the Karnaugh map of one output column of a truth table together with the implicants of its
 * minimized DNF. Clicking a cell cycles its value 0 → 1 → X as an undoable [TruthTableCellsCommand].
 */
class KarnaughMapView(
	private val ref: TruthTableReference,
	private val commandManager: CommandManager,
	private val truthTableService: TruthTableService = AntaresModelModule.truthTableService,
) : JPanel(BorderLayout(0, 5)) {

	companion object {
		private const val CELL_SIZE = 44
		private const val PADDING = 6
		private const val MARGIN = 10
		private const val SUBMAP_GAP = 24

		private val LIGHT_PALETTE = listOf(0xD62728, 0x1F77B4, 0x2CA02C, 0xE67E00, 0x9467BD, 0x8C564B, 0xE377C2, 0x17A2B8)
		private val DARK_PALETTE = listOf(0xFF6B68, 0x6CB4FF, 0x5FD35F, 0xFFB454, 0xC39BFF, 0xD9A27A, 0xFF8FD0, 0x4FD8E8)

		/** A distinct implicant color that keeps its contrast against the light or dark theme background. */
		fun implicantColor(index: Int): Color {
			val palette = if (UI.isDark) DARK_PALETTE else LIGHT_PALETTE
			return Color(palette[index % palette.size])
		}

		private fun nextValue(bit: Bit): Bit = when (bit) {
			Bit.False -> Bit.True
			Bit.True -> Bit.Error
			else -> Bit.False
		}
	}

	private val inputCount = ref.truthTable.inputColumnCount

	private val map: KarnaughMap? = inputCount.takeIf { it <= KarnaughMap.MAX_VARIABLES }?.let { KarnaughMap(it) }

	private val outputSelector = JComboBox(
		(inputCount until ref.truthTable.columnCount)
			.map { StandardBooleanExpressionWriter.ofPropertiesNotation().getOutput(ref.truthTable, it) }
			.toTypedArray()
	)

	/** The painted map, or `null` if the truth table has too many inputs. */
	val mapComponent: MapComponent? = map?.let { MapComponent(it) }

	private val expressionField = JTextField()

	private val legendPanel = JPanel(FlowLayout(FlowLayout.LEADING, 12, 2))

	/** The truth table column of the displayed output. */
	val outputColumn: Int get() = inputCount + outputSelector.selectedIndex

	/** The map rectangles of each term of the displayed output's minimized DNF. */
	var implicants: List<List<KarnaughRectangle>> = emptyList()
		private set

	private val listener = TruthTableListener { if (it.column == outputColumn) refresh() }

	init {
		if (map == null) {
			add(
				JLabel(Translations.getString("antares.truthTable.karnaugh.tooManyInputs", KarnaughMap.MAX_VARIABLES), SwingConstants.CENTER),
				BorderLayout.CENTER,
			)
		} else {
			buildUI()
			ref.addDataListener(listener)
			refresh()
		}
	}

	fun dispose() {
		ref.removeDataListener(listener)
	}

	private fun buildUI() {
		val header = JPanel()
		header.layout = BoxLayout(header, BoxLayout.LINE_AXIS)
		val tipLabel = JLabel(Translations.getString("antares.truthTable.karnaugh.tip"))
		tipLabel.border = BorderFactory.createEmptyBorder(5, 2, 5, 0)
		header.add(tipLabel)
		header.add(Box.createHorizontalGlue())
		if (outputSelector.itemCount > 1) {
			header.add(JLabel(Translations.getString("antares.truthTable.karnaugh.output")))
			header.add(Box.createHorizontalStrut(5))
			outputSelector.maximumSize = outputSelector.preferredSize
			outputSelector.addActionListener { refresh() }
			header.add(outputSelector)
		}

		expressionField.isEditable = false
		expressionField.font = Font(Font.MONOSPACED, Font.PLAIN, 13)

		val south = JPanel(BorderLayout())
		south.add(expressionField, BorderLayout.NORTH)
		south.add(legendPanel, BorderLayout.CENTER)

		add(header, BorderLayout.NORTH)
		add(JScrollPane(mapComponent), BorderLayout.CENTER)
		add(south, BorderLayout.SOUTH)
	}

	/** Recomputes the minimized DNF of the displayed output and its implicant rectangles. */
	private fun refresh() {
		val map = map ?: return
		val table = ref.truthTable
		val column = outputColumn
		val dnf = truthTableService.generateDnf(table, column)
		val notation = BooleanExpressionNotation.fromProperties()
		implicants = dnf.map { map.rectangles(it) }

		expressionField.text = truthTableService.generateExpression(table, column, notation, dnf)
		legendPanel.removeAll()
		dnf.forEachIndexed { i, term ->
			val color = implicantColor(i)
			legendPanel.add(JLabel(termText(term, notation), ColorIcon(color, color, 12, 12), SwingConstants.LEADING))
		}
		legendPanel.revalidate()
		legendPanel.repaint()
		mapComponent?.repaint()
	}

	private fun termText(term: List<Literal>, notation: BooleanExpressionNotation): String =
		truthTableService.generateExpression(ref.truthTable, outputColumn, notation, listOf(term)).substringAfter(" = ")

	/** Advances the value of the displayed output in truth table [row] to the next of 0, 1 and X. */
	private fun cycle(row: Int) {
		val value = nextValue(ref.truthTable.getValue(row, outputColumn))
		commandManager.execute(TruthTableCellsCommand(ref, listOf(TruthTableCellChange(row, outputColumn, value))))
	}

	/** Paints the submaps with their headers, cell values and implicant outlines. */
	inner class MapComponent(private val map: KarnaughMap) : JComponent() {

		private val valueFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f)

		private val labelFont = UIManager.getFont("Label.font").deriveFont(13f)

		private val smallFont = UIManager.getFont("Label.font").deriveFont(9f)

		private val labelMetrics get() = getFontMetrics(labelFont)

		private val rowNames = variableNames(map.rowVariables)

		private val columnNames = variableNames(map.columnVariables)

		private val submapNames = variableNames(map.submapVariables)

		private val rowHeaderWidth: Int get() =
			maxOf(
				labelMetrics.stringWidth(code(0, map.rowVariableCount)),
				labelMetrics.stringWidth(rowNames) + labelMetrics.stringWidth(columnNames),
			) + 3 * PADDING

		private val columnHeaderHeight: Int get() = 2 * labelMetrics.height + PADDING

		private val titleHeight: Int get() = if (map.submapCount > 1) labelMetrics.height + PADDING else 0

		private val submapWidth: Int get() = rowHeaderWidth + map.columnCount * CELL_SIZE

		init {
			isFocusable = true
			toolTipText = ""
			cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
			addMouseListener(object : MouseAdapter() {
				override fun mousePressed(e: MouseEvent) {
					requestFocusInWindow()
					if (SwingUtilities.isLeftMouseButton(e)) {
						cellAt(e.point)?.let { cycle(map.truthTableRow(it)) }
					}
				}
			})
		}

		/** The bounds of [cell] in this component's coordinates. */
		fun cellBounds(cell: KarnaughCell): Rectangle =
			gridBounds(cell.submap).let {
				Rectangle(it.x + cell.column * CELL_SIZE, it.y + cell.row * CELL_SIZE, CELL_SIZE, CELL_SIZE)
			}

		fun cellAt(point: Point): KarnaughCell? = map.cells.firstOrNull { cellBounds(it).contains(point) }

		private fun gridBounds(submap: Int): Rectangle = Rectangle(
			MARGIN + submap * (submapWidth + SUBMAP_GAP) + rowHeaderWidth,
			MARGIN + titleHeight + columnHeaderHeight,
			map.columnCount * CELL_SIZE,
			map.rowCount * CELL_SIZE,
		)

		override fun getPreferredSize(): Dimension = Dimension(
			2 * MARGIN + map.submapCount * submapWidth + (map.submapCount - 1) * SUBMAP_GAP,
			2 * MARGIN + titleHeight + columnHeaderHeight + map.rowCount * CELL_SIZE,
		)

		override fun getToolTipText(event: MouseEvent): String? {
			val cell = cellAt(event.point) ?: return null
			val row = map.truthTableRow(cell)
			val assignment = (0 until inputCount).joinToString(", ") {
				"${ref.truthTable.getColumnName(it)}=${ref.truthTable.getValue(row, it).toBinaryString()}"
			}
			return Translations.getString("antares.truthTable.karnaugh.cell", row, assignment)
		}

		override fun paintComponent(graphics: Graphics) {
			val g = graphics.create() as Graphics2D
			try {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
				g.color = UIManager.getColor("Panel.background")
				g.fillRect(0, 0, width, height)
				for (submap in 0 until map.submapCount) {
					paintSubmap(g, submap)
				}
				implicants.forEachIndexed { i, rectangles -> paintImplicant(g, i, rectangles) }
				// Truth table row numbers go on top of a backdrop so that implicant outlines don't hide them
				g.font = smallFont
				val metrics = g.fontMetrics
				for (cell in map.cells) {
					val bounds = cellBounds(cell)
					val index = map.truthTableRow(cell).toString()
					val x = bounds.x + bounds.width - metrics.stringWidth(index) - 3
					val y = bounds.y + bounds.height - 3
					g.color = UIManager.getColor("Table.background")
					g.fillRect(x - 1, y - metrics.ascent, metrics.stringWidth(index) + 2, metrics.ascent + 1)
					g.color = UIManager.getColor("Label.disabledForeground")
					g.drawString(index, x, y)
				}
			} finally {
				g.dispose()
			}
		}

		private fun paintSubmap(g: Graphics2D, submap: Int) {
			val grid = gridBounds(submap)
			val left = grid.x - rowHeaderWidth
			val top = grid.y - columnHeaderHeight
			val foreground = UIManager.getColor("Label.foreground")
			val dimmed = UIManager.getColor("Label.disabledForeground")

			g.color = UIManager.getColor("Table.background")
			g.fill(grid)

			g.font = labelFont
			g.color = foreground
			if (map.submapCount > 1) {
				drawCentered(g, "$submapNames = ${code(KarnaughMap.grayCode(submap), map.submapVariableCount)}",
					grid.x + grid.width / 2, top - titleHeight / 2)
			}

			// Corner: column variables top right, row variables bottom left, separated by a diagonal
			val lineHeight = labelMetrics.height
			g.drawString(columnNames, grid.x - PADDING / 2 - labelMetrics.stringWidth(columnNames), top + labelMetrics.ascent)
			g.drawString(rowNames, left + PADDING / 2, grid.y - labelMetrics.descent - PADDING / 2)
			g.color = dimmed
			g.drawLine(left + PADDING / 2, top + PADDING / 2, grid.x, grid.y)

			g.color = foreground
			for (column in 0 until map.columnCount) {
				drawCentered(g, code(KarnaughMap.grayCode(column), map.columnVariableCount),
					grid.x + column * CELL_SIZE + CELL_SIZE / 2, top + lineHeight + lineHeight / 2)
			}
			for (row in 0 until map.rowCount) {
				drawCentered(g, code(KarnaughMap.grayCode(row), map.rowVariableCount),
					grid.x - rowHeaderWidth / 2, grid.y + row * CELL_SIZE + CELL_SIZE / 2)
			}

			for (row in 0 until map.rowCount) {
				for (column in 0 until map.columnCount) {
					paintCell(g, KarnaughCell(submap, row, column), foreground, dimmed)
				}
			}

			g.color = UIManager.getColor("Table.gridColor") ?: dimmed
			for (row in 0..map.rowCount) {
				g.drawLine(grid.x, grid.y + row * CELL_SIZE, grid.x + grid.width, grid.y + row * CELL_SIZE)
			}
			for (column in 0..map.columnCount) {
				g.drawLine(grid.x + column * CELL_SIZE, grid.y, grid.x + column * CELL_SIZE, grid.y + grid.height)
			}
		}

		private fun paintCell(g: Graphics2D, cell: KarnaughCell, foreground: Color, dimmed: Color) {
			val bounds = cellBounds(cell)
			val row = map.truthTableRow(cell)
			val value = ref.truthTable.getValue(row, outputColumn)
			g.font = valueFont
			g.color = if (value == Bit.False) dimmed else foreground
			drawCentered(g, value.toBinaryString(), bounds.centerX.toInt(), bounds.centerY.toInt())
		}

		/**
		 * Outlines each rectangle of an implicant. Rectangles that belong to a term wrapping around an edge
		 * are drawn open towards that edge.
		 */
		private fun paintImplicant(g: Graphics2D, index: Int, rectangles: List<KarnaughRectangle>) {
			val color = implicantColor(index)
			val inset = 3 + (index % 4) * 3
			val overhang = CELL_SIZE / 2
			for ((submap, pieces) in rectangles.groupBy { it.submap }) {
				val wrapsRows = pieces.map { it.row }.distinct().size > 1
				val wrapsColumns = pieces.map { it.column }.distinct().size > 1
				val grid = gridBounds(submap)
				for (piece in pieces) {
					var x = grid.x + piece.column * CELL_SIZE + inset
					var y = grid.y + piece.row * CELL_SIZE + inset
					var w = piece.columnSpan * CELL_SIZE - 2 * inset
					var h = piece.rowSpan * CELL_SIZE - 2 * inset
					if (wrapsColumns && piece.column == 0) { x -= overhang; w += overhang }
					if (wrapsColumns && piece.column + piece.columnSpan == map.columnCount) w += overhang
					if (wrapsRows && piece.row == 0) { y -= overhang; h += overhang }
					if (wrapsRows && piece.row + piece.rowSpan == map.rowCount) h += overhang

					val shape = RoundRectangle2D.Float(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), 16f, 16f)
					val clipped = g.create() as Graphics2D
					clipped.clip(grid)
					clipped.color = Color(color.red, color.green, color.blue, 36)
					clipped.fill(shape)
					clipped.color = color
					clipped.stroke = BasicStroke(2.5f)
					clipped.draw(shape)
					clipped.dispose()
				}
			}
		}

		private fun drawCentered(g: Graphics2D, text: String, centerX: Int, centerY: Int) {
			val metrics = g.fontMetrics
			g.drawString(text, centerX - metrics.stringWidth(text) / 2, centerY + (metrics.ascent - metrics.descent) / 2)
		}

		private fun variableNames(variables: List<Int>): String {
			val names = variables.map { ref.truthTable.getColumnName(it) }
			return names.joinToString(if (names.all { it.length == 1 }) "" else ",")
		}

		private fun code(value: Int, bits: Int): String =
			if (bits == 0) "" else value.toString(2).padStart(bits, '0')
	}
}
