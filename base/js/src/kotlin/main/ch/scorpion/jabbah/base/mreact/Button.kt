package ch.scorpion.jabbah.base.mreact

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import com.ccfraser.muirwik.components.button.MButtonProps
import com.ccfraser.muirwik.components.button.color
import com.ccfraser.muirwik.components.button.variant
import com.ccfraser.muirwik.components.createStyled
import react.*


@JsModule("@material-ui/core/Button")
@JsNonModule
private external val buttonModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val buttonComponent: ComponentType<JMButtonProps> = buttonModule.default

interface JMButtonProps : MButtonProps {
	var action: Action
}

fun RBuilder.jmButton(handler: JMButtonProps.() -> Unit) {
	child(JabbahMaterialButton::class) {
		this.attrs(handler)
	}
}

/** Wraps a Jabbah [Action] in a React Material Button.*/
class JabbahMaterialButton : RComponent<JMButtonProps, State>() {

	private val actionListener = PropertyChangeListener<Any> { forceUpdate() }

	override fun componentDidMount() {
		props.action.addPropertyChangeListener(actionListener)
	}

	override fun componentWillUnmount() {
		props.action.removePropertyChangeListener(actionListener)
	}

	override fun RBuilder.render() {
		createStyled(buttonComponent) {
			attrs.color = props.color
			attrs.variant = props.variant
			attrs.disabled = !props.action.enabled
			attrs.onClick = { props.action.execute(ActionEvent("click", this, 0, "click", 0)) }

			// TODO This should really use the 'name' instead of the 'description'
			// Currently needed to ToggleApplicationModeAction work (which is really a ToggleAction)
			childList.add(ReactNode(props.action.description ?: "Button"))
		}
	}
}