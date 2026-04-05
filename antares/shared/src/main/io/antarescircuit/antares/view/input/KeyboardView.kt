package io.antarescircuit.antares.view.input

import io.antarescircuit.antares.model.EnterBehavior
import io.antarescircuit.antares.model.input.Keyboard
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.base.event.KeyEvent
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.graphics.LogicalFontFamily
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.execution.actor.ActorInteractionContext
import io.antarescircuit.jabbah.execution.actor.ActorInteractionHandler
import io.antarescircuit.jabbah.execution.actor.ActorView
import io.antarescircuit.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElementEvent
import io.antarescircuit.jabbah.graph.model.vertice.VerticeLink
import io.antarescircuit.jabbah.graph.view.AbstractGraphElementView
import io.antarescircuit.jabbah.graph.view.ControlView
import io.antarescircuit.jabbah.graph.view.ControlViewSource
import io.antarescircuit.jabbah.graph.view.LabeledRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.port.PortLabelPosition
import io.antarescircuit.jabbah.graph.view.style.GraphTheme
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/** A view representation of a [Keyboard].*/
class KeyboardView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Keyboard = Keyboard()
) : LabeledRectangularVerticeView<Keyboard>(
	styleProvider,
	model,
	Rectangle2D(AbstractAntaresPortView.LENGTH, -HEIGHT / 2, 0, 0)
), ControlView<Keyboard>, ControlViewSource<Keyboard> {

	companion object {
		const val PROP_ICON_PATH = "io.antarescircuit.antares.view.input.KeyboardView.iconPath"

		private val LOG by logger(KeyboardView::class)
		private const val WIDTH = 20 * Look.SCALE
		private const val HEIGHT = 6 * Look.SCALE
		private const val INSET = Look.SCALE
		private const val RIGHT_INSET = 2 * Look.SCALE
		private const val TEXT_INSET = Look.SCALE
	}

	private val actorInteractionHandler = InteractionHandler()

	/** The [Label] used for outputting the entered characters. */
	private val outputLabel = Label(
		text = "Test",
		font = font.deriveFont(LogicalFontFamily.MONOSPACED),
		color = styleProvider.getStyle(StyleType.BACKGROUND).color.textColor,
		horizontalAlignment = HorizontalAlignment.LEFT,
		verticalAlignment = VerticalAlignment.CENTER,
		location = Point2D(AbstractAntaresPortView.LENGTH + INSET + TEXT_INSET, 0),
		richText = false)

	init {
		initExternalLabel(Direction.NORTH)
		isFocusable = true
		modelExchanged(null)
		width = WIDTH.toDouble()
		height = HEIGHT.toDouble()
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(AbstractAntaresPortView.LENGTH + WIDTH / 2.0, bounds.minY - LABEL_DIST)

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

	@Suppress("unused") // Reflection
	var enterBehavior: EnterBehavior
		get() = model.enterBehavior
		set(value) {
			if (value != model.enterBehavior) {
				invalidate()
				model.enterBehavior = value
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
			x = WIDTH + AbstractAntaresPortView.LENGTH,
			y = 0))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.readEnableInput,
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = WIDTH + AbstractAntaresPortView.LENGTH - 2 * Look.SCALE,
			y = HEIGHT / 2))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.clearInput,
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = WIDTH + AbstractAntaresPortView.LENGTH - 5 * Look.SCALE,
			y = HEIGHT / 2))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.dataOutput,
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			showBitWidthAnnotation = false,
			x = AbstractAntaresPortView.LENGTH,
			y = 0))

		addPortView(DigitalPortView(
			styleProvider = styleProvider,
			port = model.availableData,
			direction = Direction.SOUTH,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = AbstractAntaresPortView.LENGTH + 2 * Look.SCALE,
			y = HEIGHT / 2))
	}

	override fun handleStateChanged(event: GraphElementEvent) {
		invalidate()
		outputLabel.text = buildDisplayedText()
		validate()
	}

	private fun buildDisplayedText(): String {
		val builder = StringBuilder()
		model.getBytes().forEach { builder.append(KeyHandler.displayKey(it.toInt())) }
		return builder.toString()
	}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
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

	override var isActiveControlView: Boolean = false

	override val mirrorWidth: Double get() = 2 * AbstractAntaresPortView.LENGTH + width

	override fun bindControlView(subGraphVerticeView: SubGraphVerticeView<*>, link: VerticeLink, startGraph: Graph) {
		this.model = link.getLinkedObject(startGraph) as Keyboard
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

		drawFill(context, bounds, transparent.applyTo(if (context.useContextColors) context.chooseBackground(backgroundColor) else propertiesBackgroundColor))
		drawStroke(context, bounds, transparent.applyTo(context.chooseForeground(foregroundColor)), stroke)

		context.g.color = transparent.applyTo(context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).backgroundColor)
		context.g.fillRect(x + INSET, y + INSET, width - INSET - RIGHT_INSET, height - 2 * INSET)

		if (model.isFull) {
			context.g.color = transparent.applyTo(Themes.get<GraphTheme>().error.foregroundColor)
		} else {
			context.g.color = transparent.applyTo(context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).foregroundColor)
		}
		context.g.drawRect(x + INSET, y + INSET, width - INSET - RIGHT_INSET, height - 2 * INSET)

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			drawFocus(context)
			drawBuffer(context)
		}
	}

	override fun drawFocus(context: DrawContext) {
		if (isFocusOwner) {
			context.g.color = transparent.applyTo(Themes.get<AntaresTheme>().focus.color.foregroundColor)
			context.g.stroke = Themes.get<AntaresTheme>().focus.stroke
			context.g.drawRect(x + INSET - 1, y + INSET - 1, width - INSET - RIGHT_INSET + 2, height - 2 * INSET + 2)
		}
	}

	private fun drawBuffer(context: DrawContext) {
		val oldClip = context.g.getClipBounds()
		context.g.setClipBounds((x + INSET).toInt(), (y + INSET).toInt(), (width - INSET - RIGHT_INSET).toInt(), (height - 2 * INSET).toInt())
		outputLabel.color = transparent.applyTo(styleProvider.getStyle(StyleType.BACKGROUND).color.textColor)
		outputLabel.draw(context)
		context.g.setClipBounds(oldClip)
	}

	private inner class InteractionHandler : ClickableActorInteractionHandlerAdapter() {

		override fun mousePressed(context: ActorInteractionContext): ActorInteractionHandler? {
			requestFocus()
			return null
		}

		override fun keyPressed(context: ActorInteractionContext): ActorInteractionHandler? {
			if (!isFocusOwner) {
				return null
			}
			val keyChar = context.keyEvent!!.keyChar
			LOG.trace("keyPressed '$keyChar'")
			if (KeyHandler.acceptKey(keyChar.code)) {
				val code = when (keyChar.code) {
					KeyEvent.VK_ESCAPE -> KeyHandler.ESCAPE
					'\n'.code -> if (model.enterBehavior == EnterBehavior.CR) '\r'.code.toByte() else '\n'.code.toByte()
					else -> keyChar.code
				}
				model.enter(code.toByte(), context.signalHandler)
			} else {
				LOG.trace("reject character '$keyChar'")
			}
			return null
		}
	}

	object KeyHandler {
		const val ESCAPE = 27
		private const val MIN_CHAR = ' '.code
		private const val MAX_CHAR = '~'.code
		private const val MAX_ASCII = 127

		fun acceptKey(keyCode: Int) = keyCode <= MAX_ASCII

		fun displayKey(keyChar: Int): String {
			return when (keyChar) {
				in MIN_CHAR..MAX_CHAR -> keyChar.toChar().toString()
				0 -> "<NUL>"	// CTRL + @
				1 -> "<SOH>"	// CTRL + A
				2 -> "<STX>"	// CTRL + B
				3 -> "<ETX>"	// CTRL + C
				4 -> "<EOT>"	// CTRL + D
				5 -> "<ENQ>"	// CTRL + E
				6 -> "<ACK>"	// CTRL + F
				7 -> "<BEL>"	// CTRL + G
				8 -> "\\b"		// CTRL + H
				9 -> "\\t"		// CTRL + I
				10 -> "\\n"		// CTRL + J
				11 -> "<VT>"	// CTRL + K
				12 -> "<FF>"	// CTRL + L
				13 -> "\\r"		// CTRL + M
				14 -> "<SO>"	// CTRL + N
				15 -> "<SI>"	// CTRL + O
				16 -> "<DLE>"	// CTRL + P
				17 -> "<DC1>"	// CTRL + Q
				18 -> "<DC2>"	// CTRL + R
				19 -> "<DC3>"	// CTRL + S
				20 -> "<DC4>"	// CTRL + T
				21 -> "<NAK>"	// CTRL + U
				22 -> "<SYN>"	// CTRL + V
				23 -> "<ETB>"	// CTRL + W
				24 -> "<CAN>"	// CTRL + X
				25 -> "<EM>"	// CTRL + Y
				26 -> "<SUB>"	// CTRL + Z
				27 -> "<ESC>"	// CTRL + [
				28 -> "<FS>"	// CTRL + \
				29 -> "<GS>"	// CTRL + ]
				30 -> "<RS>"	// CTRL + ^
				31 -> "<US>"	// CTRL + _
				else -> ""
			}
		}
	}
}