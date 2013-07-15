resolvers ++= Seq(
    Classpaths.typesafeResolver,
    "scct-github-repository" at "http://mtkopone.github.com/scct/maven-repo"
)

addSbtPlugin("reaktor" % "sbt-scct" % "0.2-SNAPSHOT")

addSbtPlugin("com.github.theon" %% "xsbt-coveralls-plugin" % "0.0.3")

// addSbtPlugin("com.github.philcali" % "sbt-lwjgl-plugin" % "3.1.1")

addSbtPlugin("com.github.retronym" % "sbt-onejar" % "0.8")
