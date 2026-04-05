package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.dsl.DslParser
import io.antarescircuit.jabbah.base.dsl.ParserFactory
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty

/**
 * A [CommandPropertySwing] for script properties.
 * @param parserFactory creates the [DslParser] used in the "check" function, or `null` if "check" is not supported
 */
class ScriptPropertySwing(
	propertyName: String,
	baseKey: String,
	beanProvider: BeanProvider = componentBeanProvider,
	val parserFactory: ParserFactory? = BaseModule.parserFactory,
	val helpId: HelpId? = null
) : CommandPropertySwing<ScriptProperty>(
	propertyName,
	baseKey,
	ScriptProperty::class.java,
	beanProvider,
	interactive = true) {

	override fun isEditable(): Boolean = true
}