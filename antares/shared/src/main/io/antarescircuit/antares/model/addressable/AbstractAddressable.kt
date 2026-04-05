package io.antarescircuit.antares.model.addressable

import io.antarescircuit.antares.model.signal.BitOperation
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.vertice.AdjustableBitWidth
import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graph.model.vertice.CalculatingVertice
import io.antarescircuit.jabbah.graph.model.vertice.VerticeCalculator
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

abstract class AbstractAddressable<T : AddressableVertice>(
	calculator: VerticeCalculator<T>
) : CalculatingVertice(calculator), AddressableVertice, AdjustableBitWidth {

	companion object {
		fun validateDataBitWidth(memory: Memory, bitWidth: BitWidth) {
			memory.addressWithValueLargerThan(bitWidth.maxValue)?.let {
				throw IllegalArgumentException(Translations.getString("antares.memory.bitWidthTooSmallForData.msg", it))
			}
		}
	}

	private val dataListeners = mutableListOf<AddressableListener>()

	/** ---- [Addressable] interface */

	override val memory = Memory()

	override val data: ULong
		get() {
			val address = currentAddress
			if (address >= 0) {
				return memory.read(address)
			}
			return 0UL
		}

	override var addressWidth: BitWidth
		get() = getAddressInput().bitWidth
		set(value) {
			if (value != addressWidth) {
				val oldValue = addressWidth
				getAddressInput().bitWidth = value
				stateChanged()
				notifyBitWidthChanged(true, oldValue, value)
			}
		}

	override var dataWidth: BitWidth
		get() = getDataPort().bitWidth
		set(value) {
			if (value != dataWidth) {
				if (!isReading && value.width < dataWidth.width) {
					validateDataBitWidth(memory, value)
				}

				val oldValue = dataWidth
				updateDataPortBitWidth(value)
				stateChanged()
				notifyBitWidthChanged(false, oldValue, value)
			}
		}

	protected open fun updateDataPortBitWidth(bitWidth: BitWidth) {
		getDataPort().bitWidth = bitWidth
	}

	override var dataSource: String? = null

	override fun dataAt(address: Int): ULong = memory.read(address)

	override fun setDataAt(address: Int, value: ULong, signalHandler: SignalHandler?) {
		val oldValue = memory.read(address)
		memory.write(address, value)
		update()
		notifyDataChanged(address, oldValue, value)
		signalHandler?.requestActingAfter(this, propagationDelay.value, createActorData(null))
	}

	override fun commentAt(address: Int): String? = memory.readComment(address)

	override fun setCommentAt(address: Int, value: String?, signalHandler: SignalHandler?) {
		val oldValue = memory.readComment(address)
		memory.writeComment(address, value)
		update()
		notifyCommentChanged(address, oldValue, value)
	}

	override fun clear() {
		memory.clear()
		update()
	}

	override fun update() {
		stateChanged()
		notifyDataChanged(null, null, null)
	}

	override fun addListener(listener: AddressableListener) {
		if (!dataListeners.contains(listener)) {
			dataListeners.add(listener)
		}
	}

	override fun removeListener(listener: AddressableListener) {
		dataListeners.remove(listener)
	}

	/** ---- [AdjustableBitWidth] */

	override fun adjustBitWidth(portId: Int, bitWidth: BitWidth): Boolean {
		val port = getPort<DigitalSignal>(portId)
		if (port === getAddressInput()) {
			addressWidth = bitWidth
			return true
		} else if (port === getDataPort()) {
			dataWidth = bitWidth
			return true
		}
		return false
	}

	/** ---- [AbstractAddressable] */

	protected fun notifyDataChanged(address: Int?, oldValue: ULong?, newValue: ULong?) {
		val event = AddressableDataEvent(address, oldValue, newValue)
		dataListeners.forEach { it.dataChanged(event) }
	}

	private fun notifyCommentChanged(address: Int, oldValue: String?, newValue: String?) {
		val event = AddressableCommentEvent(address, oldValue, newValue)
		dataListeners.forEach { it.commentChanged(event) }
	}

	private fun notifyBitWidthChanged(isAddress: Boolean, oldValue: BitWidth, newValue: BitWidth) {
		val event = AddressableBitWidthEvent(isAddress, oldValue, newValue)
		dataListeners.forEach { it.bitWidthChanged(event) }
	}

	override fun validateDataBitWidth(bitWidth: BitWidth) {
		validateDataBitWidth(memory, bitWidth)
	}

	/** ---- [Storable] */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (StringUtils.isNotBlank(dataSource)) {
			writer.writeString("dataSource", dataSource!!)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("dataSource")) {
			dataSource = reader.readString("dataSource")
		}
	}

	/** ---- [AbstractAddressable] */

	/** Used for HDL export in templates (reflection).*/
	fun dataAsPaddedBinaryAt(address: Int): String =
		BitOperation.longToBinaryPadded(dataAt(address), dataWidth)
}