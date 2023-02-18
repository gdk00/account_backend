package misis.account.repository
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes, StatusCode}
import akka.http.scaladsl.unmarshalling.Unmarshal
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import io.circe.syntax._
import misis.account.model.{Account, ChangeBalance, ExternalAccountsListResponse}
import shapeless.ops.hlist.Union

import scala.concurrent.{ExecutionContext, Future}

class ExternalTransferClient(implicit val ec: ExecutionContext, actorSystem: ActorSystem) extends FailFastCirceSupport {
    def external_increase(item: ChangeBalance): Future[StatusCode] = {
        val request = HttpRequest(
            method = HttpMethods.PUT,
            uri = s"http://localhost:8081/account/increase",
            entity = HttpEntity(MediaTypes.`application/json`, item.asJson.noSpaces)
        )
        for {
            response <- Http().singleRequest(request)
        } yield response.status
    }

    def getAccounts: Future[Either[String, List[Account]]] = {
        val request = HttpRequest(
            method = HttpMethods.GET,
            uri = s"http://localhost:8081/accounts"
        )
        for {
            response <- Http().singleRequest(request)
            result <- Unmarshal(response).to[List[Account]]
        } yield
            if (response.status.isFailure()) {
                Left("Bad accounts list checks")
            } else {
                Right(result)
            }
    }
}
