package ru.johnspade.kinoposter.post

import java.time.Instant

case class Post(message: String, attachments: List[String], dateTime: Instant)
