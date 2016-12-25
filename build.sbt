import play.sbt.PlayImport._
import sbt.Keys._
import sbt.Project.projectToRef

lazy val clients = Seq(client)
lazy val scalaV = "2.11.8"

lazy val server = (project in file("server")).settings(
  name := """playExample2""",
  version := "1.0-SNAPSHOT",
  scalaVersion := scalaV,
  scalaJSProjects := clients,
  pipelineStages := Seq(scalaJSProd, gzip),
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  libraryDependencies ++= Seq(
    "com.vmunier" %% "play-scalajs-scripts" % "0.5.0"
    ,"org.webjars" % "jquery" % "1.11.1"
    ,ws
    ,evolutions
    ,cache
    ,ws
    ,"com.typesafe.play" %% "play-slick" % "2.0.1"
    ,"com.typesafe.play" %% "play-slick-evolutions" % "2.0.1"
    ,"com.lihaoyi" %% "upickle" % "0.4.1"
    ,"com.github.japgolly.scalacss" %% "core" % "0.4.1"
    ,"com.h2database" % "h2" % "1.4.192"

    ,"org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
    ,specs2 % Test
  ),
  //loggers
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.16",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "ch.qos.logback" % "logback-core" % "1.1.7"
  )
).enablePlugins(PlayScala).
  aggregate(clients.map(projectToRef): _*).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  libraryDependencies ++= Seq(
    "com.github.japgolly.scalajs-react" %%% "core" % "0.11.3",
    "com.lihaoyi" %%% "upickle" % "0.4.3"
  ),
  jsDependencies ++= Seq(
    "org.webjars.bower" % "react" % "15.3.2"
      /        "react-with-addons.js"
      minified "react-with-addons.min.js"
      commonJSName "React",

    "org.webjars.bower" % "react" % "15.3.2"
      /         "react-dom.js"
      minified  "react-dom.min.js"
      dependsOn "react-with-addons.js"
      commonJSName "ReactDOM",

    "org.webjars.bower" % "react" % "15.3.2"
      /         "react-dom-server.js"
      minified  "react-dom-server.min.js"
      dependsOn "react-dom.js"
      commonJSName "ReactDOMServer"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.4.1"
    )
  ).jsConfigure(_ enablePlugins ScalaJSPlay)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value