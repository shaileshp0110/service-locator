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

package unit.connectors

import java.util.concurrent.TimeUnit
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.connectors.Retry
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RetrySpec extends UnitSpec with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  "Retry" should {

    "Should succeed on third attempt" in {
      val test = Test
      when(test.result.create).thenReturn(false, false, true)
      await(test.retry()(test.doTest()))
      verify(test.result, times(3)).create
    }

    "Should fail if retry limit is reached without success" in {
      val test = Test
      when(test.result.create).thenReturn(false, false, false)
      try {
        Await.result(test.retry()(test.doTest()), Duration(5, TimeUnit.MINUTES))
        fail("Exception was expected but not thrown")
      } catch {
        case e: RuntimeException => verify(test.result, times(3)).create
      }
    }

  }
  override def beforeEach() =  {
    reset(Test.result)
  }

  trait TestCriteria {
    def create: Boolean
  }

  object Test extends Retry {

    var result = mock[TestCriteria]

    def doTest(): () => Future[Unit] = {
      () => {
        result.create match {
          case false => Future.failed(new RuntimeException("expected test exception"))
          case true => Future.successful(())
        }
      }
    }
  }

}
