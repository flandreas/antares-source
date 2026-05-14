package io.antarescircuit.jabbah.edit.properties.magnitude

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

enum class Magnitude(
    val customName: String,
    val factor: Long,
    val inverse: Boolean = false,
    val caseSensitive: Boolean = false,
    val denotation: String = customName
) {
    Pico("p", 1_000_000_000_000, inverse = true), // 1.0E-12
    Nano("n", 1_000_000_000, inverse = true, denotation = "n"), // 1.0E-9
    Micro("u", 1_000_000, inverse = true, denotation = "µ"), // 1.0E-6
    Milli("m", 1_000, inverse = true, caseSensitive = true), // 1.0E-3
    One("", 1),
    Kilo("K", 1_000), // 1.0E3
    Mega("M", 1_000_000, caseSensitive = true), // 1.0E6
    Giga("G", 1_000_000_000), // 1.0E9
    Tera("T", 1_000_000_000_000); // 1.0E12

    companion object {

        fun withCustomName(name: String): Magnitude =
            entries.firstOrNull { it.customName == name }
                ?: throw IllegalArgumentException(Translations.getString("edit.magnitude.unknown.text", name))

        fun read(name: String, reader: StoreReader): Magnitude =
            if (reader.hasAttribute(name)) {
                withCustomName(reader.readString(name))
            } else {
                One
            }
    }

    fun write(name: String, writer: StoreWriter) {
        if (this != One) {
            writer.writeString(name, customName)
        }
    }

    fun matchText(text: String): Boolean =
        if (caseSensitive) {
            denotation == text || customName == text
        } else {
            denotation.equals(text, ignoreCase = true) || customName.equals(text, ignoreCase = true)
        }
}