package io.antarescircuit.jabbah.graph.model.param

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.semantic.Semantic
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.reflect.KClass

object StringGraphParamType : GraphParamType<String> {

    override val name: String = "String"

    override val displayableName: String by lazy { Translations.getString("graph.paramType.string.name") }

    override val valueClass: KClass<String>
        get() = String::class

    override fun toString(): String = displayableName

    override fun createValue(name: String, value: String, semantic: Semantic?): GraphParamValue<String> =
        GraphParamValue.create(name, this, value, semantic)

    override fun writeValue(name: String, value: String, writer: StoreWriter) {
        writer.writeString(name, value)
    }

    override fun readValue(name: String, reader: StoreReader): String =
        reader.readString(name)

    override fun toDslValue(value: String): Any = value

    override fun evaluateIn(graph: Graph, value: String): String = value
}