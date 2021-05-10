package ch.scorpion.antares.property

import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.edit.properties.PropertyPageRenderer
import react.RBuilder
import react.dom.div

class LEDViewPropertyPage : PropertyPageRenderer {

	override fun render(bean: Any, builder: RBuilder) {
		builder.run {
			div {
				+"This is LED ${(bean as LEDView).id}"
			}
		}
	}
}