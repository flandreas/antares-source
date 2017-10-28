package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.Timer
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.ZoomPan
import ch.scorpion.jabbah.draw.drawable.ArrowBubble
import ch.scorpion.jabbah.draw.drawable.MultilineText
import ch.scorpion.jabbah.draw.graphics.TextRenderInfoFactory
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * Represents a request to display or hide the tooltip of a [Drawable] in a particular [View].
 *
 * @property origin the [Drawable] for which the tooltip is to be displayed, or `null`if the tooltip
 * should be hidden
 * @property view the [View] in which [origin] is displayed
 * @property text the text to be displayed in the tooltip, or `null` if the tooltip should be hidden
 */
data class TooltipEvent(
        val origin: Drawable?,
        val view: View<*>,
        val text: String?
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
 */
class TooltipHandler(
        private val eventBus: EventBus,
        private val drawableRetriever: (DrawableContainer<*>, Double, Double) -> Drawable? = { c,x,y -> c.getDrawableAt(x, y) },
        private val tooltipAccessor: (Drawable, Double, Double) -> String? = { d,x,y -> d.getToolTipText(x, y, null) }
) {

    private var tooltipDrawable: Drawable? = null

    private var tooltipText: String? = null

    /**
     * Handles mouse moves events in the client of the tooltip system and requests tooltip displaying
     * or hiding as appropriate. Called by the client of the tooltip system in its event handling methods
     * if none of its [Drawable] is interested in handling a mouse moved event.
     */
    fun handle(view: View<*>, container: DrawableContainer<*>, x: Double, y: Double) {
        //val drawable = container.getDrawableAt(x, y)
        val drawable = drawableRetriever.invoke(container, x, y)

        if (drawable == null) {
            if (tooltipDrawable != null) {
                eventBus.post(TooltipEvent(null, view, null))
                tooltipDrawable = null
                tooltipText = null
            }
        } else {
            //val text = drawable.getToolTipText(x, y, null)
            val text = tooltipAccessor.invoke(drawable, x, y)
            if (StringUtils.isEmpty(text)) {
                if (tooltipDrawable != null) {
                    tooltipDrawable = null
                    tooltipText = null
                    eventBus.post(TooltipEvent(null, view, null))
                }
            } else {
                if (drawable !== tooltipDrawable || text != tooltipText) {
                    tooltipDrawable = drawable
                    tooltipText = text
                    eventBus.post(TooltipEvent(drawable, view, tooltipText))
                }
            }
        }
    }

    fun clear(view: View<*>) {
        eventBus.post(TooltipEvent(null, view, null))
    }
}

/** Captures the request for displaying a tooltip until it is scheduled by the [Timer] for display.*/
private data class TooltipRequest(
        val origin: Drawable,
        val view: View<*>,
        val text: String
)

/** Holds the view data of a currently displayed tooltip. */
private data class TooltipView(
        val arrowBubble: ArrowBubble,
        val view: View<*>
)

object TooltipManager {

    private val LOG by logger(TooltipManager::class)

    private val WIDTH = 300

    private val MIN_WIDTH = 100

    /** The vertical distance between the bottom edge of the origin's bounding box and the tip of the [ArrowBubble]. */
    private val Y_DIST = 10

    /** The time (in ms) of delaying the displaying of the tooltipView. */
    private val DELAY = 2000

    private val tooltipEventHandler: EventHandler<TooltipEvent> = { handle(it) }

    /** The currently displayed [TooltipView]. */
    private var tooltipView: TooltipView? = null

    /** Stores the last request to display a tooltipView until scheduled by the [timer] for displaying. */
    private var request: TooltipRequest? = null

    /** Used to delay the displaying of a requested tooltipView. */
    private val timer: Timer = System.get().createTimer()

    /** Listens for [ZoomPan] changes in [View] for which a [TooltipView] is displayed in order to dispose it.*/
    private val zoomPanListener = object : PropertyChangeListener<Any> {
        override fun propertyChanged(e: PropertyChangeEvent<Any>) {
            if (e.source == tooltipView?.view) {
                disposeTooltip()
            }
        }
    }

    var eventBus: EventBus = BaseModule.eventBus
    set(value) {
        if (field != value) {
            eventBus.unregister(TooltipEvent::class, tooltipEventHandler)
            field = value
            field.register(TooltipEvent::class, tooltipEventHandler)
        }
    }

    var styleProvider: StyleProvider = DrawStyleModule.styleProvider

    var textRenderInfoFactory: TextRenderInfoFactory = DrawModule.textRenderInfoFactory

    init {
        eventBus.register(TooltipEvent::class, tooltipEventHandler)
        timer.initialize(DELAY, { displayImpl() })
    }

    private fun handle(event: TooltipEvent) {
        if (event.origin == null || StringUtils.isEmpty(event.text)) {
            tooltipDismissed()
        } else {
            tooltipRequested(event)
        }
    }

    private fun tooltipRequested(event: TooltipEvent) {
        if (tooltipView != null) {
            tooltipDismissed()
        }
        LOG.debug("TooltipManager.tooltipRequested '${event.text}'")
        request = TooltipRequest(event.origin!!, event.view, event.text!!)
        timer.start()
    }

    private fun tooltipDismissed() {
        if (tooltipView != null) {
            LOG.debug("TooltipManager.tooltipDismissed()")
            disposeTooltip()
        }
        if (timer.isRunning()) {
            timer.stop()
        }
    }

    /** Called by the [timer] in order to effectively display the tooltipView, if still needed. */
    private fun displayImpl() {
        LOG.debug("TooltipManager.displayImpl")
        request?.let {
            disposeTooltip()
            tooltipView = TooltipView(createArrowBubble(it.origin, it.view, it.text), it.view)
            it.view.overlayContainer.add(tooltipView!!.arrowBubble)
            it.view.overlayContainer.validate()
            it.view.addPropertyChangeListener(zoomPanListener)
            request = null
        }
    }

    private fun disposeTooltip() {
        LOG.debug("TooltipManager.disposeTooltip")
        tooltipView?.let {
            it.view.overlayContainer.remove(it.arrowBubble)
            it.view.overlayContainer.validate()
            it.view.removePropertyChangeListener(zoomPanListener)
            tooltipView = null
        }
        tooltipView = null
    }

    private fun createArrowBubble(origin: Drawable, view: View<*>, text: String): ArrowBubble {
        val font = styleProvider.getStyle(StyleType.TOOLTIP).font
        val textRenderInfo = textRenderInfoFactory.measureHtmlText(text, font, WIDTH)
        val width = Math.max(MIN_WIDTH, textRenderInfo.textBounds.width.toInt()).toDouble()

        val multilineText = MultilineText(text = text, font = font, maxWidth = width, asHtml = true)
        multilineText.setBounds(0, 0, width.toInt(), textRenderInfo.textBounds.height.toInt())

        return ArrowBubble(
                multilineText,
                view.modelToView(calculateBubbleLocation(origin)),
                StyleType.TOOLTIP,
                styleProvider
        )
    }

    private fun calculateBubbleLocation(origin: Drawable): Point2D {
        val bounds = origin.boundingBox
        return Point2D(bounds.centerX, bounds.maxY + Y_DIST)
    }
}