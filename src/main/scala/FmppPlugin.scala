package fmpp

import sbt.{Def, Fork, *}
import Keys.*
import sbt.plugins.JvmPlugin

import java.io.File

object FmppPlugin extends AutoPlugin {

  override def requires: Plugins = JvmPlugin
  override def trigger = noTrigger

  override def projectSettings: Seq[Def.Setting[?]] = fmppSettings

  override def projectConfigurations: Seq[Configuration] = List(autoImport.Fmpp)


  object autoImport {
    val Fmpp = config("fmpp").hide

    val fmpp = TaskKey[Seq[File]]("fmpp", "Generate Scala sources from FMPP Scala Template")
    val fmppArgs = SettingKey[Seq[String]]("fmpp-args", "Extra command line parameters to FMPP.")
    val fmppMain = SettingKey[String]("fmpp-main", "FMPP main class.")
    val fmppSources =  SettingKey[Seq[String]]("fmpp-sources", "Sources type to be processed.")
    val fmppVersion =  SettingKey[String]("fmpp-version", "FMPP version.")

  }


  // TODO Add support for Compile/Test/...
  // https://github.com/sbt/sbt-xjc/blob/master/src/main/scala/com/github/retronym/sbtxjc/SbtXjcPlugin.scala
  //def fmppSettings0(config: Config) = Seq(sourceDirectory in fmpp in config := … , …) `
  //val fmppSettings = fmppSettings0(Compile)

  def fmppSettings: Seq[Setting[?]] = {
    import autoImport.*
    Seq[Setting[?]](
      fmppArgs := Seq("--ignore-temporary-files"),
      fmppMain := "fmpp.tools.CommandLine",
      fmppSources := Seq("scala", "java"),
      fmppVersion := "0.9.14",
      libraryDependencies += ("net.sourceforge.fmpp" % "fmpp" % fmppVersion.value % Fmpp),
      Fmpp / sourceDirectory := (Compile / sourceDirectory).value,
      Fmpp / scalaSource := ( Compile / sourceManaged).value,

      Fmpp / managedClasspath := Classpaths.managedJars(Fmpp, classpathTypes.value, update.value),

      fmpp := {
        val sourceDir = (Fmpp / sourceDirectory).value
        val sourceManaged =  (Fmpp / Keys.sourceManaged).value
        val cacheDirectory = streams.value.cacheDirectory
        val managedClasspath = (Fmpp / Keys.managedClasspath).value
        val fmppMain = (Fmpp / autoImport.fmppMain).value
        val fmppArgs = (Fmpp / autoImport.fmppArgs).value.toList

        (Fmpp / fmppSources).value.flatMap(x => {
          val input = sourceDir / x
          if (input.exists) {
            val output = sourceManaged / x
            val cached = FileFunction.cached(cacheDirectory / "fmpp" / x, FilesInfo.lastModified, FilesInfo.exists) {
              (in: Set[File]) => {
                IO.delete(output)
                Fork.java(
                  ForkOptions.apply(),
                  List[String](
                    "-cp", managedClasspath.map(_.data).mkString(File.pathSeparator), fmppMain,
                    "-S", input.toString, "-O", output.toString,
                    "--replace-extensions=fm, " + x,
                    "-M", "execute(**/*.fm), ignore(**/*)"
                  ) ::: fmppArgs
                )
                (output ** ("*." + x)).get.toSet
              }
            }
            cached((input ** "*.fm").get.toSet)
          } else Nil
        })
      },

      Compile / sourceGenerators += fmpp.taskValue
    )
  }

}
