package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.ArrowBubble
import ch.scorpion.jabbah.draw.drawable.MultilineText
import ch.scorpion.jabbah.draw.drawable.RectangularDrawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

fun buildToolTipText(title: String?, text: String?, subText: String?, endWithPeriod: Boolean = false): String {
	val sb = StringBuilder()

	val hasText = StringUtils.isNotEmpty(text)
	val hasSubText = StringUtils.isNotEmpty(subText)

	if (StringUtils.isNotBlank(title)) {
		sb.append(title)
		if (hasText) {
			sb.append(": ")
		}
	}

	if (hasText) {
		sb.append(text)
		if (endWithPeriod && !text!!.endsWith(".")) {
			sb.append('.')
		}
	}

	if (hasSubText) {
		if (sb.isNotEmpty()) {
			sb.appendLine().appendLine()
		}
		sb.append(subText)
		if (endWithPeriod && !subText!!.endsWith(".")) {
			sb.append('.')
		}
	}

	return sb.toString()
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

	private var lastTooltipLocation: Point2D? = null

	private val tooltipsEnabled = BaseModule.properties.getBoolean(PROP_TOOLTIPS_ENABLED)

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

		val tooltip = createTooltip(drawable, x, y)
		val explanation = explanationAccessor.invoke(drawable, x, y)
		if (StringUtils.isEmpty(tooltip?.text) && explanation == null) {
			if (lastTooltipDrawable != null || lastExplanation != null) {
				clearLastTargets()
				eventBus.post(TooltipEvent(null, view, null, null))
			}
			return
		}

		if (drawable !== lastTooltipDrawable || tooltip?.text != lastTooltipText || explanation?.explanation !== lastExplanation?.explanation || lastTooltipLocation != tooltip?.location) {
			setLastTargets(drawable, tooltip?.text, explanation, tooltip?.location)
			eventBus.post(TooltipEvent(drawable, view, tooltip, explanation))
		}
	}

	private fun createTooltip(drawable: Drawable, x: Double, y: Double): Tooltip? =
		if (tooltipsEnabled) tooltipAccessor.invoke(drawable, x, y) else null

	fun clear(view: View<*>) {
		eventBus.post(TooltipEvent(null, view, null, null))
	}

	/** Clears [lastTooltipText] and [lastExplanation], if available.*/
	private fun clearLastTargets() {
		lastExplanation?.explanation?.dispose()
		lastTooltipDrawable = null
		lastTooltipText = null
		lastExplanation = null
		lastTooltipLocation = null
	}

	private fun setLastTargets(drawable: Drawable, tooltipText: String?, explanation: DrawableExplanation<RectangularDrawable>?, location: Point2D?) {
		lastExplanation?.explanation?.dispose()
		lastTooltipDrawable = drawable
		lastTooltipText = tooltipText
		lastExplanation = explanation
		lastTooltipLocation = location
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
		timer.initialize(BaseModule.properties.getInt(PROP_DELAY)) { displayImpl() }
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
		LOG.trace("tooltipRequested for text '${event.tooltip!!.text}' and explanation ${event.explanation}")
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
		request?.let {
			disposeTooltip()
			if (it.tooltip != null) {
				textTooltip = TooltipView(createTextArrowBubble(it.tooltip, it.view), it.view)
				it.view.overlayContainer.add(textTooltip!!.arrowBubble)
			}
			if (it.explanation != null) {
				explanationTooltip = TooltipView(createExplanationArrowBubble(it.explanation, it.view), it.view)
				it.view.overlayContainer.add(explanationTooltip!!.arrowBubble)
			}
			it.view.overlayContainer.validate()
			zoomPanListener = it.view.addPropertyChangeListener { event->
				if (event.source == textTooltip?.view) {
					disposeTooltip()
				}
			}
			request = null
			timer.stop()
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
		val multilineText = MultilineText(text = tooltip.text, font = font, preferredWidth = WIDTH.toDouble(), minWidth = MIN_WIDTH.toDouble())

		return ArrowBubble(
			multilineText,
			view.modelToView(calculateTextBubbleLocation(tooltip.location)),
			true,
			StyleType.TOOLTIP,
			styleProvider
		)
	}

	private fun calculateTextBubbleLocation(location: Point2D): Point2D = Point2D(location.x, location.y + Y_DIST)

	private fun createExplanationArrowBubble(explanation: DrawableExplanation<RectangularDrawable>, view: View<*>): ArrowBubble {
		return ArrowBubble(
			explanation.explanation,
			view.modelToView(calculateExplanationBubbleLocation(explanation.location)),
			false,
			StyleType.TOOLTIP,
			styleProvider
		)
	}

	private fun calculateExplanationBubbleLocation(location: Point2D): Point2D =
		Point2D(location.x, location.y - Y_DIST)
}