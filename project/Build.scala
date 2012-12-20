import sbt._
import Keys._

object FmppBuild extends Build {
  lazy val project = Project(
    id = "root", 
    base = file("."),
    settings = Defaults.defaultSettings ++ Seq(
      publishTo <<= (version) { version: String =>
         val scalasbt = "http://repo.scala-sbt.org/scalasbt/"
         val (name, url) = if (version.contains("-SNAPSHOT"))
           ("sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
         else
           ("sbt-plugin-releases", scalasbt+"sbt-plugin-releases")
         Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
      },
      publishMavenStyle := false
    )
  )
}
