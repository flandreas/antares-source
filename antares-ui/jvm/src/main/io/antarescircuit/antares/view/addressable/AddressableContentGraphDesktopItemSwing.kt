package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.draw.CloseViewRequest
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewContent
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink
import io.antarescircuit.jabbah.graph.ui.desktop.*
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.event.FocusEvent
import java.awt.event.FocusListener

/** Wraps a [AddressableContentsPanel] as a [GraphDesktopViewItem] so it can be added to the [GraphDesktopView]. */
class AddressableContentGraphDesktopItemSwing(
	drawingView: DrawingView<GraphElementView<*>, GraphView>,
	link: ObjectLink<Addressable>,
	title: String,
	applicationContextHolder: GraphApplicationContextHolder,
	cmdManager: CommandManager = EditModule.commandManager,
	contextColor: CompositeColor,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphDesktopViewItemSwing(reusable = false) {

	private val memoryContentPanel = AddressableContentsPanel(drawingView, applicationContextHolder, link, cmdManager)

	private val headerLabel = UIBasics.createHeaderLabel(title)

	private val headerPanel = GraphDesktopItemHeaderPanelSwing(this, headerLabel, { headerLabel.text },  allowClose = true)

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

	override val drawingView: DrawingView<GraphElementView<*>, GraphView>? get() = null

	override fun displays(content: Any?): Boolean = content === memoryContentPanel.addressableRef.addressable

	override fun disposeItem() {
		memoryContentPanel.dispose()
		eventBus.unregister(closeViewRequestHandler)
	}

	override fun findContent(condition: (DrawingViewContent<GraphElementView<*>, GraphView>) -> Boolean): DrawingViewContent<*,*>? = null

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