package ru.johnspade.kinoposter.vk

import java.io.File

import com.vk.api.sdk.objects.wall.WallPostFull
import com.vk.api.sdk.queries.wall.WallGetFilter
import ru.johnspade.kinoposter.post.Post

trait VkApi[F[_]] {

  def getPosts(filter: WallGetFilter, count: Int): F[List[WallPostFull]]

  def getWallUploadUrl: F[String]

  def createPhotoAttachment(uploadUrl: String, file: File): F[String]

  def post(posts: List[Post]): F[Unit]

  def createVideoAttachment(link: String): F[String]

}
