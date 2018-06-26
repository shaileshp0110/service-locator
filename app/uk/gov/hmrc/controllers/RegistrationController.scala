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

package uk.gov.hmrc.controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import uk.gov.hmrc.models.ErrorCode._
import uk.gov.hmrc.models.JsonFormatters._
import uk.gov.hmrc.models.ServiceDetails
import uk.gov.hmrc.services.RegistrationService

import scala.concurrent.ExecutionContext.Implicits.global

case class RegistrationRequest(serviceName: String,
                               serviceUrl: String,
                               metadata: Option[Map[String, String]] = None) {

  require(serviceName.nonEmpty, "serviceName is required")
  require(serviceUrl.nonEmpty, "serviceUrl is required")
}
object RegistrationRequest{
  implicit val format = Json.format[RegistrationRequest]
}

@Singleton
class RegistrationController @Inject()(registrationService: RegistrationService) extends ServiceLocatorController {

  def register: Action[JsValue] = Action.async(BodyParsers.parse.json) {
    implicit request =>
      withJsonBody[RegistrationRequest] { requestBody =>
        Logger.info(s"Registering service $requestBody")
        registrationService.register(ServiceDetails.from(requestBody)).map { _ =>
          Logger.info(s"Service successfully registered $requestBody")
          NoContent
        } recover recovery
      }
  }

  def fetchService(serviceName: String): Action[AnyContent] = Action.async {
    registrationService.fetchService(serviceName).map {
      case Some(serviceDetails) => Ok(Json.toJson(serviceDetails))
      case None => NotFound(error(SERVICE_NOT_FOUND, s"Service not found for name: $serviceName"))
    } recover recovery
  }

}
