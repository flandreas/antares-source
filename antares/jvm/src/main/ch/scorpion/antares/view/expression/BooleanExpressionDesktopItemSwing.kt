package ch.scorpion.antares.view.expression

import ch.scorpion.antares.AntaresModuleJvm
import ch.scorpion.antares.model.expression.*
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableReference
import ch.scorpion.antares.model.truthtable.TruthTableService
import ch.scorpion.antares.view.synthesis.CreateCircuitFromTruthTablePanel
import ch.scorpion.antares.view.synthesis.CreateCircuitFromTruthTableService
import ch.scorpion.antares.view.truthtable.TruthTableDesktopItemSwing
import ch.scorpion.antares.view.truthtable.TruthTableTableView
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.DslError
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.help.HelpId
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.LineNumberTextArea
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.HelpAction
import ch.scorpion.jabbah.draw.view.DrawViewModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.graph.AbstractTitledGraphDesktopViewItemSwing
import ch.scorpion.jabbah.graph.ui.desktop.GraphDesktopViewItem
import java.awt.*
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class BooleanExpressionDesktopItemSwing(
	private val item: BooleanExpressionLibraryItem,
	private val applicationDataHolder: ApplicationDataHolder,
	private val commandManager: CommandManager,
	private val expressionService: BooleanExpressionService = AntaresModelModule.booleanExpressionService,
	private val truthTableService: TruthTableService = AntaresModelModule.truthTableService,
	private val createCircuitService: CreateCircuitFromTruthTableService = AntaresModuleJvm.createCircuitFromTruthTableService,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractTitledGraphDesktopViewItemSwing(
	createTitleText(item.storable),
	JPanel(),
	applicationDataHolder,
	eventBus,
	listOf(HelpAction.withSmallImage(HELP_ID))
) {

	companion object {
		val HELP_ID = HelpId("booleanExpressionDesktopItem")

		private val ERROR_ICON = UiUtil.themedIcon("/img/error-16.png")
		private val CORRECT_ICON = UiUtil.themedIcon("/img/checkmark.png")
		private val FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)
		private const val PREF_TEXT_AREA_HEIGHT = 150

		fun createTitleText(expression: BooleanExpressionStorable): String =
			"${Translations.getString("library.element.booleanExpression.name")} \"${expression.name.getTranslation()}\""
	}

	private val ref = BooleanExpressionReference(applicationDataHolder.data!!.content as BooleanExpressionStorable)

	private val singleCharIdentifierCheckbox = JCheckBox(
		Translations.getString("antares.booleanExpression.singleCharIdentifier"),
		item.storable.singleCharIdentifier)

	private val expressionsTextArea = LineNumberTextArea(text = item.storable.expressions, font = FONT)

	private val minimizedTextArea = JTextArea()

	private val applyAction = ApplyAction()

	private val createCircuitAction = CreateCircuitAction()

	// Initially non-empty to provide a preferred height
	private val messageLabel = JLabel(" ")

	private val minimizedLabel = JLabel(Translations.getString("library.booleanExpression.minimized"))

	private val truthTableLabel = JLabel(Translations.getString("antares.booleanExpression.truthTable.tip"))

	// The calculated minimized TruthTable. Initialized as "dummy" TruthTable in order to setup a TruthTableReference.
	private var truthTable: TruthTable = TruthTable(item.name.value)
		set(value) {
			field = value
			truthTableReference.truthTable = value
			truthTableView.reloadModel()
		}

	private val truthTableReference = TruthTableReference({ truthTable })

	private val truthTableView = TruthTableTableView(truthTableReference, commandManager, editable = false)

	private val exampleTextPane = JTextPane()

	private val createCircuitButton = JButton(ActionWrapperSwing(createCircuitAction))

	private val expressions: BooleanExpressionStorable get() = applicationDataHolder.data!!.content as BooleanExpressionStorable

	init {
		buildUI()

		singleCharIdentifierCheckbox.addActionListener {
			commandManager.execute(BooleanExpressionsSingleCharCommand(ref, singleCharIdentifierCheckbox.isSelected))
		}
		setupViewActivationFocusListener()

		ref.addListener {
			expressionsTextArea.text = ref.expressions.expressions
			singleCharIdentifierCheckbox.isSelected = ref.expressions.singleCharIdentifier
		}

		disableApplyActionForNoExpressionChanges()

		if (item.storable.expressions.isNotBlank()) {
			generateMinimizedExpressions()
		}

		SwingUtilities.invokeLater {
			expressionsTextArea.mainTextArea.requestFocusInWindow()
		}
	}

	override fun createHeaderText(): String = createTitleText(expressions)

	override fun displays(content: Any?): Boolean =
		applicationDataHolder.data?.content is BooleanExpressionStorable && content === ref.expressions

	private fun setupViewActivationFocusListener() {
		val focusListener = object : FocusListener {
			override fun focusGained(e: FocusEvent?) {
				DrawViewModule.viewManager.activeView = this@BooleanExpressionDesktopItemSwing
			}

			override fun focusLost(e: FocusEvent?) { }
		}
		singleCharIdentifierCheckbox.addFocusListener(focusListener)
		expressionsTextArea.mainTextArea.addFocusListener(focusListener)
		minimizedTextArea.addFocusListener(focusListener)
		truthTableView.table.addFocusListener(focusListener)
		createCircuitButton.addFocusListener(focusListener)
	}

	private fun disableApplyActionForNoExpressionChanges() {
		applyAction.enabled = false
		expressionsTextArea.mainTextArea.document.addDocumentListener(object : DocumentListener {
			override fun insertUpdate(e: DocumentEvent?) { disableApply() }
			override fun removeUpdate(e: DocumentEvent?) { disableApply() }
			override fun changedUpdate(e: DocumentEvent?) { disableApply() }
		})
		singleCharIdentifierCheckbox.addActionListener {
			disableApply()
			updateExpressionsExample()
		}
	}

	private fun disableApply() {
		applyAction.enabled = true
		messageLabel.icon = null
		messageLabel.text = null
	}

	private fun buildUI() {
		with(contentPanel) {
			border = BorderFactory.createEmptyBorder(0, 5, 0, 5)
			layout = BorderLayout(10, 10)
			add(createContentsPanel(), BorderLayout.CENTER)
		}
	}

	private fun createContentsPanel(): JComponent {
		val panel = JPanel(BorderLayout())
		panel.add(buildExpressionsPanel(), BorderLayout.NORTH)
		panel.add(buildTruthTablePanel(), BorderLayout.CENTER)
		return panel
	}

	private fun buildTruthTablePanel(): JComponent {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)

		truthTableLabel.border = BorderFactory.createEmptyBorder(25, 2, 5, 0)
		truthTableLabel.alignmentX = Component.LEFT_ALIGNMENT
		panel.add(truthTableLabel)

		truthTableView.alignmentX = Component.LEFT_ALIGNMENT
		truthTableView.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
		panel.add(truthTableView)

		createCircuitButton.alignmentX = Component.LEFT_ALIGNMENT
		panel.add(Box.createVerticalStrut(5))
		panel.add(createCircuitButton)

		return panel
	}

	private fun buildExpressionsPanel(): JPanel {
		val contentPanel = JPanel(BorderLayout(30, 10))

		val centerPanel = JPanel()
		centerPanel.layout = BoxLayout(centerPanel, BoxLayout.PAGE_AXIS)

		singleCharIdentifierCheckbox.alignmentX = Component.LEFT_ALIGNMENT
		singleCharIdentifierCheckbox.border = BorderFactory.createEmptyBorder(10, 2, 0, 0)
		centerPanel.add(singleCharIdentifierCheckbox)

		val tipLabel = JLabel(Translations.getString("library.element.booleanExpression.tip"))
		tipLabel.border = BorderFactory.createEmptyBorder(25, 2, 5, 0)
		centerPanel.add(tipLabel)

		expressionsTextArea.alignmentX = Component.LEFT_ALIGNMENT
		expressionsTextArea.minimumSize = Dimension(expressionsTextArea.minimumSize.width, PREF_TEXT_AREA_HEIGHT)
		expressionsTextArea.preferredSize = Dimension(Int.MAX_VALUE, PREF_TEXT_AREA_HEIGHT)
		expressionsTextArea.maximumSize = expressionsTextArea.preferredSize
		centerPanel.add(expressionsTextArea)

		val statusPanel = JPanel()
		statusPanel.layout = BoxLayout(statusPanel, BoxLayout.LINE_AXIS)
		statusPanel.alignmentX = Component.LEFT_ALIGNMENT

		val applyButton = JButton(ActionWrapperSwing(applyAction))
		applyButton.alignmentX = Component.LEFT_ALIGNMENT
		statusPanel.add(applyButton)

		messageLabel.alignmentX = Component.LEFT_ALIGNMENT
		messageLabel.preferredSize = Dimension(Integer.MAX_VALUE, messageLabel.preferredSize.height)
		statusPanel.add(Box.createHorizontalStrut(10))
		statusPanel.add(messageLabel)
		statusPanel.maximumSize = statusPanel.preferredSize

		centerPanel.add(Box.createVerticalStrut(3))
		centerPanel.add(statusPanel)

		minimizedLabel.alignmentX = Component.LEFT_ALIGNMENT
		minimizedLabel.border = BorderFactory.createEmptyBorder(25, 2, 5, 0)
		centerPanel.add(minimizedLabel)

		minimizedTextArea.isEditable = false
		minimizedTextArea.font = FONT
		minimizedTextArea.rows = 6

		val minimizedScrollPane = JScrollPane(minimizedTextArea)
		minimizedScrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
		minimizedScrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
		minimizedScrollPane.alignmentX = Component.LEFT_ALIGNMENT
		minimizedScrollPane.minimumSize = Dimension(minimizedScrollPane.minimumSize.width, PREF_TEXT_AREA_HEIGHT)
		minimizedScrollPane.preferredSize = Dimension(Int.MAX_VALUE, PREF_TEXT_AREA_HEIGHT)
		minimizedScrollPane.maximumSize = minimizedScrollPane.preferredSize
		centerPanel.add(minimizedScrollPane)

		exampleTextPane.contentType = "text/html"
		exampleTextPane.isEditable = false
		exampleTextPane.alignmentX = Component.LEFT_ALIGNMENT
		updateExpressionsExample()

		contentPanel.add(centerPanel, BorderLayout.CENTER)
		contentPanel.add(exampleTextPane, BorderLayout.EAST)

		return contentPanel
	}

	private fun updateExpressionsExample() {
		exampleTextPane.text = if (singleCharIdentifierCheckbox.isSelected) {
			"""
			${Translations.getString("antares.booleanExpression.example")}
			<br>
			<br>
			U = AB' + A'B + 0<br>
			V' = (A * B') + (A' * B) + 0<br>
			W = (A ∧ ¬B) ∨ (¬A ∧ B) ∨ 0<br>
			X = (A && !B) || (!A && B) || 0<br>
			Y = (A AND NOT B) OR (NOT A AND B) OR true<br>
		""".trimIndent()
		} else {
			"""
			${Translations.getString("antares.booleanExpression.example")}
			<br>
			<br>
			U = A * B' + A * 'B + 0<br>
			V' = (A * B') + (A' * B) + 0<br>
			W = (A ∧ ¬B) ∨ (¬A ∧ B) ∨ 0<br>
			X = (A && !B) || (!A && B) || 0<br>
			Y = (A AND NOT B) OR (NOT A AND B) OR true<br>
		""".trimIndent()
		}
	}

	/** ---- [GraphDesktopViewItem] */

	override fun disposeItem() {
		super.disposeItem()
		ref.dispose()
	}

	/** ---- [BooleanExpressionDesktopItemSwing] */

	private fun apply() {
		applyChanges()
		generateMinimizedExpressions()
	}

	private fun applyChanges() {
		commandManager.execute(BooleanExpressionsCommand(ref, expressionsTextArea.text))
		applyAction.enabled = false
	}

	private fun generateMinimizedExpressions() {
		try {
			val result = expressionService.parseExpressions(expressionsTextArea.text, singleCharIdentifierCheckbox.isSelected)
			truthTable = expressionService.createTruthTable(result)
			truthTable.name = item.name
			createCircuitAction.enabled = true
			minimizedTextArea.text = truthTableService.generateExpressions(truthTable, BooleanExpressionNotation.fromProperties())
			messageLabel.icon = CORRECT_ICON
			messageLabel.text = Translations.getString("edit.dsl.check.success.msg")
		} catch (e: DslError) {
			truthTable = TruthTable(item.name.value)
			createCircuitAction.enabled = false
			messageLabel.icon = ERROR_ICON
			messageLabel.text = e.toString()
			minimizedTextArea.text = ""
		}
	}

	private fun createCircuit() {
		CreateCircuitFromTruthTablePanel.showAsDialog(
			Frame.getFrames()[0],
			truthTable,
			item,
			createCircuitService)
	}

	private inner class ApplyAction : AbstractAction("base.action.apply") {
		override fun execute(event: ActionEvent) {
			apply()
		}
	}

	private inner class CreateCircuitAction
		: AbstractAction("antares.synthesis.createCircuitFromTruthTable.action", opensDialog = true)
	{
		override fun execute(event: ActionEvent) {
			createCircuit()
		}
	}
}