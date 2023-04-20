package ch.scorpion.antares.model.analog

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.graph.GraphImpl
import ch.scorpion.jabbah.graph.view.GraphView

class AnalogGraph(
	name: TranslatableText = TranslatableText(Translations.getString("graph.name.unknown")),
	eventBus: EventBus = BaseModule.eventBus
) : GraphImpl(name, AntaresGraphTypes.Analog, eventBus) {

	override fun executionStart(signalHandler: SignalHandler, graphView: GraphView?) {
		super.executionStart(signalHandler, graphView)
		(graphView as AnalogGraphView?)?.requestActing(signalHandler)
	}
}