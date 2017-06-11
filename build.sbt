import play.sbt.PlayImport._
import sbt.Keys._
import sbt.Project.projectToRef

val projVersion = "1.0-SNAPSHOT"

//lazy val clients = Seq(client)
lazy val scalaV = "2.11.8"
val upickleVersion = "0.4.3"

//conflictManager in ThisBuild := ConflictManager.strict

lazy val server = (project in file("server")).settings(
  name := """readtext""",
  version := projVersion,
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  libraryDependencies ++= Seq(
//    "com.vmunier" %% "scalajs-scripts" % "1.0.0"
//    ,"org.webjars" % "jquery" % "1.11.1"
    "org.webjars" % "font-awesome" % "4.7.0"
    ,"org.webjars" % "bootstrap" % "3.3.7-1"
//    ,ws
    ,evolutions
    ,cache
    ,"com.typesafe.play" %% "play-slick" % "2.1.0"
    ,"com.typesafe.play" %% "play-slick-evolutions" % "2.1.0"
    ,"com.lihaoyi" %% "upickle" % upickleVersion
    ,"com.github.japgolly.scalacss" %% "core" % "0.4.1"
    ,"com.h2database" % "h2" % "1.4.192"

    ,"org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
//    ,specs2 % Test
    ,"org.scalacheck" %% "scalacheck" % "1.12.6" % Test
//    ,"org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
  ),
  //loggers
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.25",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "ch.qos.logback" % "logback-core" % "1.2.3"
  )
).enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
//  .disablePlugins(PlayLayoutPlugin)
//  .aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJvm)

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, ScalaJSWeb)
  .settings(
  scalaVersion := scalaV,
//  persistLauncher := true,
//  persistLauncher in Test := false,
  libraryDependencies ++= Seq(
//    "com.github.japgolly.scalajs-react" %%% "core" % "0.11.3",
    "com.github.japgolly.scalajs-react" %%% "core" % "1.0.1",
    "com.lihaoyi" %%% "upickle" % upickleVersion,
//    "org.scalactic" %% "scalactic" % "3.0.1" % "test",
    "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
  )
  ,npmDependencies in Compile ++= Seq(
    "react" -> "15.5.4",
    "react-dom" -> "15.5.4"
  )
//  ,npmDependencies in Test ++= Seq(
//    "react" -> "15.5.4",
//    "react-dom" -> "15.5.4"
//  )
//  ,npmDevDependencies in Compile += "expose-loader" -> "0.7.1"
//  ,npmDevDependencies in Test += "expose-loader" -> "0.7.1"
  /*jsDependencies ++= Seq(
    "org.webjars.bower" % "react" % "15.5.4"
      /        "react-with-addons.js"
      minified "react-with-addons.min.js"
      commonJSName "React",

    "org.webjars.bower" % "react" % "15.5.4"
      /         "react-dom.js"
      minified  "react-dom.min.js"
      dependsOn "react-with-addons.js"
      commonJSName "ReactDOM",

    "org.webjars.bower" % "react" % "15.5.4"
      /         "react-dom-server.js"
      minified  "react-dom-server.min.js"
      dependsOn "react-dom.js"
      commonJSName "ReactDOMServer"
  )*/
//  ,scalaJSUseMainModuleInitializer := true
//  ,skip in packageJSDependencies := false
).dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % upickleVersion
    )
  ).jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

//requiresDOM := true

//scalaJSStage in Test := FastOptStage

//onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value

//resolvers += Resolver.sonatypeRepo("releases")
