package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.text.FormattedText
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.DrawingView

/** A [UIView] for displaying and editing the properties of arbitrary objects.*/
interface PropertyPanel : UIView {

	/** Called by [AbstractPropertyPanelController] when [AbstractPropertyPanelController.bean] has been replaced.*/
	fun handleBeanReplaced()
}

/**
 * Controls displaying and editing the properties of arbitrary objects.
 * @param editor the [Editor] used for creating undoable [Command]s when changing properties
 */
abstract class AbstractPropertyPanelController<T: PropertyPanel>(
	val editor: Editor
) : AbstractUIController<T>() {

	/** The translated text describing the selected bean to be used as title in [PropertyPanel].*/
	var title: String = Translations.getString("edit.property.bean.none")
		protected set

	var bean: Any? = null
		protected set(value) {
			if (value !== field) {
				field = value
				updateTitle()
				view.handleBeanReplaced()
			}
		}

	/** A displayable description of the currently selected bean.*/
	protected abstract val description: String?

	/** The bean to be displayed if no selection exists, such as an entire drawing. */
	protected open val defaultBean: Any? = null

	private lateinit var activeEditorListener: PropertyChangeListener<Any>

	private lateinit var drawingListener: PropertyChangeListener<Any>

	override fun onViewInitialized() {
		super.onViewInitialized()
		activeEditorListener = setupActiveEditorListener()
		drawingListener = setupDrawingListener()
	}

	override fun dispose() {
		super.dispose()
		editor.removePropertyChangeListener(activeEditorListener)
		editor.removePropertyChangeListener(drawingListener)
	}

	fun refresh() {
		view.handleBeanReplaced()
	}

	private fun updateTitle() {
		title = if (bean == null) {
			Translations.getString("edit.property.bean.none")
		} else {
			val beanDescription = if (StringUtils.isEmpty(description)) {
				Translations.getString("edit.property.bean.undefined")
			} else {
				FormattedText.replaceNegation(description!!).textWithOverline
			}
			Translations.getString("edit.property.bean", beanDescription)
		}
	}

	private fun setupActiveEditorListener(): PropertyChangeListener<Any> = editor.addPropertyChangeListener { event ->
		if (event.name == Editor.PROP_ACTIVE) {
			bean = defaultBean
		}
	}

	private fun setupDrawingListener(): PropertyChangeListener<Any> = editor.view.addPropertyChangeListener { event ->
		if (event.name == DrawingView.PROP_DRAWING) {
			bean = defaultBean
		}
	}
}