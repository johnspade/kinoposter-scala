package ru.johnspade.kinoposter

case class AppConfig(database: String, moviesCount: Int)

case class VkConfig(groupId: Int, accessToken: String, userId: Int)

case class Config(app: AppConfig, vk: VkConfig)
