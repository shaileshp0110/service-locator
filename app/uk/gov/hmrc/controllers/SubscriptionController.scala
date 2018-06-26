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
import uk.gov.hmrc.models.JsonFormatters._
import uk.gov.hmrc.models.Subscription
import uk.gov.hmrc.services.SubscriptionService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SubscriptionController @Inject()(subscriptionService: SubscriptionService) extends ServiceLocatorController {

  def subscribe: Action[JsValue] = Action.async(BodyParsers.parse.json) {
    implicit request => withJsonBody[Subscription] {
      requestBody => {
        Logger.info(s"Registering publisher $requestBody")
        subscriptionService.subscribe(requestBody).map { _ =>
          Logger.info(s"Publisher successfully registered $requestBody")
          NoContent
        } recover recovery
      }
    }
  }

}
