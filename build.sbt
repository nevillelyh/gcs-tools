organization := "com.spotify.data"
name := "gcs-tools"
version := "0.1.3-SNAPSHOT"

val gcsVersion = "1.5.2-hadoop2"
val guavaVersion = "19.0"
val hadoopVersion = "2.7.3"
val avroVersion = "1.8.1"
val parquetVersion = "1.8.1"
val protobufVersion = "3.1.0"
val protobufGenericVersion = "0.1.0"

val commonSettings = Project.defaultSettings ++ assemblySettings ++ Seq(
  scalaVersion := "2.11.8",
  autoScalaLibrary := false
)

lazy val root: Project = Project(
  "gcs-tools",
  file(".")
).aggregate(
  avroTools,
  parquetTools,
  protoTools
)

lazy val shared: Project = Project(
  "shared",
  file("shared"),
  settings = commonSettings
)

lazy val avroTools: Project = Project(
  "avro-tools",
  file("avro-tools"),
  settings = commonSettings ++ Seq(
    mainClass in assembly := Some("org.apache.avro.tool.Main"),
    assemblyJarName in assembly := s"avro-tools-$avroVersion.jar",
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro-tools" % avroVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
    )
  )
).dependsOn(
  shared
)

lazy val parquetTools: Project = Project(
  "parquet-tools",
  file("parquet-tools"),
  settings = commonSettings ++ Seq(
    mainClass in assembly := Some("org.apache.parquet.tools.Main"),
    assemblyJarName in assembly := s"parquet-tools-$parquetVersion.jar",
    libraryDependencies ++= Seq(
      "org.apache.parquet" % "parquet-tools" % parquetVersion,
      "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
    )
  )
).dependsOn(
  shared
)

lazy val protoTools: Project = Project(
  "proto-tools",
  file("proto-tools"),
  settings = commonSettings ++ Seq(
    mainClass in assembly := Some("org.apache.avro.tool.ProtoMain"),
    assemblyJarName in assembly := s"proto-tools-$protobufVersion.jar",
    assemblyShadeRules in assembly := Seq(
      ShadeRule.zap("com.google.protobuf.**").inLibrary("org.apache.avro" % "avro-tools" % avroVersion)
    ),
    libraryDependencies ++= Seq(
      "me.lyh" %% "protobuf-generic" % protobufGenericVersion,
      "com.google.protobuf" % "protobuf-java" % protobufVersion,
      "org.apache.avro" % "avro-tools" % avroVersion,
      "com.google.cloud.bigdataoss" % "gcs-connector" % gcsVersion
    )
  )
).dependsOn(
  shared
)

lazy val assemblySettings = Seq(
  mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
    case s if s.endsWith(".properties") => MergeStrategy.filterDistinctLines
    case s if s.endsWith("pom.xml") => MergeStrategy.last
    case s if s.endsWith(".class") => MergeStrategy.last
    case s if s.endsWith("libjansi.jnilib") => MergeStrategy.last
    case s if s.endsWith("jansi.dll") => MergeStrategy.rename
    case s if s.endsWith("libjansi.so") => MergeStrategy.rename
    case s if s.endsWith("libsnappyjava.jnilib") => MergeStrategy.last
    case s if s.endsWith("libsnappyjava.so") => MergeStrategy.last
    case s if s.endsWith("snappyjava_snappy.dll") => MergeStrategy.last
    case s if s.endsWith(".dtd") => MergeStrategy.rename
    case s if s.endsWith(".xsd") => MergeStrategy.rename
    case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") => MergeStrategy.filterDistinctLines
    case PathList("META-INF", "LICENSE") => MergeStrategy.discard
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case PathList("META-INF", "DUMMY.SF") => MergeStrategy.discard
    case PathList("META-INF", "DUMMY.RSA") => MergeStrategy.discard
    case PathList("META-INF", "DUMMY.DSA") => MergeStrategy.discard
    case PathList("META-INF", "MSFTSIG.RSA") => MergeStrategy.discard
    case PathList("META-INF", "MSFTSIG.SF") => MergeStrategy.discard
    case PathList("META-INF", "NOTICE") => MergeStrategy.rename
    case _ => MergeStrategy.last
  }}
)
