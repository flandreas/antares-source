package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.BeanProvider

val emptyApplicationDataBeanProvider: BeanProvider = { _, _ -> emptyList() }

var applicationDataBeanProvider: BeanProvider = emptyApplicationDataBeanProvider
