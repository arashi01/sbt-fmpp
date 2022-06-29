name := "sbt-fmpp"

version := "0.3-SNAPSHOT"

organization := "com.github.sbt"

enablePlugins(SbtPlugin)

scriptedLaunchOpts :=  scriptedLaunchOpts.value ++ Seq("-Dplugin.version=" + version.value)

scriptedBufferLog := false
  
