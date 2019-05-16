package ru.johnspade.kinoposter.movie

trait MovieRepository[F[_]] {

  def getMovies(count: Int): F[List[Movie]]

  def markPostedMovies(ids: List[Long]): F[Unit]

}
