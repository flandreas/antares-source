package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.signal.BitOperation
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

abstract class AbstractAddressable<T : AddressableVertice>(
	calculator: VerticeCalculator<T>
) : CalculatingVertice(calculator), AddressableVertice {

	private val dataListeners = mutableListOf<AddressableDataListener>()

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
			getAddressInput().bitWidth = value
			stateChanged()
		}

	override var dataWidth: BitWidth
		get() = getDataPort().bitWidth
		set(value) {
			getDataPort().bitWidth = value
			stateChanged()
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

	override fun addDataListener(listener: AddressableDataListener) {
		if (!dataListeners.contains(listener)) {
			dataListeners.add(listener)
		}
	}

	override fun removeDataListener(listener: AddressableDataListener) {
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