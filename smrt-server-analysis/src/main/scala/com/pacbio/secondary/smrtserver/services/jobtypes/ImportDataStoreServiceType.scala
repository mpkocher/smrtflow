package com.pacbio.secondary.smrtserver.services.jobtypes

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.pacbio.common.actors.{UserServiceActorRefProvider, UserServiceActor}
import com.pacbio.common.auth.{AuthenticatorProvider, Authenticator}
import com.pacbio.common.dependency.Singleton
import com.pacbio.secondary.analysis.engine.CommonMessages.CheckForRunnableJob
import com.pacbio.secondary.analysis.jobs.CoreJob
import com.pacbio.secondary.analysis.jobs.JobModels._
import com.pacbio.secondary.analysis.jobtypes.ImportDataStoreOptions
import com.pacbio.secondary.smrtlink.actors.{EngineManagerActorProvider, JobsDaoActorProvider}
import com.pacbio.secondary.smrtlink.actors.JobsDaoActor._
import com.pacbio.secondary.smrtlink.models._
import com.pacbio.secondary.smrtlink.services.jobtypes.JobTypeService
import com.pacbio.secondary.smrtlink.services.JobManagerServiceProvider
import com.pacbio.secondary.smrtserver.models.SecondaryAnalysisJsonProtocols
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import spray.json._
import spray.http._
import spray.httpx.SprayJsonSupport
import SprayJsonSupport._


class ImportDataStoreServiceType(dbActor: ActorRef, userActor: ActorRef, engineManagerActor: ActorRef, authenticator: Authenticator)
  extends JobTypeService with LazyLogging {

  import SecondaryAnalysisJsonProtocols._

  override val endpoint = "import-datastore"
  override val description = "Import a PacBio DataStore JSON file"

  override val routes =
    pathPrefix(endpoint) {
      pathEndOrSingleSlash {
        get {
          complete {
            jobList(dbActor, userActor, endpoint)
          }
        } ~
        post {
          optionalAuthenticate(authenticator.jwtAuth) { authInfo =>
            entity(as[ImportDataStoreOptions]) { sopts =>
              val uuid = UUID.randomUUID()
              val coreJob = CoreJob(uuid, sopts)
              val jsonSettings = sopts.toJson.toString()
              val fx = (dbActor ? CreateJobType(
                uuid,
                s"Job $endpoint",
                s"Importing DataStore",
                endpoint,
                coreJob,
                None,
                jsonSettings,
                authInfo.map(_.login))).mapTo[EngineJob]

              complete {
                created {
                  fx.map(job => addUser(userActor, job))
                }
              }
            }
          }
        }
      } ~
      sharedJobRoutes(dbActor, userActor)
    }
}

trait ImportDataStoreServiceTypeProvider {
  this: JobsDaoActorProvider
      with AuthenticatorProvider
      with UserServiceActorRefProvider
      with EngineManagerActorProvider
      with JobManagerServiceProvider =>

  val importDataStoreServiceType: Singleton[ImportDataStoreServiceType] =
    Singleton(() => new ImportDataStoreServiceType(jobsDaoActor(), userServiceActorRef(), engineManagerActor(), authenticator())).bindToSet(JobTypes)
}