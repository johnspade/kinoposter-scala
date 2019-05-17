package ru.johnspade.kinoposter

import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.queries.wall.WallGetFilter
import doobie._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import pureconfig.generic.auto._
import pureconfig.module.catseffect._
import ru.johnspade.kinoposter.image.{ImageIo, ImageIoInterpreter}
import ru.johnspade.kinoposter.kp.{KpClient, KpClientInterpreter}
import ru.johnspade.kinoposter.movie.{DoobieMovieRepositoryInterpreter, MovieRepository}
import ru.johnspade.kinoposter.post.DefaultPostService
import ru.johnspade.kinoposter.vk.{VkApi, VkApiInterpeter}

import scala.concurrent.ExecutionContext

object KinoposterApp extends IOApp {

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
  private val moscowZoneId = ZoneId.of("Europe/Moscow")
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  private val kp: KpClient[IO] = new KpClientInterpreter[IO]
  private val imageIo: ImageIo[IO] = new ImageIoInterpreter[IO]

  private def calcStart[F[_] : Sync](vk: VkApi[F]): F[Instant] = for {
    postponed <- vk.getPosts(WallGetFilter.POSTPONED, 100)
    published <- vk.getPosts(WallGetFilter.OWNER, 1)
    currentT <- Sync[F].delay {
      val now = LocalDateTime.now(moscowZoneId).withMinute(0).withSecond(0)
      now.toInstant(moscowZoneId.getRules.getOffset(now)).getEpochSecond
    }
    cutoff = List(
      currentT,
      postponed.lastOption.map(_.getDate.toLong).getOrElse(currentT),
      published.headOption.map(_.getDate.toLong).getOrElse(currentT)
    ).max
  } yield Instant.ofEpochSecond(cutoff)

  def toMoscowDateTime(instant: Instant): LocalDateTime = LocalDateTime.ofInstant(instant, moscowZoneId)

  override def run(args: List[String]): IO[ExitCode] = for {
    logger <- Slf4jLogger.create[IO]
    conf <- loadConfigFromFilesF[IO, Config](NonEmptyList.one(Paths.get("config.properties")))
    xa = Transactor.fromDriverManager[IO]("org.postgresql.Driver", s"jdbc:postgresql:${conf.app.database}", null, null)
    repo: MovieRepository[IO] = new DoobieMovieRepositoryInterpreter(xa)
    moviesCount = if (conf.app.moviesCount < 1) 1 else conf.app.moviesCount
    _ <- logger.info(s"Заданное количество фильмов: $moviesCount")
    movies <- repo.getMovies(moviesCount)
    _ <- logger.info(s"Выбранные фильмы: ${movies.map(m => s"${m.nameRu} (${m.id})").toList.mkString(", ")}")
    vk = new VkApiInterpeter[IO](
      new VkApiClient(new HttpTransportClient()),
      new UserActor(conf.vk.userId, conf.vk.accessToken),
      conf.vk.groupId
    )
    postService = new DefaultPostService[IO](vk, kp, imageIo)
    start <- calcStart[IO](vk)
    posts <- postService.createPosts(movies, start)
    _ <- logger.info(
      "Посты для публикации:\n" +
        posts.map(p =>
          s"Дата публикации: ${toMoscowDateTime(p.dateTime).format(dateTimeFormatter)}, текст:\n${p.message}"
        ).mkString("\n")
    )
    _ <- vk.post(posts)
    _ <- repo.markPostedMovies(movies.map(_.id))
    _ <- logger.info("Фильмы отмечены как опубликованные")
  } yield ExitCode.Success

}
