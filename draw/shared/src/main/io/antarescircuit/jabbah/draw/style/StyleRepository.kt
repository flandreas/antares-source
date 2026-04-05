package io.antarescircuit.jabbah.draw.style

import io.antarescircuit.jabbah.draw.graphics.PredefinedColorProvider
import io.antarescircuit.jabbah.draw.graphics.PredefinedColorRepository
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.draw.graphics.PredefinedStrokeProvider
import io.antarescircuit.jabbah.draw.graphics.PredefinedStrokeRepository

/**
 * Provides the registered [Style] of a [StyleType].
 */
interface StyleProvider {

    val predefinedColorProvider: PredefinedColorProvider

	val predefinedStrokeProvider: PredefinedStrokeProvider

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
     * Returns all registered [StyleType]s in the order of registration.
     */
    fun getStyleTypes(): List<StyleType>

	/** Returns the [StyleType] that can be chosen by the user, i.e. those whose property [StyleType.isSystem] is not set.*/
	fun getChoosableStyleTypes(): List<StyleType>
}

/**
 * An implementation of the [StyleProvider] interface that allows to register [Style]s.
 */
class StyleRepository(
	override val predefinedColorProvider: PredefinedColorProvider = PredefinedColorRepository,
	override val predefinedStrokeProvider: PredefinedStrokeProvider = PredefinedStrokeRepository
) : StyleProvider {

    companion object {
        var INSTANCE: StyleRepository = StyleRepository()
        private val LOG by logger(StyleRepository::class)
    }

    /** Maps the name of [StyleType] to the [StyleType] object.*/
    private val typeMap: MutableMap<String, StyleType> by lazy { mutableMapOf<String, StyleType>() }

	/** Maintains the order of [StyleType]s registrations.*/
	private val typeList: MutableList<StyleType> by lazy { mutableListOf<StyleType>() }

    /** Maps a [StyleType] to the [Style] to be used for that [StyleType].*/
    private val styleMap: MutableMap<StyleType, Style> by lazy { mutableMapOf<StyleType, Style>()}

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

    override fun getStyleTypes(): List<StyleType> {
	    return typeList
    }

	override fun getChoosableStyleTypes(): List<StyleType> {
		return getStyleTypes().filter { !it.isSystem }.toList()
	}

    /** ---- [StyleRepository] */

    /** Clears this [StyleRepository] by removing all registrations. Used mainly for testing purposes.*/
    fun clear() {
        typeMap.clear()
	    typeList.clear()
        styleMap.clear()
    }

    /**
     * Registers
     */
    fun registerStyleType(styleType: StyleType): StyleRepository {
	    typeMap[styleType.name] = styleType
	    typeList.add(styleType)
        return this
    }

    /**
     * Registers a [Style] to be used for all applications of the given [StyleType].
     * @return this to support method chaining
     */
    fun registerStyle(styleType: StyleType, style: Style): StyleRepository {
	    if (!typeMap.containsKey(styleType.name)) {
		    registerStyleType(styleType)
	    }
	    styleMap[styleType] = style
        return this
    }
}