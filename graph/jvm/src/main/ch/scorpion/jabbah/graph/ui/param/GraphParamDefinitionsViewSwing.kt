package ch.scorpion.jabbah.graph.ui.param

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.DataFormPanel
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.base.ui.HelpAction
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.edit.semantic.Semantic
import ch.scorpion.jabbah.edit.semantic.createSemanticComboBox
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.param.*
import java.awt.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.AbstractTableModel

/**
 * A UI for managing the [GraphParamDefinitions] of a [Graph].
 *
 * Shows the list of existing [GraphParamDefinition], and allows the user to add new ones,
 * edit existing ones, and delete them.
 */
class GraphParamDefinitionsViewSwing(
	private val controller: GraphParamDefinitionsController,
	private val closeHandler: () -> Unit
) : JPanel(), GraphParamDefinitionsView {

	companion object {

		/**
		 * Allows the user to edit [GraphParamDefinitions].
		 * @return the edited [GraphParamDefinitions], or `null` if the user closed the dialog with 'Cancel'
		 */
		fun showAsDialog(parent: Frame, graph: Graph): GraphParamDefinitions? {
			val controller = GraphParamDefinitionsController(graph)

			DialogBuilder<GraphParamDefinitionsViewSwing>(parent)
				.content { dialog -> GraphParamDefinitionsViewSwing(controller, closeHandler = { dialog.dispose() }) }
				.title(Translations.getString("graph.paramDefs.dialog.title"))
				.preferredSize(Dimension(500, 400))
				.show()

			return controller.definitionsToReturn
		}
	}

	private val table = JTable(TableModel())

	private val form = DataFormPanel()

	private val nameField = JTextField(20)
	private val typeField = createTypeEditor()
	private val defaultValueFieldHolder = JPanel()
	private var defaultValueEditor: GraphParamValueEditor? = null
	private val semanticField: JComboBox<Semantic> = createSemanticComboBox(Translations.getString("graph.paramDefs.dialog.semantic.none"))
	private val errorMessageLabel = JLabel(" ", SwingConstants.LEADING)

	private val documentListener = object : DocumentListener {
		override fun insertUpdate(e: DocumentEvent?) { controller.formChanged() }
		override fun removeUpdate(e: DocumentEvent?) { controller.formChanged() }
		override fun changedUpdate(e: DocumentEvent?) { controller.formChanged() }
	}

	init {
		controller.view = this

		errorMessageLabel.foreground = UiUtil.errorTextColor
		errorMessageLabel.alignmentX = Component.LEFT_ALIGNMENT

		buildUI()

		nameField.document.addDocumentListener(documentListener)

		typeField.addActionListener {
			controller.formChanged()
			setDefaultValueEditor(typeField.selectedItem as GraphParamType<*>)
		}

		semanticField.addActionListener {
			controller.formChanged()
		}

		table.selectionModel.addListSelectionListener {
			val newSelection = if (table.selectionModel.selectedItemsCount > 0) {
				controller.value.get(table.selectedRow)
			} else {
				null
			}
			if (newSelection == null) {
				clearForm()
			} else {
				fillForm(newSelection)
			}
			controller.selectedDefinition = newSelection
		}

		clearForm()
	}

	private fun clearForm() {
		nameField.text = ""
		typeField.selectedIndex = 0
		semanticField.selectedItem = null
		setDefaultValueEditor(typeField.selectedItem as GraphParamType<*>)

		updateFormEnabledness()

		invalidate()
		revalidate()
		repaint()

		controller.formReset()
	}

	private fun fillForm(def: GraphParamDefinition<*>) {
		nameField.text = def.name
		typeField.selectedItem = def.type
		semanticField.selectedItem = def.semantic
		setDefaultValueEditor(def.type, def.defaultValue)

		updateFormEnabledness()

		invalidate()
		revalidate()
		repaint()

		controller.formReset()
	}

	private fun updateFormEnabledness() {
		controller.isFormEnabled.let {
			nameField.isEnabled = it
			typeField.isEnabled = it
			defaultValueEditor?.editorEnabled = it
			semanticField.isEnabled = it
		}
	}

	private fun setDefaultValueEditor(type: GraphParamType<*>, defaultValue: Any? = null) {
		defaultValueEditor = createDefaultValueEditor(type).also { editor ->
			defaultValue?.let { editor.paramValue = it }
			defaultValueFieldHolder.removeAll()
			defaultValueFieldHolder.add(editor as JComponent, BorderLayout.CENTER)
		}
	}

	/** ---- [GraphParamDefinitionsView] */

	override fun dispose() { }

	override fun close() {
		closeHandler()
	}

	override fun startAdding(name: String) {
		table.selectionModel.clearSelection()
		clearForm()
		nameField.text = name
		typeField.selectedItem = typeField.getItemAt(0)
		setDefaultValueEditor(typeField.selectedItem as GraphParamType<*>)
		semanticField.selectedItem = null
		nameField.requestFocusInWindow()
	}

	override fun <T: Any> getEditedDefinition(): GraphParamDefinition<T> =
		GraphParamDefinition.create(
			nameField.text,
			typeField.selectedItem as GraphParamType<T>,
			defaultValueEditor!!.paramValue as T,
			semanticField.selectedItem as Semantic?
		)

	override fun valueChanged() {
		table.model = TableModel()
	}

	override fun errorMessage(msg: String?) {
		if (msg != null) {
			errorMessageLabel.text = msg
		} else {
			errorMessageLabel.text = " "
		}
	}

	/** ---- [GraphParamDefinitionsViewSwing] */

	private fun buildUI() {
		val buttonDist = 4
		layout = BorderLayout(10, 10)
		border = UIBasics.createDialogBorder()

		defaultValueFieldHolder.layout = BorderLayout()

		val scrollPane = JScrollPane(table)
		add(scrollPane, BorderLayout.CENTER)

		val southPanel = JPanel()
		southPanel.layout = BoxLayout(southPanel, BoxLayout.PAGE_AXIS)

		val tableButtonPanel = JPanel()
		tableButtonPanel.layout = BoxLayout(tableButtonPanel, BoxLayout.LINE_AXIS)
		tableButtonPanel.add(createButton(controller.addAction))
		tableButtonPanel.add(Box.createHorizontalStrut(buttonDist))
		tableButtonPanel.add(createButton(controller.removeAction))
		tableButtonPanel.add(Box.createHorizontalStrut(buttonDist))
		tableButtonPanel.add(createButton(controller.applyAction))
		tableButtonPanel.add(Box.createHorizontalGlue())
		southPanel.add(tableButtonPanel)

		form.addLabeledRow(Translations.getString("graph.paramDefs.dialog.name"), nameField)
		form.addLabeledRow(Translations.getString("graph.paramDefs.dialog.type"), typeField)
		form.addLabeledRow(Translations.getString("graph.paramDefs.dialog.defaultValue"), defaultValueFieldHolder)
		form.addLabeledRow(Translations.getString("graph.paramDefs.dialog.semantic"), semanticField)
		southPanel.add(Box.createVerticalStrut(10))
		southPanel.add(form)

		southPanel.add(Box.createVerticalStrut(5))
		val messagePanel = JPanel(BorderLayout())
		messagePanel.add(errorMessageLabel)
		southPanel.add(messagePanel)

		val southButtonPanel = JPanel()
		southButtonPanel.layout = BoxLayout(southButtonPanel, BoxLayout.LINE_AXIS)
		southButtonPanel.add(UiUtil.createToolBarButton(HelpAction(GraphParamDefinitions.HELP_ID)))
		southButtonPanel.add(Box.createHorizontalGlue())
		UIBasics.addButtons(southButtonPanel, createButton(controller.saveAction), createButton(controller.cancelAction))
		southPanel.add(Box.createVerticalStrut(20))
		southPanel.add(southButtonPanel)

		add(southPanel, BorderLayout.SOUTH)
	}

	private fun createTypeEditor(): JComboBox<GraphParamType<*>> {
		val comboBox = JComboBox<GraphParamType<*>>()
		comboBox.model = DefaultComboBoxModel(GraphParamTypeRegistry.getAll().toTypedArray())
		comboBox.addActionListener { controller.formChanged() }
		return comboBox
	}

	private fun createDefaultValueEditor(type: GraphParamType<*>): GraphParamValueEditor {
		val editor = GraphParamValueEditorRegistry.create(type)
		editor.changeHandler = controller::formChanged
		return editor
	}

	private fun createButton(action: Action): JButton = JButton(ActionWrapperSwing(action))

	private inner class TableModel : AbstractTableModel() {

		private fun getDefinition(rowIndex: Int): GraphParamDefinition<*> =
			controller.value.get(rowIndex)

		override fun getRowCount(): Int = controller.value.size

		override fun getColumnCount(): Int = 4

		override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
			val definition = getDefinition(rowIndex)
			return when (columnIndex) {
				0 -> definition.name
				1 -> definition.type.displayableName
				2 -> definition.defaultValue.toString()
				3 -> definition.semantic?.translatedName ?: Translations.getString("graph.paramDefs.dialog.semantic.none")
				else -> throw IllegalArgumentException("column does not exist")
			}
		}

		override fun getColumnName(column: Int): String =
			when (column) {
				0 -> Translations.getString("graph.paramDefs.dialog.name")
				1 -> Translations.getString("graph.paramDefs.dialog.type")
				2 -> Translations.getString("graph.paramDefs.dialog.defaultValue")
				3 -> Translations.getString("graph.paramDefs.dialog.semantic")
				else -> throw IllegalArgumentException("column does not exist")
			}
	}
}