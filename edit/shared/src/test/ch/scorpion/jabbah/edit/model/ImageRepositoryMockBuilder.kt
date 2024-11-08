package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.draw.graphics.Image
import ch.scorpion.jabbah.edit.model.image.ImageData
import ch.scorpion.jabbah.edit.model.image.ImageRepository
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
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