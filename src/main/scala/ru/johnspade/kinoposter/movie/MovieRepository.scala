package ru.johnspade.kinoposter.movie
import cats.data.NonEmptyList

trait MovieRepository[F[_]] {

  def getMovies(count: Int): F[NonEmptyList[Movie]]

  def markPostedMovies(ids: NonEmptyList[Long]): F[Int]

}
