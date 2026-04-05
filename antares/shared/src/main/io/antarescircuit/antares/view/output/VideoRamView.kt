package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.model.addressable.*
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.RoundRectangle2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.RectangularDrawable
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.graphics.RasterImage
import io.antarescircuit.jabbah.draw.graphics.RasterImageFactory
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.edit.model.Size
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.vertice.VerticeLink
import io.antarescircuit.jabbah.graph.view.AbstractGraphElementView
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.LabeledRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.math.abs
import kotlin.math.max

class VideoRamView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	colorModel: VideoRamColorModel = VideoRamColorModel.CGA_16,
	model: RAM = RAM(hasClock = true).also { it.dataWidth = colorModel.dataBitWidth },
	private val rasterImageFactory: RasterImageFactory = DrawModule.rasterImageFactory
) : LabeledRectangularVerticeView<RAM>(
	styleProvider,
	model,
	RoundRectangle2D(0.0, 0.0, 0.0, 0.0, ROUND_ARC.toDouble(), ROUND_ARC.toDouble())
), ControlViewSource<RAM>, ControlView<RAM> {
	companion object {
		const val PROP_ICON_PATH = "io.antarescircuit.antares.view.output.VideoRamView.iconPath"

		// TODO: Sync with TerminalView
		private const val BORDER_WIDTH = 2 * Look.SCALE
		private const val SCREEN_INSET = 0
		private const val ROUND_ARC = 10

		private const val MIN_WIDTH = 14 * Look.SCALE
		private const val MIN_HEIGHT = 14 * Look.SCALE

		private const val MAX_COLUMNS_COUNT = 600
		private const val MAX_ROWS_COUNT = 400
	}

	private val calculatedWidth get() = max(MIN_WIDTH, BORDER_WIDTH + SCREEN_INSET + columnsCount * pixelSize + SCREEN_INSET + BORDER_WIDTH)

	private val calculatedHeight get() = max(MIN_HEIGHT, BORDER_WIDTH + SCREEN_INSET + rowsCount * pixelSize + SCREEN_INSET + BORDER_WIDTH)

	private val pixelSize: Int get() =
		when (size) {
			Size.SMALL -> 2
			Size.MEDIUM -> 6
			Size.LARGE -> 10
		}

	var dataWidth: BitWidth
		get() = model.dataWidth
		set(value) {
			if (isNotReading) {
				throwIfInconsistentBitWidths(value, colorModel.dataBitWidth)
			}
			updateAddressBitWidth(rowsCount, columnsCount, value.width, colorModel.dataBitWidth.width)
			model.dataWidth = value
		}

	var size: Size = Size.MEDIUM
		set(value) {
			if (field != value) {
				field = value
				updateGeometry()
				validate()
			}
		}

	var rowsCount: Int = 50
		set(value) {
			if (field != value) {
				if (value > MAX_ROWS_COUNT) {
					throw IllegalArgumentException("Maximum height is $MAX_ROWS_COUNT")
				}
				updateAddressBitWidth(value, columnsCount, dataWidth.width, colorModel.dataBitWidth.width)
				field = value
				updateGeometry()
				validate()
			}
		}

	var columnsCount: Int = 50
		set(value) {
			if (field != value) {
				if (value > MAX_COLUMNS_COUNT) {
					throw IllegalArgumentException("Maximum width is $MAX_COLUMNS_COUNT")
				}
				updateAddressBitWidth(rowsCount, value, dataWidth.width, colorModel.dataBitWidth.width)
				field = value
				updateGeometry()
				validate()
			}
		}

	var colorModel: VideoRamColorModel = colorModel
		set(value) {
			if (isNotReading) {
				throwIfInconsistentBitWidths(dataWidth, value.dataBitWidth)
			}
			updateAddressBitWidth(rowsCount, columnsCount, dataWidth.width, value.dataBitWidth.width)
			field = value
		}

	private lateinit var bufferedImage: RasterImage

	private val pixelPerDataCell: Int get() = dataWidth.width / colorModel.dataBitWidth.width

	private val dataChangeListener = object : AddressableListener {

		override fun dataChanged(event: AddressableDataEvent) {
			event.address?.let { address ->
				var value = event.newValue!!
				for (i in 0 until pixelPerDataCell) {
					val subValue = value and this@VideoRamView.colorModel.dataBitWidth.maxValue
					value = value.shr(this@VideoRamView.colorModel.dataBitWidth.width)

					val color = this@VideoRamView.colorModel.getColor(subValue.toInt())
					val x = (pixelPerDataCell * (address.rem(columnsCount)) + i) * pixelSize
					val y = (address / (columnsCount * pixelPerDataCell)) * pixelSize
					fillPixel(x, y, color)
				}
			} ?: fillImage()

			invalidate()
			validate()
		}

		override fun commentChanged(event: AddressableCommentEvent) { }

		override fun bitWidthChanged(event: AddressableBitWidthEvent) { }
	}

	init {
		initExternalLabel(Direction.NORTH)
		modelExchanged(null)
		model.dataWidth = colorModel.dataBitWidth
		createImage()
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(bounds.centerX, bounds.minY - LABEL_DIST)

	private fun updateAddressBitWidth(rows: Int, columns: Int, dataBitWidth: Int, colorModelBitWidth: Int) {
		val pixelPerDataCell = dataBitWidth / colorModelBitWidth
		model.getAddressInput().bitWidth = BitWidth.smallest((rows * columns / pixelPerDataCell - 1).toULong())
			?: throw IllegalArgumentException("Width x height too big for address space")
	}

	private fun createImage() {
		bufferedImage = rasterImageFactory.invoke(columnsCount * pixelSize, rowsCount * pixelSize)
	}

	private fun throwIfInconsistentBitWidths(dataWidth: BitWidth, colorModelBitWidth: BitWidth) {
		if (dataWidth.width < colorModelBitWidth.width) {
			throw IllegalArgumentException(Translations.getString("element.property.VideoRam.dataBitWidth.tooSmall.text"))
		}
		if (dataWidth.width % colorModelBitWidth.width != 0) {
			throw IllegalArgumentException(Translations.getString("element.property.VideoRam.dataBitWidth.notMultiple.text"))
		}
	}

	/** ---- [AbstractGraphElementView] */

	override fun modelExchanged(oldModel: RAM?) {
		super.modelExchanged(oldModel)

		oldModel?.let { it.removeListener(dataChangeListener) }
		model.addListener(dataChangeListener)

		model.type = Translations.getString("library.element.VideoRam.name")
		model.typeDesc = Translations.getOptionalString("library.element.VideoRam.desc")

		model.isAdjustableBitWidth = false

		addPortView(
			DigitalPortView(
			styleProvider = styleProvider,
			port = model.getClockInput()!!,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = AbstractAntaresPortView.LENGTH,
			y = 0))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getDataPort(),
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			showBitWidthAnnotation = false,
			x = AbstractAntaresPortView.LENGTH,
			y = -4 * Look.SCALE))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getAddressInput(),
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			showBitWidthAnnotation = false,
			x = AbstractAntaresPortView.LENGTH,
			y = -8 * Look.SCALE))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getChipSelectInput(),
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = AbstractAntaresPortView.LENGTH + 3 * Look.SCALE,
			y = 3 * Look.SCALE))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getWriteInput(),
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = AbstractAntaresPortView.LENGTH + 7 * Look.SCALE,
			y = 3 * Look.SCALE))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getClearInput(),
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = AbstractAntaresPortView.LENGTH + 11 * Look.SCALE,
			y = 3 * Look.SCALE))

		updateGeometry()
	}

	override fun updateGeometry() {
		invalidate()
		setBounds(
			x = AbstractAntaresPortView.LENGTH.toDouble(),
			y = -calculatedHeight + 3 * Look.SCALE.toDouble(),
			w = calculatedWidth.toDouble(),
			h = calculatedHeight.toDouble())
		createImage()

		super.updateGeometry()
	}

	/** ---- [RectangularDrawable] */

	override fun setBounds(x: Double, y: Double, w: Double, h: Double) {
		if (this.x == x && this.y == y && this.width == w && this.height == h) {
			return
		}
		invalidate()
		rectangle.setFrame(x, y, w, h)
		updateBoxes()
		invalidate()
		if (!isResolving) {
			update()
		}
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("width", columnsCount)
		writer.writeInt("height", rowsCount)
		writer.writeString("colorModel", colorModel.customName)
		writer.writeString("pixelSize", size.customName)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		columnsCount = reader.readInt("width")
		rowsCount = reader.readInt("height")
		colorModel = VideoRamColorModel.withName(reader.readString("colorModel"))
		size = Size.withName(reader.readString("pixelSize"))
	}

	/** ---- [ControlViewSource] */

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override val controlId: String get() = "videoRam:" + model.id

	override val controlName: String get() = super.controlName

	override fun createControlView(): ControlView<RAM> {
		val clone = VideoRamView(styleProvider, colorModel, model)
		clone.isShowPortViews = false
		clone.location = Point2D.ZERO
		copyControlViewProperties(this, clone)
		return clone
	}

	private fun copyControlViewProperties(source: VideoRamView, dest: VideoRamView) {
		dest.size = source.size
		dest.rowsCount = source.rowsCount
		dest.columnsCount = source.columnsCount
		dest.colorModel = source.colorModel
	}

	/** ---- [ControlView] */

	override var isActiveControlView: Boolean = false

	override val mirrorWidth: Double get() = 2 * AbstractAntaresPortView.LENGTH + width

	override val mirrorHeight: Double get() = abs(abs(bounds.maxY) - abs(bounds.minY))

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		this.model = link.getLinkedObject(startGraph) as RAM
	}

	override fun sourcePropertiesChanged(source: ControlViewSource<RAM>) {
		if (source is VideoRamView) {
			copyControlViewProperties(source, this)
		}
	}

	override fun writeModelProperties(writer: StoreWriter) { }

	override fun readModelProperties(reader: StoreReader) { }

	/** ---- [AbstractVerticeView] */

	override fun executionStarted(signalHandler: SignalHandler) {
		createImage()
	}

	override fun drawImpl(context: DrawContext) {
		val oldStylable = context.stylable

		context.stylable = this
		drawImplBeforeBorder(context)
		context.stylable = oldStylable

		drawBody(context)
		drawBackground(context)
		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			drawImage(context)
		}

		context.stylable = this
		drawImplAfterBorder(context)
		context.stylable = oldStylable
	}

	private fun drawBody(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRoundRect(x.toInt(), y.toInt(), width.toInt(), height.toInt(),
					ROUND_ARC,
					ROUND_ARC
				)
			}
		}

		drawFill(context, rectangle, if (context.useContextColors) transparent.applyTo(context.chooseBackground(backgroundColor)) else transparent.applyTo(propertiesBackgroundColor))
		drawStroke(context, rectangle, getApplicableForegroundColor(context), stroke)
	}

	private fun drawImage(context: DrawContext) {
		context.g.drawImage(
			bufferedImage,
			rectangle.x.toInt() + BORDER_WIDTH + SCREEN_INSET,
			rectangle.y.toInt() + BORDER_WIDTH + SCREEN_INSET)
	}

	// Visible for testing
	fun fillImage() {
		val f = pixelPerDataCell
		var y = 0
		for (row in 0 until rowsCount) {
			var x = 0
			val rowBaseAddress = row * columnsCount / f
			for (col in 0 until columnsCount) {
				val address = rowBaseAddress + col / f
				val value = model.read(address)
				val subIndex = col % f
				val subValue = value.shr(subIndex * this@VideoRamView.colorModel.dataBitWidth.width) and this@VideoRamView.colorModel.dataBitWidth.maxValue
				val color = colorModel.getColor(subValue.toInt())
				fillPixel(x, y, color)
				x += pixelSize
			}
			y += pixelSize
		}
	}

	private fun fillPixel(x: Int, y: Int, color: Color) {
		for (pY in 0 until pixelSize) {
			for (pX in 0 until pixelSize) {
				bufferedImage.setColor(x + pX, y + pY, color)
			}
		}
	}

	private fun drawBackground(context: DrawContext) {
		val contentColor = if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			Themes.get<AntaresTheme>().screen
		} else {
			Themes.get<AntaresTheme>().background.color
		}
		context.g.color = transparent.applyTo(context.choose(contentColor).backgroundColor)
		context.g.fillRect(
			rectangle.x + BORDER_WIDTH, rectangle.y + BORDER_WIDTH,
			rectangle.width - 2 * BORDER_WIDTH, rectangle.height - 2 * BORDER_WIDTH
		)

		context.g.color = getApplicableForegroundColor(context)
		context.g.stroke = stroke
		context.g.drawRect(
			rectangle.x + BORDER_WIDTH - stroke.width / 2, rectangle.y + BORDER_WIDTH - stroke.width / 2,
			rectangle.width - 2 * BORDER_WIDTH + stroke.width, rectangle.height - 2 * BORDER_WIDTH + stroke.width
		)
	}
}