package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.input.Terminal
import ch.scorpion.antares.model.input.TerminalRow
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.RoundRectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.FontFamily
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/** A view representation of a [Terminal].*/
class TerminalView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Terminal = Terminal()
) : AbstractRectangularVerticeView<Terminal>(
	styleProvider,
	model,
	RoundRectangle2D(0.0, 0.0, 100.0, 100.0, ROUND_ARC.toDouble(), ROUND_ARC.toDouble())
) {

	companion object {
		private val DEFAULT_SIZE = Size.MEDIUM

		private const val INSET = 2 * Look.SCALE
		private const val ROUND_ARC = 10
		private val CONTENT_COLOR = CompositeColor(Color.BLACK, Color.BLACK, Color.WHITE)
	}

	var size: Size = DEFAULT_SIZE
		set(value) {
			if (field != value) {
				invalidate()
				field = value
				updateGeometry()
			}
		}

	init {
		modelExchanged(null)
	}

	private val sizeFactor: Float get() = when(size) {
		Size.SMALL -> 0.75f
		Size.MEDIUM -> 1f
		Size.LARGE -> 1.5f
	}

	private val textFont get() = font.deriveFont(FontFamily.MONOSPACED).deriveFont(Math.round(font.size * sizeFactor))

	private val textRenderInfo get() = DrawModule.textRenderInfoFactory.measureSingleLineText("A", textFont)

	private val cellHeight get() = textRenderInfo.textBounds.height

	private val cellWidth get() = textRenderInfo.textBounds.width

	private val calculatedWidth get() = INSET + columnsCount * cellWidth + INSET

	private val calculatedHeight get() =  INSET + rowsCount * cellHeight + INSET

	/** ---- UI properties */

	var rowsCount: Int
		get() = model!!.rowsCount
		set(value) {
			if (value != rowsCount) {
				invalidate()
				model!!.rowsCount = value
				updateGeometry()
				invalidate()
			}
		}

	var columnsCount: Int
		get() = model!!.columnsCount
		set(value) {
			if (value != columnsCount) {
				invalidate()
				model!!.columnsCount = value
				updateGeometry()
				invalidate()
			}
		}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeString("size", size.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		size = Size.withName(reader.readString("size"))
	}

	/** ---- [AbstractGraphElementView] */

	override fun modelExchanged(oldModel: Terminal?) {
		super.modelExchanged(oldModel)

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.clockInput,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = DigitalPortView.LENGTH,
			y = 0))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.dataInput,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			showBitWidthAnnotation = false,
			x = DigitalPortView.LENGTH,
			y = -4 * Look.SCALE))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.writeEnableInput,
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = DigitalPortView.LENGTH + 3 * Look.SCALE,
			y = 3 * Look.SCALE))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.clearInput,
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = DigitalPortView.LENGTH + 7 * Look.SCALE,
			y = 3 * Look.SCALE))

		updateGeometry()
	}

	private fun updateGeometry() {
		invalidate()
		setBounds(
			x = DigitalPortView.LENGTH.toDouble(),
			y = -calculatedHeight + 3 * Look.SCALE.toDouble(),
			w = calculatedWidth.toDouble(),
			h = calculatedHeight.toDouble()
		)
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		val oldStylable = context.stylable

		context.stylable = this
		drawImplBeforeBorder(context)
		context.stylable = oldStylable

		drawBody(context)
		drawScreen(context)
		drawText(context)

		context.stylable = this
		drawImplAfterBorder(context)
		context.stylable = oldStylable
	}

	private fun drawBody(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRoundRect(x.toInt(), y.toInt(), width.toInt(), height.toInt(), ROUND_ARC, ROUND_ARC)
			}
		}

		drawFill(context, rectangle, context.choose(color).backgroundColor)
		drawStroke(context, rectangle, context.choose(color).foregroundColor, stroke)
	}

	private fun drawScreen(context: DrawContext) {
		context.g.color = context.choose(CONTENT_COLOR).backgroundColor
		context.g.fillRect(rectangle.x + INSET, rectangle.y + INSET, rectangle.width - 2 * INSET, rectangle.height - 2 * INSET)

		context.g.color = context.choose(CONTENT_COLOR).foregroundColor
		context.g.drawRect(rectangle.x + INSET, rectangle.y + INSET, rectangle.width - 2 * INSET, rectangle.height - 2 * INSET)
	}

	private fun drawText(context: DrawContext) {
		context.g.font = textFont
		context.g.color = context.choose(CONTENT_COLOR).textColor

		var y = rectangle.minY.toInt() + INSET + textRenderInfo.ascent
		for (row in 0 until model!!.displayedRowsCount) {
			context.g.drawString(rowToString(model!!.getRow(row)), INSET + rectangle.minX.toInt(), y.toInt())
			y += cellHeight
		}
	}

	private fun rowToString(row: TerminalRow): String {
		val builder = StringBuilder()
		row.iterator().forEach { builder.append(it) }
		return builder.toString()
	}
}