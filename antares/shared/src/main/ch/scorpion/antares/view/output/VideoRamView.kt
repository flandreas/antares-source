package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.addressable.AddressableDataListener
import ch.scorpion.antares.model.addressable.RAM
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RoundRectangle2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.graphics.BufferedImage
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.math.max

class VideoRamView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	colorModel: VideoRamColorModel = VideoRamColorModel.CGA_16,
	model: RAM = RAM(hasClock = true)
) : AbstractRectangularVerticeView<RAM>(
	styleProvider,
	model,
	RoundRectangle2D(0.0, 0.0, 0.0, 0.0, ROUND_ARC.toDouble(), ROUND_ARC.toDouble())
), ControlViewSource<RAM>, ControlView<RAM> {
	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.output.VideoRamView.iconPath"

		// TODO: Sync with TerminalView
		private const val BORDER_WIDTH = 2 * Look.SCALE
		private const val SCREEN_INSET = 0
		private const val ROUND_ARC = 10

		private const val MIN_WIDTH = 14 * Look.SCALE
		private const val MIN_HEIGHT = 14 * Look.SCALE

		private const val MAX_COLUMNS_COUNT = 600
		private const val MAX_ROWS_COUNT = 400
	}

	private val propertiesBackgroundColor get() = if (Look.FILL_BASIC_COMPONENTS) backgroundColor else styleProvider.getStyle(
		StyleType.BACKGROUND).color.backgroundColor

	private val calculatedWidth get() = max(MIN_WIDTH, BORDER_WIDTH + SCREEN_INSET + columnsCount * pixelSize + SCREEN_INSET + BORDER_WIDTH)

	private val calculatedHeight get() = max(MIN_HEIGHT, BORDER_WIDTH + SCREEN_INSET + rowsCount * pixelSize + SCREEN_INSET + BORDER_WIDTH)

	private val pixelSize: Int get() =
		when (size) {
			Size.SMALL -> 2
			Size.MEDIUM -> 6
			Size.LARGE -> 10
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
				field = value
				model.getAddressInput().bitWidth = BitWidth.smallest((field * columnsCount).toULong())
					?: throw IllegalArgumentException("Height too big for address space")
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
				field = value
				model.getAddressInput().bitWidth = BitWidth.smallest((field * rowsCount).toULong())
					?: throw IllegalArgumentException("Width too big for address space")
				updateGeometry()
				validate()
			}
		}

	var colorModel: VideoRamColorModel = colorModel
		set(value) {
			field = value
			model.dataWidth = field.dataBitWidth
		}

	private lateinit var bufferedImage: BufferedImage

	private val dataChangeListener = AddressableDataListener { event ->
		event.address?.let { address ->
			val value = event.newValue!!
			val color = colorModel.getColor(value.toInt())
			val x = address.rem(columnsCount) * pixelSize
			val y = (address / rowsCount) * pixelSize
			fillPixel(x, y, color)
		} ?: fillImage()

		invalidate()
		validate()
	}

	init {
		modelExchanged(null)
		model.dataWidth = colorModel.dataBitWidth
		createImage()
	}

	private fun createImage() {
		bufferedImage = DrawModule.bufferedImageFactory.invoke(columnsCount * pixelSize, rowsCount * pixelSize)
	}

	/** ---- [AbstractGraphElementView] */

	override fun modelExchanged(oldModel: RAM?) {
		super.modelExchanged(oldModel)

		oldModel?.let { it.removeDataListener(dataChangeListener) }
		model.addDataListener(dataChangeListener)

		addPortView(
			DigitalPortView(
			styleProvider = styleProvider,
			port = model.getClockInput()!!,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = DigitalPortView.LENGTH,
			y = 0))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getDataPort(),
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			showBitWidthAnnotation = false,
			x = DigitalPortView.LENGTH,
			y = -4 * Look.SCALE))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getAddressInput(),
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			showBitWidthAnnotation = false,
			x = DigitalPortView.LENGTH,
			y = -8 * Look.SCALE))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getChipSelectInput(),
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = DigitalPortView.LENGTH + 3 * Look.SCALE,
			y = 3 * Look.SCALE))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getWriteInput(),
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = DigitalPortView.LENGTH + 7 * Look.SCALE,
			y = 3 * Look.SCALE))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.getClearInput(),
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = DigitalPortView.LENGTH + 11 * Look.SCALE,
			y = 3 * Look.SCALE))

		updateGeometry()
	}

	private fun updateGeometry() {
		invalidate()
		setBounds(
			x = DigitalPortView.LENGTH.toDouble(),
			y = -calculatedHeight + 3 * Look.SCALE.toDouble(),
			w = calculatedWidth.toDouble(),
			h = calculatedHeight.toDouble())
		createImage()
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

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, model: RAM) {
		this.model =  model
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

		drawFill(context, rectangle, if (context.useContextColors) transparent.applyTo(context.choose(color).backgroundColor) else transparent.applyTo(propertiesBackgroundColor))
		drawStroke(context, rectangle, getApplicableForegroundColor(context), stroke)
	}

	private fun drawImage(context: DrawContext) {
		context.g.drawImage(
			bufferedImage,
			rectangle.x.toInt() + BORDER_WIDTH + SCREEN_INSET,
			rectangle.y.toInt() + BORDER_WIDTH + SCREEN_INSET)
	}

	private fun fillImage() {
		var y = 0
		for (row in 0 until rowsCount) {
			var x = 0
			for (col in 0 until columnsCount) {
				val value = model.read(row * columnsCount + col)
				val color = colorModel.getColor(value.toInt())
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