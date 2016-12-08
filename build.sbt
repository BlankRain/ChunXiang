name := "chuanxiang"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies := Seq(

  "org.scala-lang" % "scala-library" % "2.11.8",

  "net.ruippeixotog" %% "scala-scraper" % "1.0.0",

   "com.typesafe.play" %% "play-json" %  "2.5.0",

//  "net.ruippeixotog" % "scala-scraper_2.11" % "1.2.0",

    "org.scalaj" %% "scalaj-http" % "2.3.0"
)

assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value
  cp filter {
    _.data.getName == "fr-third-8.0.jar"
  }
}

import com.github.retronym.SbtOneJar._

oneJarSettings