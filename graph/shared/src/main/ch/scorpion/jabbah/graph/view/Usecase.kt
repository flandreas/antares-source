package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Describable
import ch.scorpion.jabbah.edit.model.text.description.Namable

/**
 * A [Usecase] is a representation of a single way the user can use a [GraphView].
 * The author of a [GraphView] programs the individual interactions that should occur with the [GraphView]
 * while a [Usecase] is executed. The user can then start the execution of a [Usecase] and observe
 * how the [GraphView] behaves. During [Usecase] execution, the user cannot interact with the [GraphView].
 * <p>
 * In addition, a [Usecase] can contain assertion code that checks certain conditions after execution of
 * a [Usecase], which can be used to automatically testing a [GraphView].
 */
interface Usecase : Namable, Describable, Storable {

	/** Returns the identification of this [Usecase] that is unique within a [GraphView].*/
	var id: Int

	/** The JavaScript script to be executed when this [Usecase] is executed.*/
	var executionScript: String

	fun dispose()
}