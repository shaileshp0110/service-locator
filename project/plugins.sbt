
resolvers += Resolver.url("hmrc-sbt-plugin-releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
resolvers += Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.13")
addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "1.13.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "1.9.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "0.13.0")
addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "1.0.0")
addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.5.1")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
