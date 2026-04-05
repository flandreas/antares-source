package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.edit.BeanProvider

val emptyApplicationDataBeanProvider: BeanProvider = { _, _ -> emptyList() }

var applicationDataBeanProvider: BeanProvider = emptyApplicationDataBeanProvider
