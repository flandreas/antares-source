package ch.scorpion.jabbah.base.mreact

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import com.ccfraser.muirwik.components.MColor
import com.ccfraser.muirwik.components.button.*
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

fun RBuilder.jmButton(
	action: Action,
	color: MColor = MColor.inherit,
	variant: MButtonVariant = MButtonVariant.outlined,
	size: MButtonSize = MButtonSize.small,
	handler: (JMButtonProps.() -> Unit)? = null
) {
	child(JabbahMaterialButton::class) {
		attrs.action = action
		attrs.color = color
		attrs.variant = variant
		attrs.size = size
		handler?.let { attrs(it) }
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
			attrs.size = props.size
			attrs.variant = props.variant
			attrs.disabled = !props.action.enabled
			attrs.onClick = { props.action.execute(ActionEvent("click", this, 0, "click", 0)) }

			childList.add(ReactNode(props.action.name))
		}
	}
}