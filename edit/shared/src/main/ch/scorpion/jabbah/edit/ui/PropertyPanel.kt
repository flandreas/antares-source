package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.text.FormattedText
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Command

/** A [UIView] for displaying and editing the properties of arbitrary objects.*/
interface PropertyPanel : UIView {

	/** Called by [AbstractPropertyPanelController] when [AbstractPropertyPanelController.bean] has been replaced.*/
	fun handleBeanReplaced()
}

/**
 * Controls displaying and editing the properties of arbitrary objects.
 * @param editor the [Editor] used for creating undoable [Command]s when changing properties
 */
open abstract class AbstractPropertyPanelController<T: PropertyPanel>(
	val editor: Editor
) : AbstractUIController<T>() {

	/** The translated text describing the selected bean to be used as title in [PropertyPanel].*/
	var title: String = ""
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

	private fun updateTitle() {
		title = if (bean == null) {
			""
		} else {
			val beanDescription = if (StringUtils.isEmpty(description)) {
				Translations.getString("edit.property.bean.undefined")
			} else {
				FormattedText.replaceNegation(description!!).textWithOverline
			}
			Translations.getString("edit.property.bean", beanDescription)
		}
	}
}