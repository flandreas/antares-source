package ch.scorpion.jabbah.draw.style

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

/**
 * A builder for mocks of [StyleProvider].
 */
class StyleProviderMockBuilder {

    private val styleProvider = mock<StyleProvider>()

    init {
        whenever(styleProvider.getStyleType(any())).thenReturn(StyleType.FIGURE)
        whenever(styleProvider.getStyle(any())).thenReturn(BasicStyle())
    }

    fun withStyleType(name: String, styleType: StyleType): StyleProviderMockBuilder {
        whenever(styleProvider.getStyleType(name)).thenReturn(styleType)
        return this
    }

    fun build() = styleProvider
}