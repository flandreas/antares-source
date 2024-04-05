package ch.scorpion.jabbah.base.time

/**
 * Implemented by classes that want to explicitly confirm [SystemSpeed] changes requested by the user.
 *
 * This can be useful if certain state changes in other parts of the system have to be performed
 * before the system can be allowed to change its [SystemSpeed], e.g. stop running animation
 * that are only needed with certain [SystemSpeed] categories.
 */
interface SystemSpeedConfirmer {

    /**
     * Asks this [SystemSpeedConfirmer] to confirm a [SystemSpeedEvent].
     * [SystemSpeed] wont commit the [SystemSpeedEvent] until it has been confirmed,
     * which can be done asynchronously.
     *
     * Confirmation is done by calling [SystemSpeed.commit]
     */
    fun confirmSystemSpeedEvent(event: SystemSpeedEvent)
}