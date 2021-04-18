package ch.scorpion.jabbah.execution

interface ExecutionError {

	/**
	 * Returns the HTML formatted text to be displayed in the tooltip for the object from
	 * which this [ExecutionError] originates.
	 */
	val tooltipText: String

	fun reevaluate(signalHandler: SignalHandler)
}