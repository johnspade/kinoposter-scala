package ru.johnspade.kinoposter.movie

case class Movie(id: Long, nameRu: String, nameEn: Option[String], year: Int, country: String, genre: String,
                 description: String, director: String, actors: String, stills: List[String], trailer: Option[String])
