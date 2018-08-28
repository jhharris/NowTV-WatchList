import sbtbuildinfo.BuildInfoKeys.{buildInfoObject, buildInfoOptions, buildInfoPackage}
import sbtbuildinfo.BuildInfoOption

lazy val akkaHttpVersion = "10.1.4"
lazy val akkaVersion    = "2.5.15"
lazy val scalaLoggingVersion = "3.9.0"
lazy val logbackVersion = "1.2.3"

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    inThisBuild(List(
      organization    := "net.zeotrope",
      scalaVersion    := "2.12.6",
      sbtVersion      := "1.1.6"
    )),
    name := "nowtv-watchlist",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"           % akkaVersion,

      "com.typesafe.scala-logging" %% "scala-logging"       % scalaLoggingVersion,
      "ch.qos.logback" % "logback-classic"                  % logbackVersion,


      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
    ),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "net.zeotrope",
    buildInfoObject := "WatchListBuildInfo",
    buildInfoOptions ++= Seq(BuildInfoOption.ToJson, BuildInfoOption.BuildTime)
  )

