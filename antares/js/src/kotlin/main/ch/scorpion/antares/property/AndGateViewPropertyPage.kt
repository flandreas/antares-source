package ch.scorpion.antares.property

import ch.scorpion.antares.view.gate.AndGateView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.PropertyPageRenderer
import react.RBuilder
import react.dom.div

@Suppress("unused")
class AndGateViewPropertyPage : PropertyPageRenderer<AndGateView> {

	override fun render(bean: AndGateView, editor: Editor, builder: RBuilder) {
		builder.run {
			div {
				+"This is AND gate ${bean.id}"
			}
		}
	}
}