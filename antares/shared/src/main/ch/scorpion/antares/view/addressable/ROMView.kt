package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.ROM
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

class ROMView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	eventBus: EventBus = BaseModule.eventBus,
	model: ROM = ROM()
) : AbstractAddressableView<ROM>(styleProvider, eventBus, model) {

	override fun modelExchanged(oldModel: ROM?) {
		super.modelExchanged(oldModel)

		val addressPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getAddressInput(),
			direction = Direction.WEST)
		addressPV.setLocation(addressPV.length, 0)
		addPortView(addressPV)

		val csPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getChipSelectInput(),
			direction = Direction.SOUTH)
		csPV.setLocation(csPV.length + MIN_WIDTH / 2, MIN_HEIGHT / 2)
		addPortView(csPV)

		val dataPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getDataPort(),
			direction = Direction.EAST)
		dataPV.setLocation(dataPV.length + MIN_WIDTH, 0)
		addPortView(dataPV)

		label.text = buildLabelText()
		contentsView = AddressableContentsView(
			addressable = model,
			rowsCount = contentRowsCount,
			columnsCount = contentColumnsCount,
			showDisassembler = showDisassembler,
			highlightCurrentCellWhenNotSelected = highlightCurrentCellWhenNotSelected)
	}

	init {
		modelExchanged(null)
		updateGeometry()
	}

	/** ---- UI properties */

	var disassemblerConfig: ScriptProperty
		get() = ScriptProperty(model.disassemblerConfig)
		set(value) {
			model.disassemblerConfig = value.script!!
		}

	var showDisassembler: Boolean
		get() = contentsView.showDisassembler
		set(value) {
			if (value != showDisassembler) {
				contentsView.showDisassembler = value
				updateGeometry()
				validate()
			}
		}

	@Suppress("unused") // Reflection
	var highlightCurrentCellWhenNotSelected: Boolean = true
		set(value) {
			if (field != value) {
				field = value
				contentsView.highlightCurrentCellWhenNotSelected = field
				validate()
			}
		}

	@Suppress("unused") // Reflection
	var loadDataSource: Boolean
		get() = model.loadDataSource
		set(value) {
			model.loadDataSource = value
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeBoolean("showDisassembler", showDisassembler)
		if (highlightCurrentCellWhenNotSelected) {
			writer.writeBoolean("highlightCurrentCell", highlightCurrentCellWhenNotSelected)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)

		if (reader.hasAttribute("showDisassembler")) {
			showDisassembler = reader.readBoolean("showDisassembler")
		}
		if (reader.hasAttribute("highlightCurrentCell")) {
			highlightCurrentCellWhenNotSelected = reader.readBoolean("highlightCurrentCell")
		}
	}

	/** ---- [AbstractAddressableView] */

	override fun updatePortViewPositions() {
		getPortView(model.getChipSelectInput())!!.setLocation(x + width / 2, height / 2)
	}
}