package ru.johnspade.kinoposter.post

import java.time.Instant

import cats.data.NonEmptyList
import ru.johnspade.kinoposter.movie.Movie

trait PostService[F[_]] {

  def createPosts(movies: NonEmptyList[Movie], start: Instant): F[List[Post]]

}
