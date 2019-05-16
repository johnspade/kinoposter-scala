package ru.johnspade.kinoposter.image

import java.awt.image.{BufferedImage, RenderedImage}
import java.net.URL
import java.nio.file.{Files, Path}

import cats.effect.{Resource, Sync}
import javax.imageio.ImageIO

class ImageIoInterpreter[F[_] : Sync] extends ImageIo[F] {

  override def getImage(url: URL): F[Option[BufferedImage]] = Sync[F].delay(Option.apply(ImageIO.read(url)))

  override def saveImage(image: BufferedImage): Resource[F, Path] =
    Resource.make(Sync[F].delay {
      val path = Files.createTempFile("tmp", ".jpg")
      ImageIO.write(image.asInstanceOf[RenderedImage], "jpg", path.toFile)
      path
    }) { f =>
      Sync[F].delay(if (f.toFile.exists) Files.delete(f))
    }

}
