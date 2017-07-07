package ch.scorpion.jabbah.draw.style

import ch.scorpion.jabbah.base.collection.toImmutableSet
import ch.scorpion.jabbah.base.exception.NoSuchElementException
import ch.scorpion.jabbah.draw.graphics.PredefinedColorProvider
import ch.scorpion.jabbah.draw.graphics.PredefinedColorRepository
import ch.scorpion.jabbah.base.logger

/**
 * Provides the registered [Style] of a [StyleType].
 */
interface StyleProvider {

    val predefinedColorProvider: PredefinedColorProvider

    /**
     * Provides the [Style] with the specified [StyleType].
     * @throws NoSuchElementException if no [Style] for [styleType] is available
     */
    fun getStyle(styleType: StyleType): Style

    /**
     * Provides the registered [StyleType] with a particular name.
     * @throws NoSuchElementException if no [StyleType] with name [name] is available
     */
    fun getStyleType(name: String): StyleType

    /**
     * Returns all registered [StyleType]s as a [Set].
     */
    fun getStyleTypes(): Set<StyleType>
}

/**
 * An implementation of the [StyleProvider] interface that allows to register [Style]s.
 */
class StyleRepository(override val predefinedColorProvider: PredefinedColorProvider) : StyleProvider {

    companion object {
        var INSTANCE: StyleRepository = StyleRepository()

        /** Returns a [StyleRepository] filled with the basic 'draw' [Style]s, mainly for testing purposes. */
        fun basic(): StyleRepository {
            return StyleRepository()
                .registerStyle(StyleType.BACKGROUND, BasicStyle())
                .registerStyle(StyleType.FIGURE, BasicStyle())
        }
    }

    constructor() : this(PredefinedColorRepository)

    val LOG by logger()

    /** Maps the name of [StyleType] to the [StyleType] object.*/
    val typeMap: MutableMap<String, StyleType> by lazy { mutableMapOf<String, StyleType>() }

    /** Maps a [StyleType] to the [Style] to be used for that [StyleType].*/
    val styleMap: MutableMap<StyleType, Style> by lazy { mutableMapOf<StyleType, Style>()}

    /** ---- [StyleProvider] interface */

    override fun getStyle(styleType: StyleType): Style {
        val style = styleMap[styleType]
        if (style == null) {
            LOG.error("No registered Style for StyleType '${styleType.name}'")
            throw NoSuchElementException("getStyle")
        }
        return style
    }

    override fun getStyleType(name: String): StyleType {
        val styleType = typeMap[name]
        if (styleType == null) {
            LOG.error("No registered StyleType '$name'")
            throw NoSuchElementException("getStyleType")
        }
        return styleType
    }

    override fun getStyleTypes(): Set<StyleType> {
        return styleMap.keys.toImmutableSet()
    }

    /** ---- [StyleRepository] */

    /** Clears this [StyleRepository] by removing all registrations. Used mainly for testing purposes.*/
    fun clear() {
        typeMap.clear()
        styleMap.clear()
    }

    /**
     * Registers
     */
    fun registerStyleType(styleType: StyleType): StyleRepository {
        if (typeMap.containsKey(styleType.name)) {
            LOG.warn("StyleType '${styleType.name}' already registered, will be replaced")
        }
        typeMap.put(styleType.name, styleType)
        return this
    }

    /**
     * Registers a [Style] to be used for all applications of the given [StyleType].
     * @return this to support method chaining
     */
    fun registerStyle(styleType: StyleType, style: Style): StyleRepository {
        if (styleMap.containsKey(styleType)) {
            LOG.warn("Style for StyleType '${styleType.name}' already registered, will be replaced")
        }
        registerStyleType(styleType)
        styleMap.put(styleType, style)
        return this
    }
}