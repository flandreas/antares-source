package io.antarescircuit.jabbah.graph.model

/**
 * A combination of a [GraphInput] and a [GraphOutput].
 * @param T the type of signal
 */
interface BidirectionalGraphPort<T : Any> : GraphInput<T>, GraphOutput<T>