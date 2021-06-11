package ch.scorpion.jabbah.graph.model

interface MultiSignalSource<T> {

	val signalCount: Int

	fun getSignal(id: Int): T
}