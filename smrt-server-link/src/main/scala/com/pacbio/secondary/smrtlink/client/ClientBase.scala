package com.pacbio.secondary.smrtlink.client

import java.net.URL

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.pacbio.secondary.smrtlink.models.ServiceStatus

/**
  * Base Client trait for
  *
  */
trait ClientBase extends Retrying {

  // This starts to tangle up specific JSON conversion with the Client
  import com.pacbio.secondary.smrtlink.jsonprotocols.SmrtLinkJsonProtocols._
  import SprayJsonSupport._

  implicit val actorSystem: ActorSystem
  implicit val materializer = ActorMaterializer()
  implicit val ec = actorSystem.dispatcher

  lazy val http = Http()

  val baseUrl: URL

  // This should really return a URL instance, not a string
  def toUrl(segment: String): String =
    new URL(baseUrl.getProtocol,
            baseUrl.getHost,
            baseUrl.getPort,
            baseUrl.getPath + segment).toString

  val statusUrl = toUrl("/status")

  /**
    * Get Status of the System. The model must adhere to the SmrtServer Status
    * message schema.
    *
    * @return
    */
  def getStatus(): Future[ServiceStatus] =
    http.singleRequest(Get(statusUrl)).flatMap(Unmarshal(_).to[ServiceStatus])

  def getStatusWithRetry(
      maxRetries: Int = 3,
      retryDelay: FiniteDuration = 1.second): Future[ServiceStatus] =
    retry[ServiceStatus](getStatus, retryDelay, maxRetries)(
      actorSystem.dispatcher,
      actorSystem.scheduler)

}
