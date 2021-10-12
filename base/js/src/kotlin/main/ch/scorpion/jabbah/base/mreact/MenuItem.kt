package ch.scorpion.jabbah.base.mreact

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import com.ccfraser.muirwik.components.menu.MMenuItemProps
import com.ccfraser.muirwik.components.menu.mMenuItem
import org.w3c.dom.events.Event
import react.*

interface JMMenuItemProps : MMenuItemProps {
	var action: Action
	var parentClickHandler: () -> Unit
}

fun RBuilder.jmMenuItem(handler: JMMenuItemProps.() -> Unit) {
	child(JabbahMaterialMenuItem::class) {
		this.attrs(handler)
	}
}

/** Wraps a Jabbah [Action] in a React Material menu item.*/
class JabbahMaterialMenuItem : RComponent<JMMenuItemProps, State>() {

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
		mMenuItem(
			primaryText = props.action.name,
			selected = props.action.selected,
			disabled = !props.action.enabled,
			onClick = { onClick((it))})
	}

	private fun onClick(event: Event) {
		props.parentClickHandler()
		props.action.execute(ActionEvent(event.toString(), this, 0, "click", 0))
	}
}