package ch.scorpion.antares.view.inout

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.event.KeyEvent
import ch.scorpion.jabbah.base.event.KeyListener
import ch.scorpion.jabbah.base.event.MouseAdapter
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.DrawableAttendantPositioner
import ch.scorpion.jabbah.draw.style.*
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.draw.Focusable
import ch.scorpion.jabbah.edit.model.text.TextDrawableButtonRenderer
import ch.scorpion.jabbah.execution.actor.ActorDrawableButton
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorViewContainer
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.view.GraphView

/**
 * Displays a keyboard for entering digits into a digital target object during simulation.
 *
 * Note that this object shouldn't request the focus (and therefore can't receive key events)
 * because focus should remain in the target object, e.g. for advancing digits after entering values.
 */
object DigitalKeyboard : ActorViewContainer<Drawable>(useLocation = true), Focusable {

	/** The object consuming the entered key.*/
	interface Target {
		val keyboardTargetBoundingBox: RectangularShape
		val signalRepresentation: DigitalSignalRepresentation
		fun consumeKey(key: Int, contextHolder: GraphApplicationContextHolder, graphView: GraphView?)
		fun clear(contextHolder: GraphApplicationContextHolder)
	}

	private const val VERTICAL_DISTANCE = 0
	private const val MARGIN = 10
	private const val PADDING = 5
	private const val BUTTON_GAP = 3
	private const val BUTTON_SIZE = 25
	private const val KEYBOARD_WIDTH = 2 * MARGIN + 2 * PADDING + 4 * BUTTON_SIZE + 3 * BUTTON_GAP
	private const val KEYBOARD_HEIGHT = 2 * MARGIN + 2 * PADDING + 5 * BUTTON_SIZE + 4 * BUTTON_GAP

	private val styleType: StyleType = StyleType.FIGURE

	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider

	private val style: Style get() = styleProvider.getStyle(styleType)

	private var target: Target? = null

	private var view: DrawingView<*>? = null

	private var contextHolder: GraphApplicationContextHolder? = null

	private val positioner: DrawableAttendantPositioner = DrawableAttendantPositioner

	private var bounds: Rectangle2D = Rectangle2D(Point2D.ZERO, Dimension2D(KEYBOARD_WIDTH, KEYBOARD_HEIGHT))

	private val digitButtons = mutableListOf<ActorDrawableButton<*>>()

	private val actorInteractionHandler = Handler { super.getActorInteractionHandler(it) }

	private val quitHandler = QuitHandler()

	init {
		buildHexadecimalUI()
	}

	fun show(
		target: Target,
		view: DrawingView<*>,
		contextHolder: GraphApplicationContextHolder
	) {
		hide()

		this.target = target
		this.view = view
		this.contextHolder = contextHolder

		position(target.keyboardTargetBoundingBox, view)
		updateUI()

		view.animationContainer.add(this)
		view.animationContainer.validate()
		view.addMouseListener(quitHandler)
		view.addKeyListener(quitHandler)
	}

	fun hide() {
		view?.let {
			it.animationContainer.remove(this)
			it.animationContainer.validate()
			it.removeMouseListener(quitHandler)
			it.removeKeyListener(quitHandler)
		}
		target = null
		view = null
	}

	private fun position(boundingBox: RectangularShape, view: View<*>) {
		val loc = positioner.position(
			Dimension2D(KEYBOARD_WIDTH, KEYBOARD_HEIGHT),
			boundingBox,
			view,
			preferredBelow = true,
			VERTICAL_DISTANCE)
		bounds = Rectangle2D(loc, Dimension2D(KEYBOARD_WIDTH, KEYBOARD_HEIGHT))
		location = loc
	}

	/** ---- [DrawableContainer] */

	override fun contains(x: Double, y: Double): Boolean = bounds.contains(x, y)

	override fun draw(context: DrawContext) {
		// Draw margin area
		context.g.color = Themes.get<AntaresTheme>().background.color.foregroundColor
		context.g.fill(bounds)

		// Draw keyboard background
		context.g.color = style.color.backgroundColor
		context.g.fillRect(location.xInt + MARGIN, location.yInt + MARGIN, KEYBOARD_WIDTH - 2 * MARGIN, KEYBOARD_HEIGHT - 2 * MARGIN)

		context.g.color = style.color.foregroundColor
		context.g.stroke = style.stroke
		context.g.drawRect(location.xInt + MARGIN, location.yInt + MARGIN, KEYBOARD_WIDTH - 2 * MARGIN, KEYBOARD_HEIGHT - 2 * MARGIN)

		super.draw(context)
	}

	override val boundingBox: RectangularShape get() = Rectangle2D(bounds)

	/** ---- [ActorViewContainer] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		actorInteractionHandler

	/** ---- [Focusable] */

	override val isFocusable: Boolean get() = true

	/** ---- [DigitalKeyboard] */

	private fun buildHexadecimalUI() {
		var x = MARGIN + PADDING
		var y = MARGIN + PADDING
		for (i in 1..18) {
			val button = ActorDrawableButton<EditInputEventContext>(
				location = Point2D(x, y),
				styleType = styleType,
				renderer = TextDrawableButtonRenderer(
					text = BitOperation.HEX_CHAR[i - 1].toString(),
					dimension = Dimension2D(BUTTON_SIZE, BUTTON_SIZE),
					style = styleProvider.getStyle(styleType)),
				actorAction = { buttonClickHandler(BitOperation.HEX_KEY[i - 1], null) },
				round = true
			)
			digitButtons.add(button)
			add(button)

			x += BUTTON_SIZE + BUTTON_GAP
			if (i.mod(4) == 0) {
				x = MARGIN + PADDING
				y += BUTTON_SIZE + BUTTON_GAP
			}
		}
		val clearButton = ActorDrawableButton<EditInputEventContext>(
			location = Point2D(x, y),
			styleType = styleType,
			renderer = TextDrawableButtonRenderer(
				text = "Clear",
				dimension = Dimension2D(2 * BUTTON_SIZE + BUTTON_GAP, BUTTON_SIZE),
				style = styleProvider.getStyle(styleType)),
			actorAction = { clearHandler() },
			round = true
		)
		add(clearButton)
	}

	private fun updateUI() {
		digitButtons.forEachIndexed { index, button ->
			button.enabled = when (target!!.signalRepresentation) {
				DigitalSignalRepresentation.BINARY -> index < 2
				DigitalSignalRepresentation.OCTAL -> index < 8
				DigitalSignalRepresentation.DECIMAL -> index < 10
				DigitalSignalRepresentation.HEXADECIMAL -> true
				else -> false
			}
		}
	}

	private fun buttonClickHandler(key: Int, graphView: GraphView?) {
		target!!.consumeKey(key, contextHolder!!, graphView = graphView)
	}

	private fun clearHandler() {
		target!!.clear(contextHolder!!)
	}

	private class Handler(
		private val parent: (ActorInteractionContext) -> ActorInteractionHandler
	) : InputEventHandlerAdapter<ActorInteractionContext>() {

		private var hoverHandler: ActorInteractionHandler? = null

		override fun mouseReleased(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext> {
			return this
		}

		override fun mouseMoved(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext> {
			// Delegate to possible buttons, but keep "modal" control
			hoverHandler = hoverHandler?.mouseMoved(context)
				?: parent(context).mouseMoved(context)
			return this
		}

		override fun mouseClicked(context: ActorInteractionContext): InputEventHandler<ActorInteractionContext> {
			// Delegate to possible buttons, but keep "modal" control
			parent(context).mouseClicked(context)
			return this
		}
	}

	private class QuitHandler : MouseAdapter(), KeyListener {
		override fun mousePressed(e: MouseEvent) {
			view?.let {
				if (!contains(it.viewToModel(e.location))) {
					hide()
				}
			}
		}

		override fun keyTyped(e: KeyEvent) { }

		override fun keyPressed(e: KeyEvent) {
			if (e.key == KeyEvent.VK_ESCAPE) {
				hide()
			}
		}

		override fun keyReleased(e: KeyEvent) { }
	}
}