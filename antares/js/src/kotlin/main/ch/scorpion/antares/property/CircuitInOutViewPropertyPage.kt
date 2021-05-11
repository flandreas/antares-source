package ch.scorpion.antares.property

import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.properties.PropertyPageRenderer
import ch.scorpion.jabbah.edit.properties.jmTextField
import com.ccfraser.muirwik.components.mTextField
import react.RBuilder

@Suppress("unused")
class CircuitInOutViewPropertyPage : PropertyPageRenderer<CircuitInOutView> {

	override fun render(bean: CircuitInOutView, editor: Editor, builder: RBuilder) {
		builder.run {
			mTextField(Translations.getString("edit.property.id"), value = bean.id.toString(), disabled = true)
			jmTextField {
				this.editor = editor
				this.beanProvider = componentBeanProvider
				this.beanIds = listOf(bean.id)
				this.getter = { bean.name }
				this.setter = { _, value -> bean.name = value }
			}
		}
	}
}