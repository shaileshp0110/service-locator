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

package uk.gov.hmrc.connectors

import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import org.jboss.netty.util.{HashedWheelTimer, Timer}
import play.api.http.ContentTypes.JSON
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.libs.json._
import uk.gov.hmrc.config.WSHttp
import uk.gov.hmrc.http._
import uk.gov.hmrc.models.JsonFormatters._
import uk.gov.hmrc.models.ServiceDetails

import scala.concurrent.Future

@Singleton
class SubscriptionCallbackConnector @Inject()(http: WSHttp) extends Retry {

  implicit val timer: Timer = new HashedWheelTimer(1, TimeUnit.MILLISECONDS)
  implicit val executor = scala.concurrent.ExecutionContext.Implicits.global

  def issueCallback(callbackUrl: String, serviceDetails: ServiceDetails)(implicit  hc:HeaderCarrier): Future[Unit] = {
    retry()(doCallback(callbackUrl, serviceDetails))
  }

  private def doCallback(callbackUrl: String, serviceDetails: ServiceDetails)(implicit  hc:HeaderCarrier): () => Future[Unit] = {
    val result = (): Unit

    () =>
      val jsonPayload: JsValue = Json.toJson(serviceDetails)
      http.POST[JsValue, HttpResponse](callbackUrl, jsonPayload, Seq(CONTENT_TYPE -> JSON))
        .map(_ => result)
        .recover {
          case _: BadRequestException => result
          case _: NotFoundException => result
          case _: Upstream4xxResponse => result
          case _ => throw new RuntimeException(s"Invalid response from callback: $callbackUrl")
      }
  }

}
