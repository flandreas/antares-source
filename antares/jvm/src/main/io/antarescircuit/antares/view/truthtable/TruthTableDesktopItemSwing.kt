package io.antarescircuit.antares.view.truthtable

import io.antarescircuit.antares.AntaresModuleJvm
import io.antarescircuit.antares.model.expression.BooleanExpressionNotation
import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.model.truthtable.TruthTableLibraryItem
import io.antarescircuit.antares.model.truthtable.TruthTableReference
import io.antarescircuit.antares.model.truthtable.TruthTableService
import io.antarescircuit.antares.view.synthesis.CreateCircuitFromTruthTablePanel
import io.antarescircuit.antares.view.synthesis.CreateCircuitFromTruthTableService
import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.HelpAction
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.draw.view.DrawViewModule
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.graph.AbstractTitledGraphDesktopViewItemSwing
import io.antarescircuit.jabbah.graph.ui.desktop.GraphDesktopViewItem
import java.awt.BorderLayout
import java.awt.Font
import java.awt.Frame
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.*
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
import javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
import kotlin.math.max

class TruthTableDesktopItemSwing(
	val item: TruthTableLibraryItem,
	private val applicationDataHolder: ApplicationDataHolder,
	private val truthTableService: TruthTableService = AntaresModelModule.truthTableService,
	private val createCircuitService: CreateCircuitFromTruthTableService = AntaresModuleJvm.createCircuitFromTruthTableService,
	commandManager: CommandManager,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTitledGraphDesktopViewItemSwing(
	createTitleText(item.storable),
	JPanel(),
	applicationDataHolder,
	eventBus,
	actions = listOf(HelpAction.withSmallImage(HELP_ID))
) {

	companion object {
		val HELP_ID = HelpId("truthTableDesktopItem")

		private val EXPRESSION_FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)

		fun createTitleText(truthTable: TruthTable): String =
			"${Translations.getString("library.element.truthTable.name")} \"${truthTable.name.getTranslation()}\""
	}

	private val ref = TruthTableReference({ applicationDataHolder.data!!.content as TruthTable })

	private val tableView = TruthTableTableView(ref, commandManager)

	private val generateExpressionsAction = GenerateExpressionsAction()

	private val importCSVAction = ImportCSVAction()

	private val createCircuitAction = CreateCircuitAction()

	private val expressionsTextArea = JTextArea()

	private val expressionsButton = JButton(ActionWrapperSwing(generateExpressionsAction))

	private val importCSVButton = JButton(ActionWrapperSwing(importCSVAction))

	private val createCircuitButton = JButton(ActionWrapperSwing(createCircuitAction))

	private val truthTable: TruthTable get() = (applicationDataHolder.data!!.content as TruthTableLibraryItem).storable

	init {
		buildUI()

		createCircuitAction.enabled = false

		ref.addDataListener {
			expressionsTextArea.text = ""
			createCircuitAction.enabled = false
		}

		setupViewActivationFocusListener()
	}

	override fun createHeaderText(): String = createTitleText(truthTable)

	private fun setupViewActivationFocusListener() {
		val focusListener = object : FocusListener {
			override fun focusGained(e: FocusEvent?) {
				DrawViewModule.viewManager.activeView = this@TruthTableDesktopItemSwing
			}

			override fun focusLost(e: FocusEvent?) { }
		}
		tableView.table.addFocusListener(focusListener)
		expressionsTextArea.addFocusListener(focusListener)
		expressionsButton.addFocusListener(focusListener)
		createCircuitButton.addFocusListener(focusListener)
	}

	private fun buildUI() {
		with(contentPanel) {
			border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
			layout = BorderLayout(10, 10)

			add(createTablePanel(), BorderLayout.CENTER)
			add(createExpressionsPanel(), BorderLayout.SOUTH)
		}
	}

	private fun createTablePanel(): JComponent {
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

		expressionsTextArea.alignmentX = LEFT_ALIGNMENT
		expressionsTextArea.isEditable = false
		expressionsTextArea.rows = max(8, ref.truthTable.outputColumnCount)

		val expressionScrollPane = JScrollPane(expressionsTextArea)
		expressionScrollPane.alignmentX = LEFT_ALIGNMENT
		expressionScrollPane.horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_AS_NEEDED
		expressionScrollPane.verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED

		val expressionsButtonPanel = JPanel()
		expressionsButtonPanel.layout = BoxLayout(expressionsButtonPanel, BoxLayout.LINE_AXIS)
		expressionsButtonPanel.alignmentX = LEFT_ALIGNMENT
		expressionsButtonPanel.add(importCSVButton)
		expressionsButtonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		expressionsButtonPanel.add(expressionsButton)
		expressionsButtonPanel.add(Box.createHorizontalGlue())

		createCircuitButton.alignmentX = LEFT_ALIGNMENT

		panel.add(expressionsButtonPanel)
		panel.add(Box.createVerticalStrut(5))
		panel.add(expressionScrollPane)
		panel.add(Box.createVerticalStrut(5))
		panel.add(createCircuitButton)

		return panel
	}

	/** ---- [GraphDesktopViewItem] */

	override fun disposeItem() {
		super.disposeItem()
		ref.dispose()
	}

	override fun displays(content: Any?): Boolean =
		applicationDataHolder.data?.content is TruthTable && content === ref.truthTable

	/** ---- [TruthTableDesktopItemSwing] */

	private fun generateExpressions() {
		expressionsTextArea.text = truthTableService.generateExpressions(
			ref.truthTable,
			BooleanExpressionNotation.fromProperties())
		createCircuitAction.enabled = true
	}

	private fun createCircuit() {
		CreateCircuitFromTruthTablePanel.showAsDialog(
			Frame.getFrames()[0],
			ref.truthTable,
			item,
			createCircuitService)
	}

	private fun importCSV() {
		ImportCSVPanel.showAsDialog(Frame.getFrames()[0], ref)
	}

	private inner class GenerateExpressionsAction : AbstractAction("antares.action.truthTable.expressions") {
		override fun execute(event: ActionEvent) {
			generateExpressions()
		}
	}

	private inner class CreateCircuitAction
		: AbstractAction("antares.synthesis.createCircuitFromTruthTable.action", opensDialog = true)
	{
		override fun execute(event: ActionEvent) {
			createCircuit()
		}
	}

	private inner class ImportCSVAction : AbstractAction("antares.truthTable.csv.import", opensDialog = true) {
		override fun execute(event: ActionEvent) {
			importCSV()
		}
	}
}