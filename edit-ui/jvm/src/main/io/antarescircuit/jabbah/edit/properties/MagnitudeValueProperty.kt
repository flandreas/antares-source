package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit

class MagnitudeValueProperty(
    propertyName: String,
    baseKey: String,
    beanProvider: BeanProvider = componentBeanProvider,
    vararg units: SIUnit
) : CommandPropertySwing<MagnitudeValue>(
    propertyName,
    baseKey,
    MagnitudeValue::class.java,
    beanProvider
) {
    val units: Array<SIUnit> = arrayOf(*units)

    var parseException: IllegalArgumentException? = null

    override fun writeToBeans(force: Boolean) {
        parseException?.let { throw it }
        super.writeToBeans(force)
    }

    override fun readFromObject(bean: Any?) {
        super.readFromObject(bean)
        parseException = null
    }
}