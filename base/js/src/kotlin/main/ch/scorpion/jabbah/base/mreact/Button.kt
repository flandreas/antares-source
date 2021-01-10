package ch.scorpion.jabbah.base.mreact

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.*
import react.RBuilder
import react.RComponent
import react.RState
import react.useState
import styled.StyledHandler

@JsModule("@material-ui/core/Button")
@JsNonModule
private external val buttonModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val buttonComponent: RComponent<JMButtonProps, RState> = buttonModule.default

interface JMButtonProps : MButtonProps {
	var action: Action
}

/** Wraps a Jabbah [Action] in a React Material button.*/
fun RBuilder.jmButton(
	action: Action,
	color: MColor = MColor.default,
	variant: MButtonVariant? = null,
	size: MButtonSize = MButtonSize.medium,
	hRefOptions: HRefOptions? = null,
	addAsChild: Boolean = true,
	className: String? = null,
	handler: StyledHandler<JMButtonProps>? = null
) {
	//val (name, setName) = useState(action.name)

	createStyled(buttonComponent, addAsChild) {
		attrs.color = color
		hRefOptions?.let { setHRefTargetNoOpener(attrs, it) }
		attrs.size = size
		attrs.variant = variant
		attrs.onClick = { action.execute(ActionEvent("click", this@jmButton, 0, "click", 0)) }

		/*
		action.addPropertyChangeListener(object : PropertyChangeListener<Any> {
			override fun propertyChanged(e: PropertyChangeEvent<Any>) {
				when(e.name) {
					Action.PROP_DESCRIPTION -> setName(e.newValue as String)
				}
			}
		})
		*/

		childList.add(action.name)

		setStyledPropsAndRunHandler(className, handler)
	}
}
/*
= createStyled(buttonComponent, addAsChild) {
	attrs.color = color
	hRefOptions?.let { setHRefTargetNoOpener(attrs, it) }
	attrs.size = size
	attrs.variant = variant
	attrs.onClick = { action.execute(ActionEvent("click", this@jmButton, 0, "click", 0)) }


	childList.add(action.name)

	setStyledPropsAndRunHandler(className, handler)
}
*/