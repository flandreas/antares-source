package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.dsl.ParserFactory
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.text.ScriptProperty

class ScriptPropertySwing(
	propertyName: String,
	baseKey: String,
	beanProvider: BeanProvider = componentBeanProvider,
	val parserFactory: ParserFactory = BaseModule.parserFactory
) : CommandPropertySwing<ScriptProperty>(
	propertyName,
	baseKey,
	ScriptProperty::class.java,
	beanProvider,
	interactive = true)