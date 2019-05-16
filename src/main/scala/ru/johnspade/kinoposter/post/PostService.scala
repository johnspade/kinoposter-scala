package ru.johnspade.kinoposter.post

import java.time.Instant

import ru.johnspade.kinoposter.movie.Movie

trait PostService[F[_]] {

  def createPosts(movies: List[Movie], start: Instant): F[List[Post]]

}
