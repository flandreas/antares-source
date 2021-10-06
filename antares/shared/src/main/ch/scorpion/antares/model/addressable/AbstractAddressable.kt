package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.graph.model.vertice.CalculatingVertice
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator

abstract class AbstractAddressable<T : Addressable>(
	calculator: VerticeCalculator<T>
) : CalculatingVertice(calculator), Addressable {

	private val dataListeners = mutableListOf<AddressableDataListener>()

	override fun addDataListener(listener: AddressableDataListener) {
		if (!dataListeners.contains(listener)) {
			dataListeners.add(listener)
		}
	}

	override fun removeDataListener(listener: AddressableDataListener) {
		dataListeners.remove(listener)
	}

	protected fun notifyDataChanged(address: Int?, oldValue: ULong?, newValue: ULong?) {
		val event = AddressableDataEvent(address, oldValue, newValue)
		dataListeners.forEach { it.dataChanged(event) }
	}
}