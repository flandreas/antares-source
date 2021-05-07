package ch.scorpion.jabbah.edit.ui

import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView

/** A [UIView] for displaying and editing the properties of arbitrary objects.*/
interface PropertyPanel : UIView {
	fun clearProperties()
	fun loadProperties(bean: Any)
}

/** Controls displaying and editing the properties of arbitrary objects.*/
open abstract class PropertyPanelController<T: PropertyPanel> : AbstractUIController<T>()