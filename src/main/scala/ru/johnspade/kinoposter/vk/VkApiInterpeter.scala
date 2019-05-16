package ru.johnspade.kinoposter.vk

import java.io.File

import cats.effect.Sync
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.client.{AbstractQueryBuilder, VkApiClient}
import com.vk.api.sdk.objects.wall.WallPostFull
import com.vk.api.sdk.queries.wall.WallGetFilter
import ru.johnspade.kinoposter.post.Post

import scala.collection.JavaConverters._

class VkApiInterpeter[F[_] : Sync](val client: VkApiClient, val actor: UserActor, val groupId: Int)
  extends VkApi[F] {

  override def getPosts(filter: WallGetFilter, count: Int): F[List[WallPostFull]] =
    Sync[F].delay(client.wall().get(actor).ownerId(-groupId).filter(filter).count(count).execute().getItems.asScala.toList)

  override def getWallUploadUrl: F[String] = Sync[F].delay(
    client.photos().getWallUploadServer(actor).groupId(groupId).execute().getUploadUrl
  )

  override def createPhotoAttachment(uploadUrl: String, file: File): F[String] = Sync[F].delay {
    val uploadResponse = client.upload().photoWall(uploadUrl, file).execute()
    client.photos().saveWallPhoto(actor, uploadResponse.getPhoto)
      .server(uploadResponse.getServer)
      .groupId(groupId)
      .hash(uploadResponse.getHash)
      .execute().asScala.map { p => s"photo${p.getOwnerId}_${p.getId}" }.head
  }

  override def post(posts: List[Post]): F[Unit] = Sync[F].delay {
    val queries: List[AbstractQueryBuilder[_, _]] = posts.map(p => client.wall().post(actor)
      .ownerId(-groupId)
      .message(p.message)
      .attachments(p.attachments.asJava)
      .publishDate(p.dateTime.getEpochSecond.toInt)
      .fromGroup(true))
    client.execute().batch(actor, queries.asJava).execute()
  }

  override def createVideoAttachment(link: String): F[String] = Sync[F].delay {
    val saveResponse = client.videos().save(actor).groupId(groupId).noComments(true).link(link).execute()
    client.upload().video(saveResponse.getUploadUrl, null).execute()
    s"video${saveResponse.getOwnerId}_${saveResponse.getVideoId}"
  }

}
