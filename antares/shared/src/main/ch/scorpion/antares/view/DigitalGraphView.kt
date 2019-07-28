package ch.scorpion.antares.view

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.io.*

class DigitalGraphView<T : GraphElementView<*>>(
	graph: DigitalGraph?,
	storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
	eventBus: EventBus = BaseModule.eventBus
) : GraphViewImpl<T>(graph, storableCloner, eventBus) {

	constructor(): this(Translations.getString("graph.name.unknown"))
	constructor(name: String): this(GraphModelModule.graphFactory.invoke(name) as DigitalGraph)

	/**
	 * The [LightColor] to be used when adding [LightEmitter]s to this [DigitalGraphView],
	 * or `null` if the system default is to be used.
	 */
	var defaultLightColor: LightColor? = null

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		defaultLightColor?.let { writer.writeString("lightColor", it.customName) }
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("lightColor")) {
			defaultLightColor = LightColor.withName(reader.readString("lightColor"))
		}
	}
}