package ch.scorpion.jabbah.base.event

/**
 * Required to work around the __doNotUseOrImplementIt problem in JS,
 * which Kotlin MPP produces for functional interfaces in JS.
 */
@JsExport
external interface PropertyChangeListenerJs<in T: Any> {
    fun propertyChanged(e: PropertyChangeEvent<T>)
}

@JsExport
fun <T: Any> mpListener(l: PropertyChangeListenerJs<T>): PropertyChangeListener<T> = object : PropertyChangeListener<T> {
    override fun propertyChanged(e: PropertyChangeEvent<T>) {
        l.propertyChanged(e)
    }
}