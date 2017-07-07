package ch.scorpion.antares.model.signal

/**
 * Utility methods for [DigitalSignal].
 */
object DigitalSignalUtil {

    /** Returns a [DigitalSignal] whose [Bit]s are all undefined.*/
    fun undefined(bitWidth: BitWidth): DigitalSignal {
        return Word.undefined(bitWidth)
    }
}