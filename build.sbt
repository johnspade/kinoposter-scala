name := "kinoposter"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-Ypartial-unification",
  "-language:higherKinds"
)

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.6.0",
  "org.typelevel" %% "cats-effect" % "1.2.0",
  "org.tpolecat" %% "doobie-core" % "0.6.0",
  "org.tpolecat" %% "doobie-postgres" % "0.6.0",
  "com.github.pureconfig" %% "pureconfig" % "0.10.2",
  "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.10.2",
  "com.vk.api" % "sdk" % "0.5.12",
  "com.softwaremill.sttp" %% "core" % "1.5.16",
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "io.chrisdavenport" %% "log4cats-slf4j" % "0.3.0",
  "ch.qos.logback" % "logback-classic" % "1.1.10"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
