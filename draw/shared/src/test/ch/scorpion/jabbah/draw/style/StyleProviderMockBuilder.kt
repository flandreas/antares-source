package ch.scorpion.jabbah.draw.style

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever

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