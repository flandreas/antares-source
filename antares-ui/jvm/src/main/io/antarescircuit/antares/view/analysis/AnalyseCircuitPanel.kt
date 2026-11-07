package io.antarescircuit.antares.view.analysis

import io.antarescircuit.antares.model.expression.BooleanExpressionLibraryItem
import io.antarescircuit.antares.model.expression.BooleanExpressionNotation
import io.antarescircuit.antares.model.expression.BooleanExpressionStorable
import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.antares.model.truthtable.TruthTable
import io.antarescircuit.antares.model.truthtable.TruthTableLibraryItem
import io.antarescircuit.antares.model.truthtable.TruthTableReference
import io.antarescircuit.antares.model.truthtable.TruthTableService
import io.antarescircuit.antares.view.truthtable.TruthTableTableView
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.ui.NewNamePanel
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import java.awt.*
import javax.swing.*
import kotlin.math.max

class AnalyseCircuitPanel(
	private val containerLibraryElement: ContainerLibraryElement,
	private val truthTable: TruthTable,
	commandManager: CommandManager = EditModule.commandManager,
	private val truthTableService: TruthTableService = AntaresModelModule.truthTableService,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		private val EXPRESSION_FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)
		private const val PREF_TEXT_AREA_HEIGHT = 150

		fun showAsDialog(
			parent: Frame,
			containerLibraryElement: ContainerLibraryElement,
			truthTable: TruthTable,
			commandManager: CommandManager = EditModule.commandManager,
			truthTableService: TruthTableService = AntaresModelModule.truthTableService
		) {
			DialogBuilder<AnalyseCircuitPanel>(parent)
				.content { dialog -> AnalyseCircuitPanel(containerLibraryElement, truthTable, commandManager, truthTableService, closeHandler = { dialog.dispose()} )}
				.title(Translations.getString("antares.circuitAnalysis.title"))
				.defaultButton { it.closeButton }
				.preferredSize(Dimension(600, 600))
				.resizable()
				.show()
		}
	}

	private val closeAction = CloseAction()
	private val closeButton = JButton(ActionWrapperSwing(closeAction))

	private val ref = TruthTableReference(truthTableProvider = { truthTable })

	private val tableView = TruthTableTableView(ref, commandManager, editable = false)

	private val expressionsTextArea = JTextArea()

	private val saveTruthTableAction = SaveTruthTableAction()

	private val saveExpressionsAction = SaveExpressionsAction()

	init {
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		add(buildContentsPanel(), BorderLayout.CENTER)
		add(buildButtonPanel(), BorderLayout.SOUTH)
	}

	private fun buildContentsPanel(): JPanel {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.PAGE_AXIS)

		val tableLabel = JLabel(Translations.getString("library.element.truthTable.name"))
		tableLabel.border = BorderFactory.createEmptyBorder(5, 2, 5, 0)
		tableLabel.alignmentX = Component.LEFT_ALIGNMENT
		panel.add(tableLabel)

		tableView.alignmentX = Component.LEFT_ALIGNMENT
		panel.add(tableView)
		panel.add(Box.createVerticalStrut(15))

		val expressionsLabel = JLabel(Translations.getString("library.booleanExpression.minimized"))
		expressionsLabel.border = BorderFactory.createEmptyBorder(5, 2, 5, 0)
		expressionsLabel.alignmentX = Component.LEFT_ALIGNMENT
		panel.add(expressionsLabel)

		expressionsTextArea.font = EXPRESSION_FONT
		expressionsTextArea.isEditable = false
		expressionsTextArea.alignmentX = Component.LEFT_ALIGNMENT
		expressionsTextArea.rows = max(4, ref.truthTable.outputColumnCount)
		expressionsTextArea.text = truthTableService.generateExpressions(truthTable, BooleanExpressionNotation.fromProperties())

		val expressionScrollPane = JScrollPane(expressionsTextArea)
		expressionScrollPane.alignmentX = Component.LEFT_ALIGNMENT
		expressionScrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
		expressionScrollPane.verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
		expressionScrollPane.minimumSize = Dimension(expressionScrollPane.minimumSize.width, PREF_TEXT_AREA_HEIGHT)
		expressionScrollPane.preferredSize = Dimension(Int.MAX_VALUE, PREF_TEXT_AREA_HEIGHT)
		expressionScrollPane.maximumSize = expressionScrollPane.preferredSize

		panel.add(expressionScrollPane)

		return panel
	}

	private fun buildButtonPanel(): JPanel {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

		panel.add(JButton(ActionWrapperSwing(saveTruthTableAction)))
		panel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		panel.add(JButton(ActionWrapperSwing(saveExpressionsAction)))
		panel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		panel.add(Box.createHorizontalGlue())
		panel.add(closeButton)

		return panel
	}

	private inner class CloseAction : AbstractAction("base.action.close") {
		override fun execute(event: ActionEvent) {
			closeHandler()
		}
	}

	private inner class SaveTruthTableAction : AbstractAction("antares.circuitAnalysis.saveTruthTable.action", opensDialog = true) {

		override fun execute(event: ActionEvent) {
			SaveTruthTablePanel.showAsDialog(name, Frame.getFrames()[0])
				?.let {
					truthTable.name = Name(it)
					val library = containerLibraryElement.library!!
					val directory = library.libraryService.getDirectoryOf(library, containerLibraryElement)
					val item = TruthTableLibraryItem(truthTable)

					library.libraryService.addLibraryItem(library, item, directory)
				}
		}
	}

	private inner class SaveExpressionsAction : AbstractAction("antares.circuitAnalysis.saveExpressions.action", opensDialog = true) {

		override fun execute(event: ActionEvent) {
			NewNamePanel
				.showAsDialog(name, Frame.getFrames()[0])
				?.let { name ->
					val library = containerLibraryElement.library!!
					val directory = library.libraryService.getDirectoryOf(library, containerLibraryElement)
					val item = BooleanExpressionLibraryItem(TranslatableText(name))
					item.updateStorable(BooleanExpressionStorable(
						TranslatableText(name),
						expressionsTextArea.text,
						singleCharIdentifier = truthTable.allNamesAreSingleChar)
					)

					library.libraryService.addLibraryItem(library, item, directory)
				}
		}
	}
}