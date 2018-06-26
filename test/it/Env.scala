/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration


object Env {

  val port = sys.env.getOrElse("SERVICE_LOCATOR", "14234").toInt
  val host = sys.env.getOrElse("HOST", "http://localhost:14234")
  val context = sys.env.getOrElse("CONTEXT", "")
  val contextUrl = host + context

  val stubPort = sys.env.getOrElse("WIREMOCK", "11112").toInt
  val stubHost = "localhost"
  val wiremockUrl = s"http://$stubHost:$stubPort"
  private val wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(stubPort))

  def shutdown(): Unit = wireMockServer.stop()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = shutdown()
  })

  def before() = {
    if (!wireMockServer.isRunning) {
      wireMockServer.start()
    }
    WireMock.configureFor(stubHost, stubPort)
    stubFor(post(urlEqualTo("/notification")).willReturn(aResponse()))
  }

  def after() = {
    if (wireMockServer.isRunning) WireMock.reset()
  }

}