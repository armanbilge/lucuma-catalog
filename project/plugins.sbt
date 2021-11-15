resolvers += Resolver.sonatypeRepo("public")

addSbtPlugin("edu.gemini"         % "sbt-lucuma"               % "0.4.2")
addSbtPlugin("com.geirsson"       % "sbt-ci-release"           % "1.5.7")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.7.1")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("com.timushev.sbt"   % "sbt-updates"              % "0.6.0")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"             % "2.4.3")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"      % "0.20.0")
