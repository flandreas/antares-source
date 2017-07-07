package ch.scorpion.jabbah.draw

/** Listens for [DrawableEvent]s from a [Drawable]. */
interface DrawableListener {

    /**
     * Gets called whenever a [Drawable] has been invaliated, i.e. whenever it has changed its
     * graphical representation and should therefore be repainted.
     */
    fun drawableInvalidated(event: DrawableEvent)

    /**
     * Gets called whenever updates and invalidations of a [Drawable] have been finished, and any
     * that renders the listened [Drawable] can be repainted.
     */
    fun drawableRequestRedraw(event: DrawableEvent)

    /**
     * Gets called whenever a [Drawable] has been updated, i.e. whenever it has changed its geometrical
     * properties. A call to this method is always followed by a call to [drawableInvalidated].
     */
    fun drawableUpdated(event: DrawableEvent)
}

/**
 * An empty implementation of the [DrawableListener] interface intended to be subclassed by
 * classes that only need to implement a subset of the [DrawableListener] interface
 */
open class DrawableAdapter : DrawableListener {
    override fun drawableInvalidated(event: DrawableEvent) {}
    override fun drawableRequestRedraw(event: DrawableEvent) {}
    override fun drawableUpdated(event: DrawableEvent) {}
}