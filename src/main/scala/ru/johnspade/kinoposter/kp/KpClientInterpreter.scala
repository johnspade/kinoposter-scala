package ru.johnspade.kinoposter.kp

import cats.effect.Sync
import com.softwaremill.sttp._

import scala.xml.XML

class KpClientInterpreter[F[_] : Sync] extends KpClient[F] {

  private implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()

  override def getRating(id: Long): F[Rating] = Sync[F].delay(
    sttp.get(uri"https://rating.kinopoisk.ru/$id.xml").response(asString).send().body.map { s =>
      val xml = XML.loadString(s)
      val kpRating = (xml \ "kp_rating").head.text
      val imdbRating = (xml \ "imdb_rating").headOption.map(_.text)
      Rating(kpRating, imdbRating)
    }.toOption.get
  )

}
