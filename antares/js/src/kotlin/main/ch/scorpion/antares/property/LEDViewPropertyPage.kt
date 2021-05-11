package ch.scorpion.antares.property

import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyPageRenderer
import react.RBuilder
import react.dom.div

@Suppress("unused")
class LEDViewPropertyPage : PropertyPageRenderer<LEDView> {

	override fun render(bean: LEDView, editor: Editor, builder: RBuilder) {
		builder.run {
			div {
				+"This is LED ${bean.id}"
			}
		}
	}
}