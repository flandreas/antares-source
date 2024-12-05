package ch.scorpion.antares.view.truthtable

import ch.scorpion.antares.AntaresModuleJvm
import ch.scorpion.antares.model.expression.BooleanExpressionNotation
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.antares.model.truthtable.TruthTableReference
import ch.scorpion.antares.model.truthtable.TruthTableService
import ch.scorpion.antares.view.synthesis.CreateCircuitFromTruthTablePanel
import ch.scorpion.antares.view.synthesis.CreateCircuitFromTruthTableService
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.HelpAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.graph.AbstractTitledGraphDesktopViewItemSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import java.awt.BorderLayout
import java.awt.Component
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
	createTitleText(item.truthTable),
	JPanel(),
	applicationDataHolder,
	eventBus,
	listOf(HelpAction.withSmallImage(HELP_ID))
) {

	companion object {
		val HELP_ID = HelpId("truthTableDesktopItem")

		private val EXPRESSION_FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)

		fun createTitleText(truthTable: TruthTable): String =
			"${Translations.getString("library.element.truthTable.name")} \"${truthTable.name.getTranslation()}\""
	}

	private val ref = TruthTableReference(item)

	private val tableView = TruthTableTableView(ref, commandManager)

	private val generateExpressionsAction = GenerateExpressionsAction()

	private val createCircuitAction = CreateCircuitAction()

	private val expressionsTextArea = JTextArea()

	private val expressionsButton = JButton(ActionWrapperSwing(generateExpressionsAction))

	private val createCircuitButton = JButton(ActionWrapperSwing(createCircuitAction))

	private val truthTable: TruthTable get() = applicationDataHolder.data!!.content as TruthTable

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

		expressionsTextArea.alignmentX = Component.LEFT_ALIGNMENT
		expressionsTextArea.isEditable = false
		expressionsTextArea.rows = max(4, ref.truthTable.outputColumnCount)

		val expressionScrollPane = JScrollPane(expressionsTextArea)
		expressionScrollPane.alignmentX = Component.LEFT_ALIGNMENT
		expressionScrollPane.horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_AS_NEEDED
		expressionScrollPane.verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED

		expressionsButton.alignmentX = Component.LEFT_ALIGNMENT

		createCircuitButton.alignmentX = Component.LEFT_ALIGNMENT

		panel.add(expressionsButton)
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

	override fun displays(content: Any?): Boolean = content === item

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