val scala3Version = "3.5.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "bookmanager",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
    "org.scalameta" %% "munit" % "1.0.0" % Test,
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl"          % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "org.atnos" %% "eff" % "7.0.5",
    "org.atnos" %% "eff-cats-effect" % "7.0.5",
    "org.scala-lang.modules" %% "scala-xml" % "2.1.0",
    "org.http4s" %% "http4s-blaze-client" % "1.0.0-M39",
    "org.http4s" %% "http4s-blaze-server" % "1.0.0-M39",
    "io.circe" %% "circe-generic" % "0.14.8",
    "io.circe" %% "circe-parser" % "0.14.10",
    "org.atnos" %% "eff" % "5.21.0",
    "org.typelevel" %% "cats-effect" % "3.5.6",
    "org.typelevel" %% "log4cats-slf4j"   % "2.7.0",
    "ch.qos.logback" % "logback-classic" % "1.5.12",
    "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    "com.typesafe.slick" %% "slick" % "3.5.1",
    "org.slf4j" % "slf4j-nop" % "1.7.26",
    "com.h2database" % "h2" % "1.4.200",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.5.1",
    "org.postgresql" % "postgresql" % "42.7.4",
    "com.typesafe" % "config" % "1.4.3"
    )
  )

val http4sVersion = "1.0.0-M40"



// to write types like Reader[String, *]
libraryDependencies ++= {
  if (scalaBinaryVersion.value == "3") {
    Nil
  } else {
    Seq(compilerPlugin("org.typelevel" %% "kind-projector" % "0.13.3" cross CrossVersion.full))
  }
}

scalacOptions ++= {
  if (scalaBinaryVersion.value == "3") {
    Seq("-Ykind-projector")
  } else {
    Nil
  }
}

// to get types like Reader[String, *] (with more than one type parameter) correctly inferred for scala 2.12.x
scalacOptions ++= {
  if (scalaBinaryVersion.value == "2.12") {
    Seq("-Ypartial-unification")
  } else {
    Nil
  }
}


scalacOptions ++= Seq("-Xmax-inlines", "64")

