package ch.scorpion.antares.view.truthtable

import ch.scorpion.antares.AntaresModuleJvm
import ch.scorpion.antares.model.expression.BooleanExpressionNotation
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.antares.model.truthtable.TruthTableReference
import ch.scorpion.antares.model.truthtable.TruthTableService
import ch.scorpion.antares.view.synthesis.CreateCircuitFromTruthTablePanel
import ch.scorpion.antares.view.synthesis.CreateCircuitFromTruthTableService
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
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
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItemCloseRequest
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import java.awt.Frame
import javax.swing.*
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
import javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
import kotlin.math.max

class TruthTableDesktopItemSwing(
	private val item: TruthTableLibraryItem,
	private val truthTableService: TruthTableService = AntaresModelModule.truthTableService,
	private val createCircuitService: CreateCircuitFromTruthTableService = AntaresModuleJvm.createCircuitFromTruthTableService,
	commandManager: CommandManager,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractGraphDesktopItemPanelSwing() {

	companion object {
		private val EXPRESSION_FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)
	}

	private val ref = TruthTableReference(item)

	private val headerPanel = GraphDesktopItemHeaderPanelSwing(
		this,
		JLabel("${Translations.getString("library.element.truthTable.name")} \"${item.truthTable.name.getTranslation()}\""),
		allowClose = true)

	private val closeViewRequestHandler: EventHandler<CloseViewRequest> = { handle(it) }

	private val tableView = TruthTableTableView(ref, commandManager)

	private val generateExpressionsAction = GenerateExpressionsAction()

	private val createCircuitAction = CreateCircuitAction()

	private val expressionsTextArea = JTextArea()

	init {
		eventBus.register(CloseViewRequest::class, closeViewRequestHandler)
		buildUI()

		createCircuitAction.enabled = false

		ref.addDataListener {
			expressionsTextArea.text = ""
			createCircuitAction.enabled = false
		}
	}

	private fun buildUI() {
		border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
		layout = BorderLayout(10, 10)

		add(headerPanel, BorderLayout.NORTH)
		add(createContentsPanel(), BorderLayout.CENTER)
		add(createExpressionsPanel(), BorderLayout.SOUTH)
	}

	private fun createContentsPanel(): JComponent {
		expressionsTextArea.font = EXPRESSION_FONT

		val panel = JPanel(BorderLayout())
		val tipLabel = JLabel(Translations.getString("library.element.truthTable.tip"))
		tipLabel.border = BorderFactory.createEmptyBorder(5, 2, 5, 0)
		panel.add(tipLabel, BorderLayout.NORTH)
		panel.add(tableView, BorderLayout.CENTER)

		return panel
	}

	private fun createExpressionsPanel(): JComponent {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)

		expressionsTextArea.alignmentX = Component.LEFT_ALIGNMENT
		expressionsTextArea.isEditable = false
		expressionsTextArea.rows = max(4, ref.truthTable.outputColumnCount)

		val expressionScrollPane = JScrollPane(expressionsTextArea)
		expressionScrollPane.alignmentX = Component.LEFT_ALIGNMENT
		expressionScrollPane.horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_AS_NEEDED
		expressionScrollPane.verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED

		val expressionsButton = JButton(ActionWrapperSwing(generateExpressionsAction))
		expressionsButton.alignmentX = Component.LEFT_ALIGNMENT

		val createCircuitButton = JButton(ActionWrapperSwing(createCircuitAction))
		createCircuitButton.alignmentX = Component.LEFT_ALIGNMENT

		panel.add(expressionsButton)
		panel.add(Box.createVerticalStrut(5))
		panel.add(expressionScrollPane)
		panel.add(Box.createVerticalStrut(5))
		panel.add(createCircuitButton)

		return panel
	}

	override fun addContextColorBorder(color: Color) { }

	override fun removeContextColorBorder() { }

	override val drawingView: DrawingView<GraphView>? get() = null

	override fun disposeItem() {
		eventBus.unregister(closeViewRequestHandler)
		ref.dispose()
	}

	override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? = null

	override fun createCloseRequest(): Any = CloseViewRequest(this)

	/** ---- [TruthTableDesktopItemSwing] */

	private fun handle(request: CloseViewRequest) {
		if (request.view === this) {
			eventBus.postTwoPhase(
				prepareEvent = GraphDesktopViewItemCloseRequest(this, isRoot = true),
				execEvent = GraphDesktopViewItemCloseRequest(this, isRoot = true)
			)
		}
	}

	private fun generateExpressions() {
		expressionsTextArea.text = truthTableService.generateExpressions(
			ref.truthTable,
			BooleanExpressionNotation.fromProperties())
		createCircuitAction.enabled = true
	}

	private fun createCircuit() {
		CreateCircuitFromTruthTablePanel.showAsDialog(
			Frame.getFrames()[0],
			item.truthTable,
			item,
			createCircuitService)
	}

	private inner class GenerateExpressionsAction
		: ch.scorpion.jabbah.base.AbstractAction("antares.action.truthTable.expressions")
	{
		override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
			generateExpressions()
		}
	}

	private inner class CreateCircuitAction
		: ch.scorpion.jabbah.base.AbstractAction("antares.synthesis.createCircuitFromTruthTable.action")
	{
		override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
			createCircuit()
		}
	}
}