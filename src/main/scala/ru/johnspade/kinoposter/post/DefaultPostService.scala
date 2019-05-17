package ru.johnspade.kinoposter.post

import java.awt.image.BufferedImage
import java.net.URL
import java.time.Instant
import java.time.temporal.ChronoUnit

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits._
import ru.johnspade.kinoposter.KinoposterApp.toMoscowDateTime
import ru.johnspade.kinoposter.image.ImageIo
import ru.johnspade.kinoposter.kp.KpClient
import ru.johnspade.kinoposter.movie.Movie
import ru.johnspade.kinoposter.vk.VkApi

class DefaultPostService[F[_] : Sync](vk: VkApi[F], kp: KpClient[F], imageIo: ImageIo[F]) extends PostService[F] {

  override def createPosts(movies: NonEmptyList[Movie], start: Instant): F[List[Post]] =
    movies.traverseWithIndexM((m, i) => createPost(m, start, i)).map(_.toList)

  private val intervals = List(6, 12, 6)

  private def getPostInstant(start: Instant, number: Int): Instant = {
    val lastHour = toMoscowDateTime(start).getHour
    val startInterval = lastHour match {
      case h if 0 to 8 contains h => (14, 0)
      case h if 9 to 14 contains h => (20, 1)
      case _ => (32, 2)
    }
    val hoursToAdd = List.tabulate(number)(i => intervals((i + startInterval._2) % intervals.size)).sum
    start.plus(startInterval._1 - lastHour + hoursToAdd, ChronoUnit.HOURS)
  }

  private def createCoverLink(id: Long): String = s"https://st.kp.yandex.net/images/film_big/$id.jpg"

  private def createPhotoAttachmentFromImage(img: BufferedImage, uploadUrl: String): F[String] = imageIo.saveImage(img)
    .use(p => vk.createPhotoAttachment(uploadUrl, p.toFile))

  private def getImages(stills: List[String]): F[List[BufferedImage]] =
    stills.map(s => imageIo.getImage(new URL(s))).sequence.map(l => l.flatten)

  private def createPhotoAttachments(stills: List[String], uploadUrl: String): F[List[String]] =
    getImages(stills).flatMap(l => l.map(i => createPhotoAttachmentFromImage(i, uploadUrl)).sequence)

  private def createAttachments(movie: Movie, uploadUrl: String): F[List[String]] = {
    val link = s"https://www.kinopoisk.ru/film/${movie.id}"
    val photos = createPhotoAttachments(createCoverLink(movie.id) :: movie.stills.take(4), uploadUrl)
    photos.map(link :: _)
  }

  private def createPost(movie: Movie, start: Instant, number: Int): F[Post] = for {
    rating <- kp.getRating(movie.id)
    message =
    s"""
       |${movie.nameRu} ${movie.nameEn.map(n => s"($n)").getOrElse("")}
       |
       |Жанр: ${movie.genre.split(", ").map(g => s"#$g").mkString(", ")}
       |Год: ${movie.year}
       |Страна: ${movie.country}
       |Режиссер: ${movie.director}
       |В главных ролях: ${movie.actors.split(", ").take(4).mkString(", ")}
       |Оценка на Кинопоиске: ${rating.kp_rating}${rating.imdb_rating.map(r => s"\nОценка на IMDb: $r").getOrElse("")}
       |
       |${movie.description}
    """.stripMargin
    wallUploadUrl <- vk.getWallUploadUrl
    attachments <- createAttachments(movie, wallUploadUrl)
  } yield Post(message, attachments, getPostInstant(start, number))

}
