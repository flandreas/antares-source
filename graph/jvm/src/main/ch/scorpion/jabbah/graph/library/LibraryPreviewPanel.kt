package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.help.HelpIdProvider
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.draw.drawable.DefaultDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.ui.MultilineTextDisplayJvm
import ch.scorpion.jabbah.draw.view.buildToolTipText
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.ui.library.LibrarySelectionChangedEvent
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.io.FileNotFoundException
import javax.swing.*
import kotlin.math.min


/**
 * A [JPanel] that provides a preview of the [LibraryElement] that is currently selected in a [LibraryTreeViewSwing].
 */
class LibraryPreviewPanel(
	eventBus: EventBus,
	private val controller: LibraryTreeViewController
) : JPanel() {

	companion object {
		private val LOG by logger(LibraryPreviewPanel::class)
		private val BACKGROUND_COLOR = UIManager.getColor("Table.background")
	}

	init {
		eventBus.register(CurrentSavableEvent::class) {
			SwingUtilities.invokeLater {
				updateWithSelectedItem()
			}
		}
	}

	@Suppress("unused")
	constructor(controller: LibraryTreeViewController) : this(BaseModule.eventBus, controller)

	/** Maps a [LibraryElement] to the instantiated [Component] to be displayed as preview.*/
	private val map: MutableMap<LibraryElement, Component> = mutableMapOf()

	private val helpAction = HelpAction()

	private val componentDisplay = ComponentDisplay()

	private val descriptionDisplay = MultilineTextDisplayJvm()

	/** Stores the preview [Component] of the currently selected [LibraryElement]. */
	private var selection: Component? = null

	init {
		eventBus.register(LibrarySelectionChangedEvent::class) { handleLibrarySelectionChanged(it) }
		eventBus.register(LibraryItemUpdatedEvent::class) { map.remove(it.item) }
		eventBus.register(PreferencesChangedEvent::class) { componentDisplay.repaint() }

		buildUI()

		addComponentListener(object : ComponentAdapter() {
			override fun componentResized(e: ComponentEvent?) {
				componentDisplay.updateLayout()
			}
		})
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
			descriptionDisplay.styledText = null
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
		LOG.error("Error when loading preview", e)
		val msgKey = when(e) {
			is FileNotFoundException -> "graph.action.load.error.fileNotFound.desc"
			else -> "graph.action.load.error.general.desc"
		}
		JOptionPane.showConfirmDialog(
			JFrame.getFrames()[0],
			Translations.getString(msgKey),
			Translations.getString("graph.preview.name"),
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.ERROR_MESSAGE)
	}

	private fun updateSelectionImpl(component: Component) {
		selection = component
		componentDisplay.updateLayout()
		descriptionDisplay.styledText = buildToolTipText(selection!!.type, selection!!.typeDesc, null, true)
	}

	private inner class HelpAction : AbstractAction("base.action.help", imagePath = "/img/help.png") {
		override fun execute(event: ActionEvent) {
			BaseModule.helpProvider.provideHelpFor((controller.selectedItem as HelpIdProvider).helpId)
		}
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