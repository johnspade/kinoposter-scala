package ru.johnspade.kinoposter.kp

trait KpClient[F[_]] {

  def getRating(id: Long): F[Rating]

}
