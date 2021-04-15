package ch.scorpion.jabbah.graph.model.net

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import javax.swing.ButtonGroup
import javax.swing.JMenu
import javax.swing.JRadioButtonMenuItem

/** A [JMenu] containing [Action]s for all available [SignalConflictBehaviour]s.*/
class SignalConflictBehaviourMenu(
	holder: SignalConflictBehaviourHolder = GraphModelModule.signalConflictBehaviourHolder,
	eventBus: EventBus = BaseModule.eventBus
): JMenu(Translations.getString("graph.action.signalConflictBehaviour.name")) {

	init {
		val buttonGroup = ButtonGroup()
		SignalConflictBehaviour.values().forEach {
			add(it, buttonGroup, holder, eventBus)
		}
	}

	private fun add(behaviour: SignalConflictBehaviour, buttonGroup: ButtonGroup, holder: SignalConflictBehaviourHolder, eventBus: EventBus) {
		val action = SignalConflictBehaviourAction(behaviour, holder, eventBus)
		val menuItem = JRadioButtonMenuItem(ActionWrapperSwing(action))
		buttonGroup.add(menuItem)
		add(menuItem)
	}
}