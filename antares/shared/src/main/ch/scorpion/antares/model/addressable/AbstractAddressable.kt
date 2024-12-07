package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

abstract class AbstractAddressable<T : AddressableVertice>(
	calculator: VerticeCalculator<T>
) : CalculatingVertice(calculator), AddressableVertice {

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
					Companion.validateDataBitWidth(memory, value)
				}

				val oldValue = dataWidth
				getDataPort().bitWidth = value
				stateChanged()
				notifyBitWidthChanged(false, oldValue, value)
			}
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
		notifyDataChanged(null, null, null)
	}

	override fun update() {
		stateChanged()
	}

	override fun addListener(listener: AddressableListener) {
		if (!dataListeners.contains(listener)) {
			dataListeners.add(listener)
		}
	}

	override fun removeListener(listener: AddressableListener) {
		dataListeners.remove(listener)
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
		Companion.validateDataBitWidth(memory, bitWidth)
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