package ch.scorpion.antares.view

import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class DigitalGraphView(
	graph: DigitalGraph?,
	eventBus: EventBus = BaseModule.eventBus
) : GraphViewImpl(graph, eventBus) {

	constructor() : this(Translations.getString("graph.name.unknown"))
	constructor(name: String) : this(GraphModelModule.graphFactory.invoke(name) as DigitalGraph)

	/**
	 * The [LightColor] to be used when adding [LightEmitter]s to this [DigitalGraphView],
	 * or `null` if the system default is to be used. Posts a [DefaultLightColorEvent] on this
	 * [DigitalGraphView]'s [EventBus] when changed by the user.
	 */
	var defaultLightColor: LightColor? = null
		set(value) {
			if (field != value) {
				field = value
				if (!isReading) {
					System.invokeLater { eventBus.post(DefaultLightColorEvent(this)) }
				}
			}
		}

	private var isReading = false

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		defaultLightColor?.let { writer.writeString("lightColor", it.customName) }
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		isReading = true
		if (reader.hasAttribute("lightColor")) {
			defaultLightColor = LightColor.withName(reader.readString("lightColor"))
		}
		isReading = false
	}
}

/** Posted on [EventBus] when the default [LightColor] of a [DigitalGraphView] as changed by the user.*/
data class DefaultLightColorEvent(val graphView: DigitalGraphView)