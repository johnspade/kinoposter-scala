package ru.johnspade.kinoposter.movie

import cats.data.NonEmptyList
import cats.effect.Sync
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

private object MovieSql {

  def select(count: Int): Query0[Movie] =
    sql"""
         select id, name_ru, name_en, year, country, genre, description, director, actors, stills, trailer
         from good_movies order by random() limit $count;
    """.query[Movie]

  def setPosted(ids: NonEmptyList[Long]): Update0 =
    (fr"""update good_movies set posted = true where""" ++ Fragments.in(fr"id", ids)).update

}

class DoobieMovieRepositoryInterpreter[F[_] : Sync](val xa: Transactor[F]) extends MovieRepository[F] {

  override def getMovies(count: Int): F[NonEmptyList[Movie]] = MovieSql.select(count).nel.transact(xa)

  override def markPostedMovies(ids: NonEmptyList[Long]): F[Int] = MovieSql.setPosted(ids).run.transact(xa)

}
