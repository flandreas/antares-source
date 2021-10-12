package ch.scorpion.jabbah.base.mreact

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import com.ccfraser.muirwik.components.MIconFontSize
import com.ccfraser.muirwik.components.button.MButtonProps
import com.ccfraser.muirwik.components.createStyled
import com.ccfraser.muirwik.components.mIcon
import com.ccfraser.muirwik.components.mTooltip
import org.w3c.dom.events.MouseEvent
import react.ComponentType
import react.RBuilder
import react.RComponent
import react.State

@JsModule("@material-ui/lab/ToggleButton")
@JsNonModule
private external val toggleButtonModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val toggleButtonComponent: ComponentType<JMToggleButtonProps> = toggleButtonModule.default

interface JMToggleButtonProps : MButtonProps {
	var action: Action
	var iconName: String
	var selected: Boolean
	var value: Any
}

fun RBuilder.jmToggleButton(handler: JMToggleButtonProps.() -> Unit) {
	child(JabbahMaterialToggleButton::class) {
		this.attrs(handler)
	}
}

/** Wraps a Jabbah [Action] in a React Material ToggleButton.*/
class JabbahMaterialToggleButton : RComponent<JMToggleButtonProps, State>() {

	private val actionListener = PropertyChangeListener<Any> { forceUpdate() }

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

			props.action.description?.let {
				mTooltip(it) {
					mIcon(props.iconName, fontSize = MIconFontSize.small)
				}
			} ?: mIcon(props.iconName, fontSize = MIconFontSize.small)
		}
	}

	private fun onClick(event: MouseEvent) {
		props.action.execute(ActionEvent("click", this, 0, "click", 0))
	}
}