package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.RAM
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider

class RAMView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	eventBus: EventBus = BaseModule.eventBus,
	model: RAM = RAM()
) : AbstractAddressableView<RAM>(styleProvider, eventBus, model) {

	companion object {
		const val CLOCK_PORT_X_FACTOR = 6
		const val CS_PORT_X_FACTOR = 10
		const val WRITE_PORT_X_FACTOR = 14
		const val CLEAR_PORT_X_FACTOR = 18
	}

	override fun modelExchanged(oldModel: RAM?) {
		super.modelExchanged(oldModel)

		val addressPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getAddressInput(),
			direction = Direction.WEST)
		addressPV.setLocation(addressPV.length, 0)
		addPortView(addressPV)

		val dataPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getDataPort(),
			direction = Direction.EAST)
		dataPV.setLocation(dataPV.length + MIN_WIDTH, 0)
		addPortView(dataPV)

		if (model.hasClock) {
			val clockPV = DigitalPortView(
				styleProvider = styleProvider,
				port = model.getClockInput()!!,
				direction = Direction.SOUTH)
			clockPV.setLocation(clockPV.length + CLOCK_PORT_X_FACTOR * Look.GRID, MIN_HEIGHT / 2)
			addPortView(clockPV)
		}

		val csPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getChipSelectInput(),
			direction = Direction.SOUTH)
		csPV.setLocation(csPV.length + CS_PORT_X_FACTOR * Look.GRID, MIN_HEIGHT / 2)
		addPortView(csPV)

		val writePV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getWriteInput(),
			direction = Direction.SOUTH)
		writePV.setLocation(writePV.length + WRITE_PORT_X_FACTOR * Look.GRID, MIN_HEIGHT / 2)
		addPortView(writePV)

		val clearPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getClearInput(),
			direction = Direction.SOUTH)
		clearPV.setLocation(clearPV.length + CLEAR_PORT_X_FACTOR * Look.GRID, MIN_HEIGHT / 2)
		addPortView(clearPV)

		label.text = buildLabelText()
		contentsView = AddressableContentsView(
			addressable = model,
			rowsCount = contentRowsCount,
			columnsCount = contentColumnsCount,
			showDisassembler = false)
	}

	init {
		modelExchanged(null)
		updateGeometry()
	}

	/** ---- UI properties */

	@Suppress("unused")
	var hasClock: Boolean
		get() = model.hasClock
		set(value) {
			invalidate()
			model.hasClock = value
			modelExchanged(model)
			updateGeometry()
			invalidate()
			validate()
		}

	@Suppress("unused")
	var nonVolatile: Boolean
		get() = model.nonVolatile
		set(value) {
			model.nonVolatile = value
		}

	/** ---- [AbstractAddressableView] */

	override fun updatePortViewPositions() {
		if (model.hasClock) {
			getPortView(model.getClockInput()!!)!!.setLocation(x + CLOCK_PORT_X_FACTOR * Look.GRID.toDouble(), height / 2)
		}
		getPortView(model.getChipSelectInput())!!.setLocation(x + CS_PORT_X_FACTOR * Look.GRID.toDouble(), height / 2)
		getPortView(model.getWriteInput())!!.setLocation(x + WRITE_PORT_X_FACTOR * Look.GRID.toDouble(), height / 2)
		getPortView(model.getClearInput())!!.setLocation(x + CLEAR_PORT_X_FACTOR * Look.GRID.toDouble(), height / 2)
	}
}