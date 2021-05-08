package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Command

/** A [UIView] for displaying and editing the properties of arbitrary objects.*/
interface PropertyPanel : UIView {
	fun clearProperties()
	fun loadProperties(bean: Any)
}

/**
 * Controls displaying and editing the properties of arbitrary objects.
 * @param editor the [Editor] used for creating undoable [Command]s when changing properties
 */
open abstract class AbstractPropertyPanelController<T: PropertyPanel>(
	val editor: Editor
) : AbstractUIController<T>()