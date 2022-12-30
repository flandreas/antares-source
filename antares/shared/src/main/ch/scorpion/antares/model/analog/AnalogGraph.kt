package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.graph.GraphImpl

class AnalogGraph(
	name: String = Translations.getString("graph.name.unknown"),
	eventBus: EventBus = BaseModule.eventBus
) : GraphImpl(name, eventBus)