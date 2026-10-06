package io.antarescircuit.jabbah.app.tip

import io.antarescircuit.jabbah.app.module.AppModule

/**
 * Provides [TipOfTheDay]s from persistent storage.
 *
 * Implementations can also keep track of the last displayed tip, so that the next call to [getNextTip] will return
 * the next tip after the last one.
 */
interface TipOfTheDayProvider {

    /**
     * The persistent property that determines whether tips are shown when the application is started.
     */
    var showOnStartup: Boolean

    /**
     * Resets the ID of the last displayed tip.
     * The next call to [getNextTip] will return the first tip.
     */
    fun resetCurrentId()

    /**
     * Returns `true` if there is a next tip after the last one displayed to the user.
     */
    fun hasNextTip(): Boolean

    /**
     * Returns the next [TipOfTheDay] the user has not seen yet.
     * This call assumes that the tip is displayed to the user, and therefore the persistent "last ID" is incremented.
     */
    fun getNextTip(): TipOfTheDay?

    /**
     * Returns `true`if there is a next tip after the one with the given id.
     */
    fun hasNextTip(currentId: Int): Boolean

    /**
     * Returns the next [TipOfTheDay] after the one with the given id.
     * This call assumes that the tip is displayed to the user, and therefore the persistent "last ID" is incremented.
     */
    fun getNextTip(currentId: Int): TipOfTheDay?
}

/**
 * Dummy implementation of [TipOfTheDayProvider] that always returns `null`.
 * Used for initialization in the [AppModule] configuration.
 */
class NopTipOfTheDayProvider : TipOfTheDayProvider {

    override var showOnStartup: Boolean = false

    override fun resetCurrentId() {}
    override fun hasNextTip(): Boolean = false
    override fun getNextTip(): TipOfTheDay? = null
    override fun hasNextTip(currentId: Int): Boolean = false
    override fun getNextTip(currentId: Int): TipOfTheDay? = null
}