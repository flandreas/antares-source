package ch.scorpion.jabbah.graph.model

/**
 * Utility methods for handling signals.
 */
object SignalUtil {

    /**
     * Decides whether two signals are equal, while both signals can be {@code null}.
     * @param a the first signal
     * @param b the second signal.
     * @return `true` if both signals are equal.
     */
    fun equals(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) {
            return true;
        }
        return b == a
    }

	fun differ(a: Any?, b: Any?): Boolean = !SignalUtil.equals(a, b)
}