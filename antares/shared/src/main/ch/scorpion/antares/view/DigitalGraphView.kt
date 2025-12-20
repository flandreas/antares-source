package ch.scorpion.antares.view

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.model.net.NetSignalApplierStrategy
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightEmitter
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.model.Size
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class DigitalGraphView(
	graph: DigitalGraph?,
	eventBus: EventBus = BaseModule.eventBus
) : GraphViewImpl(graph, eventBus) {

	@Suppress("unused") // Reflection
	constructor() : this(TranslatableText(Translations.getString("graph.name.unknown")))

	constructor(name: TranslatableText) : this(GraphModelModule.graphFactory.create(name, AntaresGraphTypes.Digital) as DigitalGraph)

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

	var defaultSignalRepresentation: DigitalSignalRepresentation? = null

	var defaultLogicGateSize: Size = Size.LARGE

	@Suppress("unused") // Reflection
	var netSignalApplierStrategy: NetSignalApplierStrategy
		get() = (graph as DigitalGraph).netSignalApplierStrategy
		set(value) {
			(graph as DigitalGraph).netSignalApplierStrategy = value
		}

	override val allowMultipleOutputsPerNet: Boolean get() =
		when (netSignalApplierStrategy) {
			NetSignalApplierStrategy.Conflict -> false
			NetSignalApplierStrategy.WiredOr -> true
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		defaultLightColor?.let { writer.writeString("lightColor", it.customName) }
		defaultSignalRepresentation?.let { writer.writeString("signalRepresentation", it.customName) }
		if (defaultLogicGateSize != Size.LARGE) {
			writer.writeString("logicGateSize", defaultLogicGateSize.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("lightColor")) {
			defaultLightColor = LightColor.withName(reader.readString("lightColor"))
		}
		if (reader.hasAttribute("signalRepresentation")) {
			defaultSignalRepresentation = DigitalSignalRepresentation.withName(reader.readString("signalRepresentation"))
		}
		if (reader.hasAttribute("logicGateSize")) {
			defaultLogicGateSize = Size.withName(reader.readString("logicGateSize"))
		}
	}
}

/** Posted on [EventBus] when the default [LightColor] of a [DigitalGraphView] as changed by the user.*/
data class DefaultLightColorEvent(val graphView: DigitalGraphView)