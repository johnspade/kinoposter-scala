package ru.johnspade.kinoposter.image

import java.awt.image.BufferedImage
import java.net.URL
import java.nio.file.Path

import cats.effect.Resource

trait ImageIo[F[_]] {

  def getImage(url: URL): F[Option[BufferedImage]]

  def saveImage(image: BufferedImage): Resource[F, Path]

}
