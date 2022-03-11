package ch.scorpion.antares.view.expression

import ch.scorpion.antares.model.expression.*
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.truthtable.TruthTableService
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
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
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class BooleanExpressionDesktopItemSwing(
	item: BooleanExpressionLibraryItem,
	private val commandManager: CommandManager,
	private val expressionService: BooleanExpressionService = AntaresModelModule.booleanExpressionService,
	private val truthTableService: TruthTableService = AntaresModelModule.truthTableService,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphDesktopItemPanelSwing() {

	companion object {
		private val LOG by logger(BooleanExpressionDesktopItemSwing::class)
	}

	private val ref = BooleanExpressionReference(item)

	private val headerPanel = GraphDesktopItemHeaderPanelSwing(
		this,
		JLabel("${Translations.getString("library.element.booleanExpression.name")} \"${item.name.getTranslation()}\""),
		allowClose = true)

	private val closeViewRequestHandler: EventHandler<CloseViewRequest> = { handle(it) }

	private val expressionsTextArea = JTextArea()

	private val minimizedTextArea = JTextArea()

	private val applyAction = ApplyAction()

	private val statusLabel = JLabel()

	private val minimizedLabel = JLabel(Translations.getString("library.booleanExpression.minimized"))

	init {
		expressionsTextArea.text = item.expressions.expressions

		eventBus.register(CloseViewRequest::class, closeViewRequestHandler)
		buildUI()

		ref.addListener {
			expressionsTextArea.text = ref.expressions.expressions
		}

		disableApplyActionForNoExpressionChanges()
	}

	private fun disableApplyActionForNoExpressionChanges() {
		applyAction.enabled = false
		expressionsTextArea.document.addDocumentListener(object : DocumentListener {
			override fun insertUpdate(e: DocumentEvent?) { update() }
			override fun removeUpdate(e: DocumentEvent?) { update() }
			override fun changedUpdate(e: DocumentEvent?) { update() }

			private fun update() {
				applyAction.enabled = true
			}
		})
	}

	private fun buildUI() {
		border = BorderFactory.createEmptyBorder(0, 5, 0, 5)
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

		val expressionsScrollPane = JScrollPane(expressionsTextArea)
		expressionsScrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
		expressionsScrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
		expressionsScrollPane.alignmentX = Component.LEFT_ALIGNMENT
		expressionsScrollPane.maximumSize = expressionsScrollPane.preferredSize
		panel.add(expressionsScrollPane)

		val statusPanel = JPanel()
		statusPanel.layout = BoxLayout(statusPanel, BoxLayout.LINE_AXIS)
		statusPanel.alignmentX = Component.LEFT_ALIGNMENT

		val applyButton = JButton(ActionWrapperSwing(applyAction))
		applyButton.alignmentX = Component.LEFT_ALIGNMENT
		statusPanel.add(applyButton)

		statusLabel.alignmentX = Component.LEFT_ALIGNMENT
		statusLabel.text = "This is the status"
		statusLabel.foreground = java.awt.Color.RED
		statusPanel.add(Box.createHorizontalStrut(10))
		statusPanel.add(statusLabel)
		statusPanel.maximumSize = statusPanel.preferredSize

		panel.add(Box.createVerticalStrut(3))
		panel.add(statusPanel)

		minimizedLabel.alignmentX = Component.LEFT_ALIGNMENT
		minimizedLabel.border = BorderFactory.createEmptyBorder(25, 2, 5, 0)
		panel.add(minimizedLabel)

		minimizedTextArea.isEditable = false
		minimizedTextArea.columns = 60
		minimizedTextArea.rows = 6
		minimizedTextArea.maximumSize = minimizedTextArea.preferredSize

		val minimizedScrollPane = JScrollPane(minimizedTextArea)
		minimizedScrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
		minimizedScrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
		minimizedScrollPane.alignmentX = Component.LEFT_ALIGNMENT
		minimizedScrollPane.maximumSize = expressionsScrollPane.preferredSize
		panel.add(minimizedScrollPane)

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
		applyChanges()
		generateMinimizedExpressions()
	}

	private fun applyChanges() {
		commandManager.execute(BooleanExpressionCommand(ref, expressionsTextArea.text))
		applyAction.enabled = false
	}

	private fun generateMinimizedExpressions() {
		try {
			val result = expressionService.parseExpressions(expressionsTextArea.text, BooleanExpressionNotation.fromProperties())
			val truthTable = expressionService.createTruthTable(result)
			minimizedTextArea.text = truthTableService.generateExpressions(truthTable, BooleanExpressionNotation.fromProperties())
		} catch (e: DslError) {
			// TODO Handle
			LOG.debug("generate minimized expressions: ${e.message}")
		}
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