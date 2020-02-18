name := "CRUDCassandra"

version := "0.1"

scalaVersion := "2.12.7"

lazy val akkaVersion = "2.5.25"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-slf4j"   % akkaVersion,
  "ch.qos.logback"    % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-http"   % "10.1.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  // AWS SDK
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.670",
  "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "1.1.2"
)