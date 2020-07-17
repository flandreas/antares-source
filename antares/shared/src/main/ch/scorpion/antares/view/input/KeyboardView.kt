package ch.scorpion.antares.view.input

import ch.scorpion.antares.model.input.Keyboard
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.graphics.FontFamily
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.style.GraphTheme
import ch.scorpion.jabbah.graph.view.vertice.AbstractRectangularVerticeView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/** A view representation of a [Keyboard].*/
class KeyboardView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Keyboard = Keyboard()
) : AbstractRectangularVerticeView<Keyboard>(
	styleProvider,
	model,
	x = DigitalPortView.LENGTH.toDouble(),
	y = -HEIGHT / 2.0
), ControlView<Keyboard>, ControlViewSource<Keyboard> {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.input.KeyboardView.iconPath"

		private val LOG by logger(KeyboardView::class)
		private const val WIDTH = 20 * Look.SCALE
		private const val HEIGHT = 6 * Look.SCALE
		private const val INSET = Look.SCALE
		private const val RIGHT_INSET = 2 * Look.SCALE
		private const val TEXT_INSET = Look.SCALE
	}

	private val actorInteractionHandler = InteractionHandler()

	private val label = Label(
		text = "Test",
		font = font.deriveFont(FontFamily.MONOSPACED),
		color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor,
		horizontalAlignment = HorizontalAlignment.LEFT,
		verticalAlignment = VerticalAlignment.CENTER,
		location = Point2D(DigitalPortView.LENGTH + INSET + TEXT_INSET, 0))

	private val propertiesBackgroundColor get() = if (Look.FILL_BASIC_COMPONENTS) backgroundColor else styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor

	init {
		isFocusable = true
		modelExchanged(null)
		width = WIDTH.toDouble()
		height = HEIGHT.toDouble()
	}

	/** ---- UI properties */

	var bufferSize: Int
		get() = model.bufferSize
		set(value) {
			if (value != model.bufferSize) {
				invalidate()
				model.bufferSize = value
				invalidate()
			}
		}

	/** ---- [AbstractGraphElementView] */

	override fun modelExchanged(oldModel: Keyboard?) {
		super.modelExchanged(oldModel)

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.clockInput,
			direction = Direction.EAST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = WIDTH + DigitalPortView.LENGTH,
			y = 0))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.readEnableInput,
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = WIDTH + DigitalPortView.LENGTH - 2 * Look.SCALE,
			y = HEIGHT / 2))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.clearInput,
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = WIDTH + DigitalPortView.LENGTH - 5 * Look.SCALE,
			y = HEIGHT / 2))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.dataOutput,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			showBitWidthAnnotation = false,
			x = DigitalPortView.LENGTH,
			y = 0))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.availableData,
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = DigitalPortView.LENGTH + 2 * Look.SCALE,
			y = HEIGHT / 2))
	}

	override fun handleStateChanged(event: GraphElementEvent) {
		invalidate()
		label.text = buildDisplayedText()
		validate()
	}

	private fun buildDisplayedText(): String {
		val builder = StringBuilder()
		model.getBytes().forEach { builder.append(KeyHandler.displayKey(it.toInt())) }
		return builder.toString()
	}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler? {
		return actorInteractionHandler
	}

	/** ---- [ControlViewSource] */

	override val controlId: String get() = "keyboard:" + model.id

	override val controlName: String get() = super.controlName

	override val iconPath: String get() = BaseModule.properties.getString(PROP_ICON_PATH)

	override fun createControlView(): ControlView<Keyboard> {
		val clone = KeyboardView(styleProvider, model)
		clone.isShowPortViews = false
		clone.location = Point2D.ZERO
		copyControlViewProperties(this, clone)
		return clone
	}

	/** ---- [ControlView] */

	override fun bindToModel(model: Keyboard) {
		this.model = model
	}

	override fun sourcePropertiesChanged(source: ControlViewSource<Keyboard>) {
		if (source is KeyboardView) {
			copyControlViewProperties(source, this)
		}
	}

	override fun writeModelProperties(writer: StoreWriter) {
		writer.writeInt("bufferSize", bufferSize)
	}

	override fun readModelProperties(reader: StoreReader) {
		bufferSize = reader.readInt("bufferSize")
	}

	@Suppress("UNUSED_PARAMETER")
	private fun copyControlViewProperties(source: KeyboardView, dest: KeyboardView) {
		// empty
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		val oldStylable = context.stylable

		context.stylable = this
		drawImplBeforeBorder(context)
		context.stylable = oldStylable

		drawBody(context)

		context.stylable = this
		drawImplAfterBorder(context)
		context.stylable = oldStylable
	}

	private fun drawBody(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(x, y, width, height)
			}
		}

		drawFill(context, bounds, if (context.useContextColors) context.choose(color).backgroundColor else propertiesBackgroundColor)
		drawStroke(context, bounds, context.choose(color).foregroundColor, stroke)

		context.g.color = context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).backgroundColor
		context.g.fillRect(x + INSET, y + INSET, width - INSET - RIGHT_INSET, height - 2 * INSET)

		if (model.isFull) {
			context.g.color = transparent.applyTo(Themes.get<GraphTheme>().error.foregroundColor)
		} else {
			context.g.color = context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).foregroundColor
		}
		context.g.drawRect(x + INSET, y + INSET, width - INSET - RIGHT_INSET, height - 2 * INSET)

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			drawFocus(context)
			drawBuffer(context)
		}
	}

	private fun drawFocus(context: DrawContext) {
		if (isFocusOwner) {
			context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
			context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
			context.g.drawRect(x + INSET - 1, y + INSET - 1, width - INSET - RIGHT_INSET + 2, height - 2 * INSET + 2)
		}
	}

	private fun drawBuffer(context: DrawContext) {
		val oldClip = context.g.getClipBounds()
		context.g.setClipBounds((x + INSET).toInt(), (y + INSET).toInt(), (width - INSET - RIGHT_INSET).toInt(), (height - 2 * INSET).toInt())
		label.color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor
		label.draw(context)
		context.g.setClipBounds(oldClip.x.toInt(), oldClip.y.toInt(), oldClip.width.toInt(), oldClip.height.toInt())
	}

	private inner class InteractionHandler : ClickableActorInteractionHandlerAdapter() {

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			requestFocus()
			return null
		}

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
			val keyChar = context.keyEvent!!.keyChar
			LOG.debug("keyPressed '$keyChar'")
			if (KeyHandler.acceptKey(keyChar)) {
				model.enter(keyChar.toByte(), context.signalHandler)
			} else {
				LOG.debug("reject character '$keyChar'")
			}
			return null
		}
	}

	object KeyHandler {

		private const val BACKSPACE = 8
		private const val TAB = 9
		private const val LINEFEED = 10
		private const val MIN_CHAR = ' '.toInt()
		private const val MAX_CHAR = '~'.toInt()

		fun acceptKey(keyChar: Char): Boolean {
			return keyChar.toInt() in MIN_CHAR..MAX_CHAR
				|| keyChar.toInt() == BACKSPACE
				|| keyChar.toInt() == TAB
				|| keyChar.toInt() == LINEFEED
		}

		fun displayKey(keyChar: Int): String {
			return when (keyChar) {
				in MIN_CHAR..MAX_CHAR -> keyChar.toChar().toString()
				BACKSPACE -> "\\b"
				TAB -> "\\t"
				LINEFEED -> "\\n"
				else -> ""
			}
		}
	}
}