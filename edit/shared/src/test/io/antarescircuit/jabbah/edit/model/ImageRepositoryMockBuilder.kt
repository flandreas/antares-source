package io.antarescircuit.jabbah.edit.model

import io.antarescircuit.jabbah.draw.graphics.Image
import io.antarescircuit.jabbah.edit.model.image.ImageData
import io.antarescircuit.jabbah.edit.model.image.ImageRepository
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.description.Name
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.matcher.any

class ImageRepositoryMockBuilder {

    private val imageRepository = mock<ImageRepository>()

    fun withImage(image: Image): ImageRepositoryMockBuilder {
        every { imageRepository.getImage(any()) } returns ImageData(image, Name(TranslatableText()))
        return this
    }

    fun withImageOfSize(width: Int, height: Int): ImageRepositoryMockBuilder {
        val image = mock<Image>()
        every { image.width } returns width
        every { image.height } returns height
        withImage(image)
        return this
    }

    fun build(): ImageRepository = imageRepository
}