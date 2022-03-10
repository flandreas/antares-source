package ch.scorpion.antares.view.expression

import ch.scorpion.antares.model.expression.BooleanExpressionCommand
import ch.scorpion.antares.model.expression.BooleanExpressionLibraryItem
import ch.scorpion.antares.model.expression.BooleanExpressionReference
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.CloseViewRequest
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.graph.ui.desktop.AbstractGraphDesktopItemPanelSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopItemHeaderPanelSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemCloseRequest
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*

class BooleanExpressionDesktopItemSwing(
	item: BooleanExpressionLibraryItem,
	private val commandManager: CommandManager,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphDesktopItemPanelSwing() {

	private val ref = BooleanExpressionReference(item)

	private val headerPanel = GraphDesktopItemHeaderPanelSwing(
		this,
		JLabel("${Translations.getString("library.element.booleanExpression.name")} \"${item.name.getTranslation()}\""),
		allowClose = true)

	private val closeViewRequestHandler: EventHandler<CloseViewRequest> = { handle(it) }

	private val expressionsTextArea = JTextArea()

	private val applyAction = ApplyAction()

	init {
		expressionsTextArea.text = item.expressions.expressions

		eventBus.register(CloseViewRequest::class, closeViewRequestHandler)
		buildUI()

		ref.addListener {
			expressionsTextArea.text = ref.expressions.expressions
		}
	}

	private fun buildUI() {
		border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
		layout = BorderLayout(10, 10)

		add(headerPanel, BorderLayout.NORTH)
		add(createContentsPanel(), BorderLayout.CENTER)
	}

	private fun createContentsPanel(): JComponent {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)

		val tipLabel = JLabel(Translations.getString("library.element.booleanExpression.tip"))
		tipLabel.border = BorderFactory.createEmptyBorder(5, 2, 5, 0)
		panel.add(tipLabel)

		expressionsTextArea.columns = 60
		expressionsTextArea.rows = 6
		expressionsTextArea.maximumSize = expressionsTextArea.preferredSize

		val scrollPane = JScrollPane(expressionsTextArea)
		scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
		scrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
		scrollPane.alignmentX = Component.LEFT_ALIGNMENT
		scrollPane.maximumSize = scrollPane.preferredSize
		panel.add(scrollPane)

		val applyButton = JButton(ActionWrapperSwing(applyAction))
		applyButton.alignmentX = Component.LEFT_ALIGNMENT
		panel.add(Box.createVerticalStrut(3))
		panel.add(applyButton)

		return panel
	}

	/** ---- [GraphDesktopViewItem] */

	override fun addContextColorBorder(color: Color) { }

	override fun removeContextColorBorder() { }

	override val drawingView: DrawingView<GraphView>? get() = null

	override fun disposeItem() {
		eventBus.unregister(closeViewRequestHandler)
		ref.dispose()
	}

	override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? = null

	override fun createCloseRequest(): Any = CloseViewRequest(this)

	/** ---- [BooleanExpressionDesktopItemSwing] */

	private fun apply() {
		commandManager.execute(BooleanExpressionCommand(ref, expressionsTextArea.text))
	}

	private fun handle(request: CloseViewRequest) {
		if (request.view === this) {
			eventBus.postTwoPhase(
				prepareEvent = GraphDesktopViewItemCloseRequest(this, isRoot = true),
				execEvent = GraphDesktopViewItemCloseRequest(this, isRoot = true)
			)
		}
	}

	private inner class ApplyAction : AbstractAction("base.action.apply") {
		override fun execute(event: ActionEvent) {
			apply()
		}
	}
}