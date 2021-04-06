package ch.scorpion.jabbah.graph.model.net

/**
 * Converts a signal.
 */
interface SignalConverter<T : Any> {
	fun convert(signal: T?): T?
}
