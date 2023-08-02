package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.richtext.RichTextParser
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.ArrowBubble
import ch.scorpion.jabbah.draw.drawable.ArrowBubblePositioner
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * The tooltip system consists of various parts used by client objects that want to display tooltips.
 *
 * Client objects such a selection tool instantiate a [TooltipHandler] and delegate handling of mouse move
 * event to that [TooltipHandler].
 *
 * The [TooltipHandler] searches for a [Drawable] at the mouse location and, if one is found, asks that
 * [Drawable] for its [Tooltip], typically using [Drawable.getTooltip]. A [Tooltip] consists of
 * a [RichTextDrawable] and a location where to display it (only data, no graphical representation).
 *
 * If there is a [Tooltip] to be displayed, [TooltipHandler] posts a [TooltipEvent] on the [EventBus],
 * which is handled by [TooltipManager] in order to display it. [TooltipManager] uses a [Timer] for
 * delaying [Tooltip] displaying, and creates the appropriate visual representation of a [Tooltip].
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
		builder.append(RichTextParser.bold(title!!))
		if (hasText) {
			builder.append(": ")
		}
	}

	if (hasText) {
		builder.append(text!!)
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

/**
 * Represents a request to display or hide the tooltip of a [Drawable] in a particular [View].
 *
 * @property origin the [Drawable] for which the tooltip is to be displayed, or `null`if the tooltip
 * should be hidden
 * @property view the [View] in which [origin] is displayed
 * @property tooltip the [Tooltip] to be displayed in [view], or `null` if the tooltip should be hidden
 */
private data class TooltipEvent(
	val origin: Drawable?,
	val view: View<*>,
	val tooltip: Tooltip?,
	val explanation: DrawableExplanation<RectangularDrawable>?
)

/**
 * A utility class that handles [MouseEvent], captures certain state information,
 * and fires [TooltipEvent] if necessary. Used for building clients of the tooltip system.
 *
 * @property eventBus the [EventBus] to use for posting [TooltipEvent]s
 * @property drawableRetriever retrieves the source of tooltip texts as the [Drawable] in the specified [DrawableContainer]
 *  at location (x,y)
 * @property tooltipAccessor accesses the tooltip text of the [Drawable] returned by [drawableRetriever]
 *  at location (x,y)
 *  @property explanationAccessor accesses the [DrawableExplanation] of the [Drawable] returned by [drawableRetriever]
 *  at location (x,y)
 */
class TooltipHandler(
	private val eventBus: EventBus,
	private val drawableRetriever: (DrawableContainer<*>, Double, Double) -> Drawable? = { c, x, y -> c.getDrawableAt(x, y)},
	private val tooltipAccessor: (Drawable, Double, Double) -> Tooltip? = { d, x, y -> d.getTooltip(x, y) },
	private val explanationAccessor: (Drawable, Double, Double) -> DrawableExplanation<RectangularDrawable>? = { d, x, y -> d.getExplanation(x, y) }
) {

	companion object {

		/** The name of the [Boolean] property in [Properties] that decides if textual HTML tooltips are to be displayed.*/
		const val PROP_TOOLTIPS_ENABLED = "draw.view.tooltipsEnabled"
	}

	/**
	 * The [Drawable] for which a [TooltipEvent] has been posted by this [TooltipHandler] recently.
	 * This reference is kept in order to avoid duplicate consecutive event posts.
	 */
	private var lastTooltipDrawable: Drawable? = null

	/**
	 * The text for which a [TooltipEvent] has been posted by this [TooltipHandler] recently.
	 * This reference is kept in order to avoid duplicate consecutive event posts.
	 */
	private var lastTooltipText: String? = null

	private var lastExplanation: DrawableExplanation<RectangularDrawable>? = null

	private var lastTooltipSourceRect: RectangularShape? = null

	private val tooltipsEnabled = BaseModule.properties.getBoolean(PROP_TOOLTIPS_ENABLED)

	private val popupMenuHandler: EventHandler<PopupMenuEvent> = { clear(it.canvas.view) }

	init {
		eventBus.register(PopupMenuEvent::class, popupMenuHandler)
	}

	fun dispose() {
		eventBus.unregister(popupMenuHandler)
	}

	/**
	 * Handles mouse move events in the client of the tooltip system and requests tooltip displaying
	 * or hiding as appropriate. Called by the client of the tooltip system in its event handling methods
	 * if none of its [Drawable]s is interested in handling a mouse moved event.
	 */
	fun handle(view: View<*>, container: DrawableContainer<*>, x: Double, y: Double) {
		val drawable = drawableRetriever.invoke(container, x, y)

		if (drawable == null) {
			if (lastTooltipDrawable != null) {
				eventBus.post(TooltipEvent(null, view, null, null))
				clearLastTargets()
			}
			return
		}

		val tooltip = getTooltip(drawable, x, y)
		val explanation = getExplanation(drawable, x, y)

		if (StringUtils.isBlank(tooltip?.text) && explanation == null) {
			if (lastTooltipDrawable != null || lastExplanation != null) {
				clearLastTargets()
				eventBus.post(TooltipEvent(null, view, null, null))
			}
			return
		}

		if (drawable !== lastTooltipDrawable
			|| tooltip?.text != lastTooltipText
			|| explanation?.explanation !== lastExplanation?.explanation
			|| lastTooltipSourceRect != tooltip?.sourceRect
		) {
			setLastTargets(drawable, tooltip?.text, explanation, tooltip?.sourceRect)
			eventBus.post(TooltipEvent(drawable, view, tooltip, explanation))
		}
	}

	private fun getTooltip(drawable: Drawable, x: Double, y: Double): Tooltip? =
		if (tooltipsEnabled) {
			tooltipAccessor(drawable, x, y)
		} else null

	private fun getExplanation(drawable: Drawable, x: Double, y: Double): DrawableExplanation<RectangularDrawable>? =
		if (tooltipsEnabled) {
			explanationAccessor(drawable, x, y)
		} else null

	fun clear(view: View<*>) {
		eventBus.post(TooltipEvent(null, view, null, null))
	}

	/** Clears [lastTooltipText] and [lastExplanation], if available.*/
	private fun clearLastTargets() {
		lastTooltipDrawable = null
		lastTooltipText = null
		lastExplanation = null
		lastTooltipSourceRect = null
	}

	private fun setLastTargets(
		drawable: Drawable,
		tooltipText: String?,
	    explanation: DrawableExplanation<RectangularDrawable>?,
		sourceRect: RectangularShape?
	) {
		lastTooltipDrawable = drawable
		lastTooltipText = tooltipText
		lastExplanation = explanation
		lastTooltipSourceRect = sourceRect
	}
}

/**
 * Captures the request for displaying a tooltip until it is scheduled by the [Timer] for display.
 * At least one of [tooltip] and [explanation] must be set.
 */
private data class TooltipRequest(
	val origin: Drawable,
	val view: View<*>,
	val tooltip: Tooltip?,
	val explanation: DrawableExplanation<RectangularDrawable>?
)

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

	private val LOG by logger(TooltipManager::class)

	/** The name of the [Int] property in [Properties] designating the time (in ms) of delaying the displaying of the tooltip.*/
	const val PROP_DELAY = "draw.view.TooltipManager.delay"

	private const val WIDTH = 300

	private const val MIN_WIDTH = 50

	/** The vertical distance between the bottom edge of the origin's bounding box and the tip of the [ArrowBubble]. */
	private const val Y_DIST = 10

	private val tooltipEventHandler: EventHandler<TooltipEvent> = { handle(it) }

	private val preferencesChangeHandler: EventHandler<PreferencesChangedEvent> = { initTimer() }

	/** The currently displayed text [TooltipView]. */
	private var textTooltip: TooltipView? = null

	/** The currently displayed explanation [TooltipView]. */
	private var explanationTooltip: TooltipView? = null

	/** Stores the last request to display a textTooltip until scheduled by the [timer] for displaying. */
	private var request: TooltipRequest? = null

	/** Used to delay the displaying of a requested textTooltip. */
	private var _timer: Timer? = null

	private val timer: Timer
		get() {
			if (_timer == null) {
				initTimer()
			}
			return _timer!!
		}

	/** Listens for [ZoomPan] changes in [View] for which a [TooltipView] is displayed in order to dispose it.*/
	private var zoomPanListener: PropertyChangeListener<Any>? = null

	var eventBus: EventBus = BaseModule.eventBus
		set(value) {
			if (field != value) {
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
		_timer = System.createTimer()
		timer.initialize(BaseModule.properties.getInt(PROP_DELAY), repeats = false) { displayImpl() }
	}

	private fun handle(event: TooltipEvent) {
		if (event.origin == null || event.tooltip == null) {
			tooltipDismissed()
		} else {
			tooltipRequested(event)
		}
	}

	private fun tooltipRequested(event: TooltipEvent) {
		if (textTooltip != null || explanationTooltip != null) {
			disposeTooltip()
		}
		request = TooltipRequest(event.origin!!, event.view, event.tooltip, event.explanation)
		timer.start()
	}

	private fun tooltipDismissed() {
		if (textTooltip != null || explanationTooltip != null) {
			LOG.trace("tooltipDismissed()")
			disposeTooltip()
		}
		if (timer.isRunning()) {
			timer.stop()
		}
	}

	/** Called by the [timer] in order to effectively display the textTooltip, if still needed. */
	private fun displayImpl() {
		timer.stop()
		request?.let {
			disposeTooltip()
			if (it.tooltip != null) {
				textTooltip = TooltipView(createTextArrowBubble(it.tooltip, it.view), it.view)
				it.view.overlayContainer.add(textTooltip!!.arrowBubble)
			}
			if (it.explanation != null) {
				explanationTooltip = TooltipView(createExplanationArrowBubble(it.explanation, it.view), it.view)

				// Don't show both TooltipViews above each other
				if (textTooltip == null || textTooltip!!.arrowBubble.position.belowLocation != explanationTooltip!!.arrowBubble.position.belowLocation) {
					it.view.overlayContainer.add(explanationTooltip!!.arrowBubble)
				}
			}
			it.view.overlayContainer.validate()
			zoomPanListener = it.view.addPropertyChangeListener { event->
				if (event.source == textTooltip?.view) {
					disposeTooltip()
				}
			}
			request = null
		}
	}

	private fun disposeTooltip() {
		textTooltip?.let {
			it.view.overlayContainer.remove(it.arrowBubble)
			it.view.overlayContainer.validate()
			zoomPanListener?.let { listener -> it.view.removePropertyChangeListener(listener) }
			textTooltip = null
		}
		explanationTooltip?.let {
			it.view.overlayContainer.remove(it.arrowBubble)
			it.view.overlayContainer.validate()
			zoomPanListener?.let { listener -> it.view.removePropertyChangeListener(listener) }
			explanationTooltip = null
		}
		textTooltip = null
		explanationTooltip = null
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