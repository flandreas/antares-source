package ch.scorpion.antares.property

import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.jabbah.edit.properties.PropertyPageRenderer
import react.RBuilder
import react.dom.div

class AndGateViewPropertyPage : PropertyPageRenderer {

	override fun render(bean: Any, builder: RBuilder) {
		builder.run {
			div {
				+"This is AND gate ${(bean as AndGateView).id}"
			}
		}
	}
}