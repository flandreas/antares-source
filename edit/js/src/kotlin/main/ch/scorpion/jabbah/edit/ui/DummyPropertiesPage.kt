package ch.scorpion.jabbah.edit.ui

import com.ccfraser.muirwik.components.form.mFormControlLabel
import com.ccfraser.muirwik.components.mCheckbox
import com.ccfraser.muirwik.components.mTextField
import kotlinx.html.InputType
import react.*
import react.dom.br

external interface DummyPropertiesPageProps : RProps

fun RBuilder.dummyPropertiesPage(handler: DummyPropertiesPageProps.() -> Unit): ReactElement =
	child(DummyPropertiesPage::class) {
		this.attrs(handler)
	}

class DummyPropertiesPage : RComponent<DummyPropertiesPageProps, RState>() {

	override fun RBuilder.render() {
		mTextField("ID", "36", type = InputType.number, disabled = true)
		br {  }
		mTextField("Model ID", "38", type = InputType.number, disabled = true)
		br {  }
		mTextField("Propagation Delay", defaultValue = "0", type = InputType.number, disabled = false)
		br {  }
		mFormControlLabel("Shadow", mCheckbox(true))
		br {  }
		mTextField("Name", defaultValue = "This is a name")
		br {  }
		mTextField("Description", defaultValue = "This is a description") {
			attrs.multiline = true
		}
	}
}