package ru.johnspade.kinoposter.movie

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

  def setPosted(ids: List[Long]): Update0 =
    sql"""
         update good_movies set posted = true where id = any(?);
    """.update

}

class DoobieMovieRepositoryInterpreter[F[_] : Sync](val xa: Transactor[F]) extends MovieRepository[F] {

  override def getMovies(count: Int): F[List[Movie]] = MovieSql.select(count).to[List].transact(xa)

  override def markPostedMovies(ids: List[Long]): F[Unit] = Sync[F].delay(MovieSql.setPosted(ids).run.transact(xa))

}
