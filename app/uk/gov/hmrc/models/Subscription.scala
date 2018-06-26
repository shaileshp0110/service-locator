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

package uk.gov.hmrc.models

import play.api.libs.json.Json

case class Subscription(serviceName: String,
                        callbackUrl: String,
                        criteria: Option[Map[String, String]] = None) {

  require(serviceName.nonEmpty, "serviceName is required")
  require(callbackUrl.nonEmpty, "callbackUrl is required")

  def isCriteriaSatisfied(serviceDetails: ServiceDetails): Boolean = {
    criteria match {
      case None => true
      case Some(c) => c.toSet subsetOf serviceDetails.metadata.getOrElse(Map()).toSet
    }
  }

}

object Subscription {
  implicit val format = Json.format[Subscription]
}