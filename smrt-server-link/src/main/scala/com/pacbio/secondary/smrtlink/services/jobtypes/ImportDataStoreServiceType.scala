package com.pacbio.secondary.smrtlink.services.jobtypes

import java.util.UUID

import akka.actor.ActorRef
import com.pacbio.common.auth.{Authenticator, AuthenticatorProvider}
import com.pacbio.common.dependency.Singleton
import com.pacbio.common.models.UserRecord
import com.pacbio.secondary.analysis.jobs.CoreJob
import com.pacbio.secondary.analysis.jobs.JobModels._
import com.pacbio.secondary.analysis.jobtypes.ImportDataStoreOptions
import com.pacbio.secondary.smrtlink.actors.JobsDaoActor._
import com.pacbio.secondary.smrtlink.actors.JobsDaoActorProvider
import com.pacbio.secondary.smrtlink.app.SmrtLinkConfigProvider
import com.pacbio.secondary.smrtlink.models.SecondaryAnalysisJsonProtocols._
import com.pacbio.secondary.smrtlink.services.JobManagerServiceProvider
import com.typesafe.scalalogging.LazyLogging
import spray.httpx.SprayJsonSupport._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportDataStoreServiceType(dbActor: ActorRef,
                                 authenticator: Authenticator,
                                 smrtLinkVersion: Option[String],
                                 smrtLinkToolsVersion: Option[String])
  extends {
    override val endpoint = JobTypeIds.IMPORT_DATASTORE.id
    override val description = "Import a PacBio DataStore JSON file"
  } with JobTypeService[ImportDataStoreOptions](dbActor, authenticator) with LazyLogging {

  override def createJob(sopts: ImportDataStoreOptions, user: Option[UserRecord]): Future[CreateJobType] = Future {
    val uuid = UUID.randomUUID()
    CreateJobType(
      uuid,
      s"Job $endpoint",
      s"Importing DataStore",
      endpoint,
      CoreJob(uuid, sopts),
      None,
      sopts.toJson.toString(),
      user.map(_.userId),
      smrtLinkVersion,
      smrtLinkToolsVersion)
  }
}

trait ImportDataStoreServiceTypeProvider {
  this: JobsDaoActorProvider
    with AuthenticatorProvider
    with JobManagerServiceProvider
    with SmrtLinkConfigProvider =>

  val importDataStoreServiceType: Singleton[ImportDataStoreServiceType] =
    Singleton(() => new ImportDataStoreServiceType(jobsDaoActor(), authenticator(), smrtLinkVersion(), smrtLinkToolsVersion())).bindToSet(JobTypes)
}
