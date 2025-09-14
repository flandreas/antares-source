package ch.scorpion.antares.view.output

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.semantic.Semantic
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.param.GraphParamType
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import kotlin.reflect.KClass

object LightColorGraphParamType : GraphParamType<LightColor> {

    override val name: String = "lightColor"

    override val displayableName: String by lazy { Translations.getString("element.property.LEDColor.name") }

    override val valueClass: KClass<LightColor> get() = LightColor::class

    override fun toString(): String = displayableName

    override fun writeValue(name: String, value: LightColor, writer: StoreWriter) {
        writer.writeString(name, value.customName)
    }

    override fun readValue(name: String, reader: StoreReader): LightColor =
        LightColor.withName(reader.readString(name))

    override fun createValue(name: String, value: LightColor, semantic: Semantic?): GraphParamValue<LightColor> =
        GraphParamValue.create(name, this, value, semantic)

    override fun toDslValue(value: LightColor): Any = value.ordinal.toLong()

    override fun evaluateIn(graph: Graph, value: LightColor): LightColor = value
}