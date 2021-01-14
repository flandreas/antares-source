package ch.scorpion.jabbah.base.mreact

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.MButtonProps
import org.w3c.dom.events.MouseEvent
import react.RBuilder
import react.RComponent
import react.RState
import react.ReactElement

@JsModule("@material-ui/lab/ToggleButton")
@JsNonModule
private external val toggleButtonModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val toggleButtonComponent: RComponent<JMToggleButtonProps, RState> = toggleButtonModule.default

interface JMToggleButtonProps : MButtonProps {
	var action: Action
	var iconName: String
	var selected: Boolean
	var value: Any
}

fun RBuilder.jrToggleButton(handler: JMToggleButtonProps.() -> Unit): ReactElement {
	return child(JabbahReactToggleButton::class) {
		this.attrs(handler)
	}
}

/** Wraps a Jabbah [Action] in a React Material ToggleButton.*/
class JabbahReactToggleButton(
) : RComponent<JMToggleButtonProps, RState>() {

	private val actionListener = object : PropertyChangeListener<Any> {
		override fun propertyChanged(e: PropertyChangeEvent<Any>) { forceUpdate() }
	}

	override fun componentDidMount() {
		props.action.addPropertyChangeListener(actionListener)
	}

	override fun componentWillUnmount() {
		props.action.removePropertyChangeListener(actionListener)
	}

	override fun RBuilder.render() {
		createStyled(toggleButtonComponent) {
			attrs.disabled = !props.action.enabled
			attrs.selected = props.action.selected
			attrs.value = ""
			attrs.onClick = ::onClick

			mIcon(props.iconName, fontSize = MIconFontSize.small)
		}
	}

	private fun onClick(event: MouseEvent) {
		props.action.execute(ActionEvent("click", this, 0, "click", 0))
	}
}