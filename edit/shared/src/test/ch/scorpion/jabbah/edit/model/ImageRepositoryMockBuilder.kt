package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.draw.graphics.Image
import ch.scorpion.jabbah.edit.model.image.ImageData
import ch.scorpion.jabbah.edit.model.image.ImageRepository
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import io.mockk.every
import io.mockk.mockk

class ImageRepositoryMockBuilder {

    private val imageRepository = mockk<ImageRepository>()

    fun withImage(image: Image): ImageRepositoryMockBuilder {
        every { imageRepository.getImage(any()) } returns ImageData(image, Name(TranslatableText()))
        return this
    }

    fun withImageOfSize(width: Int, height: Int): ImageRepositoryMockBuilder {
        val image = mockk<Image>()
        every { image.width } returns width
        every { image.height } returns height
        withImage(image)
        return this
    }

    fun build(): ImageRepository = imageRepository
}