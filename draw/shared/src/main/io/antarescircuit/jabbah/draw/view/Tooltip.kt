package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.*
import io.antarescircuit.jabbah.base.event.*
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.richtext.RichTextParser
import io.antarescircuit.jabbah.base.time.Timer
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.drawable.ArrowBubble
import io.antarescircuit.jabbah.draw.drawable.ArrowBubblePositioner
import io.antarescircuit.jabbah.draw.drawable.RectangularDrawable
import io.antarescircuit.jabbah.draw.drawable.RichTextDrawable
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType

/**
 * The tooltip system consists of various parts used by client objects that want to display tooltips.
 *
 * Client objects such a selection tool instantiate a [TooltipHandler] and delegate handling of mouse move
 * event to that [TooltipHandler].
 *
 * The [TooltipHandler] posts a deferred content lookup on the [EventBus]. [TooltipManager] waits
 * until the mouse has rested before invoking that lookup, which finds the current [Drawable] and
 * asks it for its [Tooltip] and graphical explanation. A [Tooltip] contains text and a source rectangle;
 * the manager creates its graphical representation only when it is ready to display it.
 *
 * Dismissing [Tooltip]s is initiated by client objects either explicitly using [TooltipHandler.clear],
 * or implicitly using [TooltipHandler.handle] for a location where no [Tooltip] is needed.
 *
 * Tooltips can generally be enabled/disabled using [TooltipHandler.PROP_TOOLTIPS_ENABLED].
 *
 * [buildToolTipText] is used by client of the tooltip system to build structured texts to be displayed within a tooltip,
 * typically in implementations of [Drawable.getTooltip].
 *
 * @param title the title to be rendered in bold
 * @param text the text following the title
 * @param subText additional text separated from the previous text by an empty line
 * @param endWithPeriod if `true` add a period at the end of [subText] if it doesn't have one already
 */
fun buildToolTipText(
	title: String?,
	text: String?,
	subText: String?,
	endWithPeriod: Boolean = false
): String? {
	val builder = StringBuilder()
	val hasText = StringUtils.isNotEmpty(text)
	val hasSubText = StringUtils.isNotEmpty(subText)

	if (StringUtils.isNotBlank(title)) {
		var titleText = title!!
		if (hasText && !text!!.startsWith('\n')) {
			titleText += ":"
		}
		builder
			.append(RichTextParser.bold(titleText))
	}

	if (hasText) {
		builder.append(" ").append(text!!)
		if (endWithPeriod && !text.endsWith(".")) {
			builder.append('.')
		}
	}

	if (hasSubText) {
		if (builder.isNotEmpty()) {
			builder.appendLine().appendLine()
		}
		builder.append(subText!!)
		if (endWithPeriod && !subText.endsWith(".")) {
			builder.append('.')
		}
	}

	return if (builder.isEmpty()) null else builder.toString()
}

/** A hover request, or an owner-scoped cancellation when [resolve] is null. */
private data class TooltipEvent(
	val owner: TooltipHandler,
	val view: View<*>? = null,
	val resolve: (() -> TooltipContent?)? = null,
)

/** Content resolved once the pointer has rested. Either part can be absent. */
private data class TooltipContent(
	val tooltip: Tooltip?,
	val explanation: DrawableExplanation<RectangularDrawable>?,
)

/**
 * Submits deferred tooltip lookups for mouse moves. Neither hit testing nor content creation happens
 * until [TooltipManager]'s delay expires. Custom accessors also support execution-specific tooltips.
 */
class TooltipHandler(
	private val eventBus: EventBus,
	private val drawableRetriever: (DrawableContainer<*>, Double, Double) -> Drawable? = { c, x, y -> c.getDrawableAt(x, y) },
	private val tooltipAccessor: (Drawable, InputEventContext) -> Tooltip? = { d, context -> d.getTooltip(context) },
	private val explanationAccessor: (Drawable, Double, Double) -> DrawableExplanation<RectangularDrawable>? = { d, x, y -> d.getExplanation(x, y) },
) {

	companion object {

		/** The name of the [Boolean] property in [Properties] that enables tooltips. */
		const val PROP_TOOLTIPS_ENABLED = "draw.view.tooltipsEnabled"
	}

	private var disposed = false

	private val popupMenuHandler: EventHandler<PopupMenuEvent> = { clear(it.canvas.view) }

	init {
		eventBus.register(PopupMenuEvent::class, popupMenuHandler)
	}

	fun dispose() {
		disposed = true
		eventBus.post(TooltipEvent(this))
		eventBus.unregister(popupMenuHandler)
	}

	/** Replaces the pending hover lookup and starts a full delay from this mouse move. */
	fun handle(view: View<*>, container: DrawableContainer<*>, context: InputEventContext) {
		if (disposed) return
		if (!BaseModule.properties.getBoolean(PROP_TOOLTIPS_ENABLED)) {
			clear(view)
			return
		}
		eventBus.post(TooltipEvent(this, view) { resolve(container, context) })
	}

	private fun resolve(container: DrawableContainer<*>, context: InputEventContext): TooltipContent? {
		if (disposed || !BaseModule.properties.getBoolean(PROP_TOOLTIPS_ENABLED)) return null
		val drawable = drawableRetriever(container, context.x, context.y) ?: return null
		val tooltip = tooltipAccessor(drawable, context)?.takeUnless { StringUtils.isBlank(it.text) }
		val explanation = if (BaseModule.properties.getBoolean(PROP_BEGINNER_HELP_TOOLTIP)) {
			explanationAccessor(drawable, context.x, context.y)
		} else null
		return if (tooltip == null && explanation == null) null else TooltipContent(tooltip, explanation)
	}

	/** Cancels this handler's pending or displayed tooltip in [view]. */
	fun clear(view: View<*>) {
		eventBus.post(TooltipEvent(this, view))
	}
}

/** Holds the view data of a currently displayed [Tooltip] or [DrawableExplanation]. */
private data class TooltipView(
	val arrowBubble: ArrowBubble,
	val view: View<*>
)

/**
 * Listens for [TooltipEvent]s and displays the corresponding tooltip texts and graphical explanations
 * nearby the origin [Drawable] in the corresponding [View].
 */
object TooltipManager {

	/** The name of the [Int] property in [Properties] designating the time (in ms) of delaying the displaying of the tooltip.*/
	const val PROP_DELAY = "draw.view.TooltipManager.delay"

	private const val WIDTH = 300

	private val tooltipEventHandler: EventHandler<TooltipEvent> = { handle(it) }

	private val preferencesChangeHandler: EventHandler<PreferencesChangedEvent> = { initTimer() }

	/** The currently displayed text [TooltipView]. */
	private var textTooltip: TooltipView? = null

	/** The currently displayed explanation [TooltipView]. */
	private var explanationTooltip: TooltipView? = null

	/** Stores the last request to display a textTooltip until scheduled by the [timer] for displaying. */
	private var request: TooltipEvent? = null

	/** Used to delay the displaying of a requested textTooltip. */
	private var _timer: Timer? = null

	private val timer: Timer
		get() {
			if (_timer == null) {
				initTimer()
			}
			return _timer!!
		}

	/** The owner and view remain observed while content is pending or displayed. */
	private var owner: TooltipHandler? = null
	private var observedView: View<*>? = null
	private var observedCanvas: Canvas? = null

	private val viewChangeListener = PropertyChangeListener<Any> { tooltipDismissed() }

	private val inputListener = object : MouseAdapter(), KeyListener {
		override fun mouseExited(e: MouseEvent) = tooltipDismissed()
		override fun mousePressed(e: MouseEvent) = tooltipDismissed()
		override fun mouseClicked(e: MouseEvent) = tooltipDismissed()
		override fun mouseDragged(e: MouseEvent) = tooltipDismissed()
		override fun mouseWheelRotated(e: MouseEvent) = tooltipDismissed()
		override fun keyTyped(e: KeyEvent) = tooltipDismissed()
		override fun keyPressed(e: KeyEvent) = tooltipDismissed()
		override fun keyReleased(e: KeyEvent) = tooltipDismissed()
	}

	var eventBus: EventBus = BaseModule.eventBus
		set(value) {
			if (field != value) {
				tooltipDismissed()
				_timer = null
				eventBus.unregister(TooltipEvent::class, tooltipEventHandler)
				eventBus.unregister(PreferencesChangedEvent::class, preferencesChangeHandler)
				field = value
				field.register(TooltipEvent::class, tooltipEventHandler)
				field.register(PreferencesChangedEvent::class, preferencesChangeHandler)
			}
		}

	var styleProvider: StyleProvider = DrawStyleModule.styleProvider

	init {
		eventBus.register(TooltipEvent::class, tooltipEventHandler)
		eventBus.register(PreferencesChangedEvent::class, preferencesChangeHandler)
	}

	private fun initTimer() {
		tooltipDismissed()
		val newTimer = System.createTimer()
		_timer = newTimer
		newTimer.initialize(BaseModule.properties.getInt(PROP_DELAY), repeats = false) {
			if (_timer === newTimer) displayImpl()
		}
	}

	private fun handle(event: TooltipEvent) {
		if (event.resolve == null) {
			if (event.owner === owner && (event.view == null || event.view === observedView)) {
				tooltipDismissed()
			}
			return
		}

		// Timer.start() does not restart an already running timer.
		val hoverTimer = timer
		hoverTimer.stop()
		disposeTooltip()
		owner = event.owner
		request = event
		val view = event.view!!
		if (observedView !== view) {
			stopObservingView()
			observedView = view
			view.addPropertyChangeListener(viewChangeListener)
			if (view.canvasInitialized) {
				observedCanvas = view.canvas.also {
					it.addMouseListener(inputListener)
					it.addMouseMotionListener(inputListener)
					it.addMouseWheelListener(inputListener)
					it.addKeyListener(inputListener)
				}
			}
		}
		hoverTimer.start()
	}

	private fun tooltipDismissed() {
		_timer?.stop()
		request = null
		owner = null
		stopObservingView()
		disposeTooltip()
	}

	private fun stopObservingView() {
		observedView?.removePropertyChangeListener(viewChangeListener)
		observedView = null
		observedCanvas?.let {
			it.removeMouseListener(inputListener)
			it.removeMouseMotionListener(inputListener)
			it.removeMouseWheelListener(inputListener)
			it.removeKeyListener(inputListener)
		}
		observedCanvas = null
	}

	/** Resolves current content only after the mouse has rested for the full delay. */
	private fun displayImpl() {
		_timer?.stop()
		val pending = request ?: return
		val content = pending.resolve!!.invoke()
		// Resolution may itself have triggered cancellation or another hover request.
		if (request !== pending) return
		request = null
		if (content == null) {
			tooltipDismissed()
			return
		}
		val view = pending.view!!
		content.tooltip?.let {
			textTooltip = TooltipView(createTextArrowBubble(it, view), view)
			view.overlayContainer.add(textTooltip!!.arrowBubble)
		}
		content.explanation?.let {
			val bubble = createExplanationArrowBubble(it, view)
			// Don't show both TooltipViews above each other.
			if (textTooltip == null || textTooltip!!.arrowBubble.position.belowLocation != bubble.position.belowLocation) {
				explanationTooltip = TooltipView(bubble, view)
				view.overlayContainer.add(bubble)
			}
		}
		view.overlayContainer.validate()
	}

	private fun disposeTooltip() {
		val text = textTooltip
		val explanation = explanationTooltip
		textTooltip = null
		explanationTooltip = null
		text?.let {
			it.view.overlayContainer.remove(it.arrowBubble)
			it.view.overlayContainer.validate()
		}
		explanation?.let {
			it.view.overlayContainer.remove(it.arrowBubble)
			it.view.overlayContainer.validate()
		}
	}

	private fun createTextArrowBubble(tooltip: Tooltip, view: View<*>): ArrowBubble {
		val font = styleProvider.getStyle(StyleType.TOOLTIP).font
		val multilineText = RichTextDrawable.multiline(tooltip.text, font, WIDTH.toDouble())

		return ArrowBubble(
			multilineText,
			ArrowBubblePositioner.position(multilineText, tooltip.sourceRect, view, preferredBelow = true),
			StyleType.TOOLTIP,
			styleProvider
		)
	}

	private fun createExplanationArrowBubble(explanation: DrawableExplanation<RectangularDrawable>, view: View<*>): ArrowBubble {
		return ArrowBubble(
			explanation.explanation,
			ArrowBubblePositioner.position(explanation.explanation, explanation.sourceRect, view, preferredBelow = false),
			StyleType.TOOLTIP,
			styleProvider
		)
	}
}
