package ch.scorpion.antares.view.addressable

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.model.vertice.VerticeLink
import ch.scorpion.jabbah.graph.ui.desktop.*
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.event.FocusEvent
import java.awt.event.FocusListener

/** Wraps a [AddressableContentsPanel] as a [GraphDesktopViewItem] so it can be added to the [GraphDesktopView]. */
class AddressableContentGraphDesktopItemSwing(
	drawingView: DrawingView<GraphView>,
	link: VerticeLink,
	title: String,
	applicationContextHolder: GraphApplicationContextHolder,
	cmdManager: CommandManager = EditModule.commandManager,
	contextColor: CompositeColor,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphDesktopItemPanelSwing() {

	private val memoryContentPanel = AddressableContentsPanel(drawingView, applicationContextHolder, link, cmdManager)

	private val headerPanel = GraphDesktopItemHeaderPanelSwing(this, UIBasics.createHeaderLabel(title), allowClose = true)

	private val closeViewRequestHandler: EventHandler<CloseViewRequest> = { handle(it) }

	init {
		buildUI(contextColor)
		setupViewActionFocusListener()
		eventBus.register(CloseViewRequest::class, closeViewRequestHandler)
	}


	private fun buildUI(contextColor: CompositeColor) {
		layout = BorderLayout()
		add(headerPanel, BorderLayout.NORTH)
		add(memoryContentPanel, BorderLayout.CENTER)
		super.contextColor = contextColor
	}

	private fun setupViewActionFocusListener() {
		val focusListener = object : FocusListener {
			override fun focusGained(e: FocusEvent?) {
				DrawViewModule.viewManager.activeView = this@AddressableContentGraphDesktopItemSwing
			}

			override fun focusLost(e: FocusEvent?) { }
		}
		memoryContentPanel.addViewActivationFocusListener(focusListener)
	}

	/** ---- [GraphDesktopViewItem] */

	override val drawingView: DrawingView<GraphView>? get() = null

	override fun disposeItem() {
		memoryContentPanel.dispose()
		eventBus.unregister(closeViewRequestHandler)
	}

	override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? = null

	override fun addContextColorBorder(color: Color) {
		memoryContentPanel.border = createContextColorBorder(color)
	}

	override fun removeContextColorBorder() {
		memoryContentPanel.border = null
	}

	override fun createCloseRequest(): Any = GraphDesktopViewItemCloseRequest(this, false)

	private fun handle(request: CloseViewRequest) {
		if (request.view === this) {
			eventBus.postTwoPhase(
				prepareEvent = createCloseRequest(),
				execEvent = createCloseRequest()
			)
		}
	}
}