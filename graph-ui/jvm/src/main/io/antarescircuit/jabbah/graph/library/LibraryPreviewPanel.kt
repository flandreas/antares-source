package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.PreferencesChangedEvent
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.help.HelpIdProvider
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.base.ui.HelpAction
import io.antarescircuit.jabbah.draw.drawable.DefaultDrawableDrawer
import io.antarescircuit.jabbah.draw.drawable.DrawableDrawer
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.draw.ui.MultilineTextDisplayJvm
import io.antarescircuit.jabbah.draw.view.buildToolTipText
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.model.text.EditModelTextModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.style.EditStyleType
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.ui.library.LibrarySelectionChangedEvent
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*
import kotlin.math.min


/**
 * A [JPanel] that provides a preview of the [LibraryElement] that is currently selected in a [LibraryTreeViewSwing].
 */
class LibraryPreviewPanel(
	private val eventBus: EventBus,
	private val controller: LibraryTreeViewController
) : JPanel() {

	companion object {
		private val LOG by logger(LibraryPreviewPanel::class)
		private val BACKGROUND_COLOR = UIManager.getColor("Table.background")
	}

	@Suppress("unused")
	constructor(controller: LibraryTreeViewController) : this(BaseModule.eventBus, controller)

	/** Maps a [LibraryElement] to the instantiated [Component] to be displayed as preview.*/
	private val map: MutableMap<LibraryElement, Component> = mutableMapOf()

	private val helpAction = HelpAction.withSmallImage { (controller.selectedItem as HelpIdProvider).helpId }

	private val componentDisplay = ComponentDisplay()

	private val descriptionDisplay = MultilineTextDisplayJvm()

	/** Stores the preview [Component] of the currently selected [LibraryElement]. */
	private var selection: Component? = null

	private val librarySelectionHandler: EventHandler<LibrarySelectionChangedEvent> = { handleLibrarySelectionChanged(it) }

	private val libraryItemUpdatedHandler: EventHandler<LibraryItemUpdatedEvent> = { map.remove(it.item) }

	private val preferencesChangedHandler: EventHandler<PreferencesChangedEvent> = { componentDisplay.repaint() }

	private val errorComponent = EditModelTextModule.textComponentFactory.create(TranslatableText(Translations.getString("base.error.txt")), styleType = EditStyleType.MESSAGE_ERROR)

	init {
		helpAction.enabled = false

		eventBus.register(LibrarySelectionChangedEvent::class, librarySelectionHandler)
		eventBus.register(LibraryItemUpdatedEvent::class, libraryItemUpdatedHandler)
		eventBus.register(PreferencesChangedEvent::class, preferencesChangedHandler)

		buildUI()

		addComponentListener(object : ComponentAdapter() {
			override fun componentResized(e: ComponentEvent?) {
				componentDisplay.updateLayout()
			}
		})
	}

	fun dispose() {
		eventBus.unregister(librarySelectionHandler)
		eventBus.unregister(libraryItemUpdatedHandler)
		eventBus.unregister(preferencesChangedHandler)
	}

	fun addDrawableDrawer(drawableDrawer: DrawableDrawer<Component>) {
		componentDisplay.addDrawableDrawer(drawableDrawer)
	}

	private fun buildUI() {
		background = BACKGROUND_COLOR
		layout = BorderLayout(10, 0)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		descriptionDisplay.background = BACKGROUND_COLOR
		add(componentDisplay, BorderLayout.WEST)
		add(descriptionDisplay, BorderLayout.CENTER)

		val buttonPanel = JToolBar()
		val helpButton = JButton(ActionWrapperSwing(helpAction))
		helpButton.text = null
		//helpButton.border = null
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(helpButton)
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.background = BACKGROUND_COLOR
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun handleLibrarySelectionChanged(e: LibrarySelectionChangedEvent) {
		if (e.controller !== controller) {
			return
		}
		updateWithSelectedItem()
	}

	private fun updateWithSelectedItem() {
		val selectedItem = controller.selectedItem
		if (selectedItem !is LibraryElement) {
			updateSelection(null)
		} else {
			try {
				updateSelection(selectedItem)
			} catch (e: Exception) {
				LOG.error("Exception when handling LibraryImpl selection change: ${e.message}")
			}
		}
		helpAction.enabled = selectedItem is HelpIdProvider && selectedItem.helpId != null
		repaint()
	}

	private fun updateSelection(libraryElement: LibraryElement?) {
		if (libraryElement == null) {
			selection = null
			descriptionDisplay.plainText = null
			return
		}

		val component = map[libraryElement]
		if (component != null) {
			updateSelectionImpl(component)
			return
		}

		InvocationHandler.invoke {
			try {
				val c = libraryElement.getNewInstance<GraphElement>()
				map[libraryElement] = c
				updateSelectionImpl(c)
			} catch (e: Throwable) {
				handleLoadError(e)
			}
		}
	}

	private fun handleLoadError(e: Throwable) {
		selection = errorComponent
		descriptionDisplay.plainText = e.message
	}

	private fun updateSelectionImpl(component: Component) {
		selection = component
		componentDisplay.updateLayout()

		descriptionDisplay.plainText = buildToolTipText(
			selection!!.type,
			selection!!.typeDesc,
			null,
			true
		)
	}

	/** Displays the graphical preview of the selected [Component]. */
	private inner class ComponentDisplay : JPanel() {

		private var drawableDrawer: DrawableDrawer<Component> = DefaultDrawableDrawer()
		private var scale: Double = 1.0
		private val appContext = GraphApplicationContext(CurrentSystemSpeedCategory(SystemSpeed()))

		init {
			background = BACKGROUND_COLOR
			preferredSize = Dimension(75, 150)
		}

		fun addDrawableDrawer(drawableDrawer: DrawableDrawer<Component>) {
			drawableDrawer.successor = this.drawableDrawer
			this.drawableDrawer = drawableDrawer
		}

		override fun paintComponent(g: Graphics) {
			val g2 = g as Graphics2D
			super.paintComponent(g)

			g2.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON)

			if (scale != 1.0) {
				g2.scale(scale, scale)
			}

			if (selection != null) {
				drawableDrawer.process(DrawModule.drawContextFactory(Graphics2DJvm(g2), null, appContext), selection!!)
			}

			if (scale != 1.0) {
				g2.scale(1 / scale, 1 / scale)
			}
		}

		fun updateLayout() {
			if (selection == null) {
				return
			}

			selection!!.location = Point2D(0, 0)
			val bbox = selection!!.boundingBox

			val fx = this.width / bbox.width
			val fy = this.height / bbox.height

			scale = 1.0
			if (fx < 1 || fy < 1) {
				scale = min(fx, fy)
			}

			// Horizontally centered
			val dx = (this.width.toDouble() - bbox.width * scale) / 2 - bbox.x
			// Vertically top aligned
			val dy = -bbox.y

			selection!!.moveBy(dx, dy)
			repaint()
		}
	}
}