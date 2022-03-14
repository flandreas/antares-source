package ch.scorpion.antares.view.analysis

import ch.scorpion.antares.model.expression.BooleanExpressionLibraryItem
import ch.scorpion.antares.model.expression.BooleanExpressionNotation
import ch.scorpion.antares.model.expression.BooleanExpressionStorable
import ch.scorpion.antares.model.module.AntaresModelModule
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableReference
import ch.scorpion.antares.model.truthtable.TruthTableService
import ch.scorpion.antares.view.expression.NewBooleanExpressionPanel
import ch.scorpion.antares.view.truthtable.TruthTableTableView
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
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

	private val tableView = TruthTableTableView(ref, commandManager)

	private val expressionsTextArea = JTextArea()

	private val saveExpressionsAction = SaveExpressionsAction()

	init {
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout(10, 10)
		border = BorderFactory.createEmptyBorder(15, 15, 15, 15)

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

		panel.add(expressionScrollPane)

		return panel
	}

	private fun buildButtonPanel(): JPanel {
		val panel = JPanel()
		panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

		panel.add(JButton(ActionWrapperSwing(saveExpressionsAction)))
		panel.add(Box.createHorizontalStrut(5))
		panel.add(Box.createHorizontalGlue())
		panel.add(closeButton)

		return panel
	}

	private inner class CloseAction : AbstractAction("base.action.close") {
		override fun execute(event: ActionEvent) {
			closeHandler()
		}
	}

	private inner class SaveExpressionsAction : AbstractAction("antares.circuitAnalysis.saveExpressions.action") {
		override fun execute(event: ActionEvent) {
			NewBooleanExpressionPanel
				.showAsDialog(Frame.getFrames()[0])
				?.let { name ->
					val library = containerLibraryElement.library!!
					val directory = library.libraryService.getDirectoryOf(library, containerLibraryElement)
					val item = BooleanExpressionLibraryItem(TranslatableText(name))
					item.expressions = BooleanExpressionStorable(expressionsTextArea.text)

					library.libraryService.addLibraryItem(library, item, directory)
				}
		}
	}
}