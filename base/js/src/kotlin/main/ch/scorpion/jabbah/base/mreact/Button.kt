package ch.scorpion.jabbah.base.mreact

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import com.ccfraser.muirwik.components.button.MButtonProps
import com.ccfraser.muirwik.components.button.color
import com.ccfraser.muirwik.components.button.variant
import com.ccfraser.muirwik.components.createStyled
import react.RBuilder
import react.RComponent
import react.RState
import react.ReactElement


@JsModule("@material-ui/core/Button")
@JsNonModule
private external val buttonModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val buttonComponent: RComponent<JMButtonProps, RState> = buttonModule.default

interface JMButtonProps : MButtonProps {
	var action: Action
}

fun RBuilder.jrButton(handler: JMButtonProps.() -> Unit): ReactElement {
	return child(JabbahReactButton::class) {
		this.attrs(handler)
	}
}

/** Wraps a Jabbah [Action] in a React Material Button.*/
class JabbahReactButton : RComponent<JMButtonProps, RState>() {

	override fun componentDidMount() {
		props.action.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) { forceUpdate() }
		})
	}

	override fun RBuilder.render() {
		createStyled(buttonComponent) {
			attrs.color = props.color
			attrs.variant = props.variant
			attrs.disabled = !props.action.enabled
			attrs.onClick = { props.action.execute(ActionEvent("click", this, 0, "click", 0)) }

			// TODO This should really use the 'name' instead of the 'description'
			// Currently needed to ToggleApplicationModeAction work (which is really a ToggleAction)
			childList.add(props.action.description ?: "Button")
		}
	}
}

