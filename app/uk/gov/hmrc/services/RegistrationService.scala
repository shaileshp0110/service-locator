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

package uk.gov.hmrc.services

import javax.inject.{Inject, Singleton}
import akka.actor.Cancellable
import play.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import uk.gov.hmrc.config.AppContext
import uk.gov.hmrc.connectors.SubscriptionCallbackConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.models.{ServiceDetails, Subscription}
import uk.gov.hmrc.repository.{ServiceDetailsRepository, SubscriptionRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

@Singleton
class RegistrationService @Inject()(serviceDetailsRepository: ServiceDetailsRepository,
                                    subscriptionRepository: SubscriptionRepository,
                                    subscriptionCallbackConnector: SubscriptionCallbackConnector,
                                    appContext: AppContext) {

  def register(serviceDetails: ServiceDetails)(implicit  hc:HeaderCarrier): Future[ServiceDetails] = {
    Logger.info(s"Registering service: $serviceDetails")
    val future: Future[ServiceDetails] = serviceDetailsRepository.save(serviceDetails)
    triggerSubscriptions(serviceDetails)
    future
  }

  def fetchService(serviceName: String): Future[Option[ServiceDetails]] = {
    serviceDetailsRepository.fetch(serviceName)
  }

  private def triggerSubscriptions(serviceDetails: ServiceDetails)(implicit  hc:HeaderCarrier): Cancellable = {
    Akka.system.scheduler.scheduleOnce(appContext.subscriptionDelayInSeconds.seconds) {
      subscriptionRepository.fetchAll().flatMap { subscriptions =>
        val futures = subscriptions.map { subscription =>
          subscription.isCriteriaSatisfied(serviceDetails) match {
            case true => issueCallback(subscription, serviceDetails)
            case false => Future.successful(())
          }
        }
        Future.sequence(futures)
      }
    }
  }

  private def issueCallback(subscription: Subscription, serviceDetails: ServiceDetails)(implicit  hc:HeaderCarrier): Future[Unit] = {
    Logger.info(s"Attempting to invoke callback to $subscription")
    subscriptionCallbackConnector.issueCallback(subscription.callbackUrl, serviceDetails).map {
      _ => Logger.info(s"Successfully invoked callback to $subscription")
    } recover {
      case t => Logger.error(s"Failed to invoke callback to $subscription with ${t.getMessage}")
    }
  }

}
