package com.pacbio.secondary.smrtlink.jobtypes

import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import com.pacbio.common.models.CommonModels.IdAble
import com.pacbio.common.models.CommonModelImplicits
import com.pacbio.secondary.smrtlink.actors.JobsDao
import com.pacbio.secondary.smrtlink.analysis.datasets.DataSetMetaTypes
import com.pacbio.secondary.smrtlink.analysis.jobs.JobModels._
import com.pacbio.secondary.smrtlink.analysis.jobs.{
  InvalidJobOptionError,
  JobResultsWriter
}
import com.pacbio.secondary.smrtlink.analysis.jobtypes.ExportDataSetsOptions
import com.pacbio.secondary.smrtlink.models.ConfigModels.SystemJobConfig
import com.pacbio.secondary.smrtlink.models.{
  BoundServiceEntryPoint,
  DataSetExportServiceOptions,
  EngineJobEntryPointRecord,
  EngineJobEntryPoint
}
import com.pacbio.secondary.smrtlink.jsonprotocols.SmrtLinkJsonProtocols
import com.pacbio.secondary.smrtlink.services.PacBioServiceErrors.UnprocessableEntityError
import com.pacbio.secondary.smrtlink.validators.ValidateServiceDataSetUtils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal

trait ValidateJobUtils {

  // I think this should be deleted.
  def projectJoiner(projectIds: Seq[Int]): Int = {
    val ids = projectIds.toSet
    if (ids.size == 1) ids.head
    else JobConstants.GENERAL_PROJECT_ID
  }

  def validateOutputDir(dir: Path): Future[Path] = {
    if (!dir.toFile.exists)
      Future.failed(
        new UnprocessableEntityError(
          s"The directory ${dir.toString} does not exist"))
    else if (!Files.isWritable(dir))
      Future.failed(new UnprocessableEntityError(
        s"SMRTLink does not have write permissions for the directory ${dir.toString}"))
    else Future.successful(dir)
  }

  def validateOutputPath(p: Path): Future[Path] = {
    val dir = p.getParent
    if (p.toFile.exists)
      Future.failed(
        new UnprocessableEntityError(s"The file $p already exists"))
    else validateOutputDir(dir).map(d => p)
  }
}

/**
  * Created by mkocher on 8/17/17.
  */
case class ExportDataSetsJobOptions(
    datasetType: DataSetMetaTypes.DataSetMetaType,
    ids: Seq[IdAble],
    outputPath: Path,
    deleteAfterExport: Option[Boolean],
    name: Option[String],
    description: Option[String],
    projectId: Option[Int] = Some(JobConstants.GENERAL_PROJECT_ID))
    extends ServiceJobOptions
    with ValidateJobUtils {

  import CommonModelImplicits._

  // Need to think about how this is set from the EngineJob or if it's even necessary
  override def jobTypeId = JobTypeIds.EXPORT_DATASETS
  override def toJob() = new ExportDataSetJob(this)

  override def resolveEntryPoints(dao: JobsDao) =
    validateAndResolveEntryPoints(dao, datasetType, ids)

  override def validate(
      dao: JobsDao,
      config: SystemJobConfig): Option[InvalidJobOptionError] = {
    // This should probably reuse resolveEntryPoints
    val f: Future[Option[InvalidJobOptionError]] = for {
      _ <- validateOutputPath(outputPath)
      _ <- ValidateServiceDataSetUtils.resolveInputs(datasetType, ids, dao)
    } yield None

    val f2 = f.recover {
      case NonFatal(ex) =>
        Some(
          InvalidJobOptionError(
            s"Invalid ExportDataSet options ${ex.getMessage}"))
    }

    Await.result(f2, DEFAULT_TIMEOUT)
  }

}

class ExportDataSetJob(opts: ExportDataSetsJobOptions)
    extends ServiceCoreJob(opts) {
  type Out = PacBioDataStore

  import SmrtLinkJsonProtocols._
  import com.pacbio.common.models.CommonModelImplicits._

  private def createDeleteJob(resources: JobResourceBase,
                              dao: JobsDao): EngineJob = {
    def creator(parentJob: EngineJob,
                epoints: Seq[EngineJobEntryPoint]): Future[EngineJob] = {
      val name = "Delete exported datasets"
      val desc = s"Created from export-datasets job ${parentJob.id}"
      val dOpts = DeleteDataSetJobOptions(opts.ids,
                                          opts.datasetType,
                                          true,
                                          Some(name),
                                          Some(desc),
                                          opts.projectId)
      val jsettings = dOpts.toJson.asJsObject
      dao.createCoreJob(
        UUID.randomUUID(),
        name,
        desc,
        dOpts.jobTypeId,
        epoints.map(_.toRecord),
        jsettings,
        parentJob.createdBy,
        parentJob.createdByEmail,
        parentJob.smrtlinkVersion,
        parentJob.projectId
      )
    }

    val fx = for {
      parentJob <- dao.getJobById(resources.jobId)
      epoints <- dao.getJobEntryPoints(parentJob.id)
      deleteJob <- creator(parentJob, epoints)
    } yield deleteJob

    Await.result(fx, opts.DEFAULT_TIMEOUT)
  }

  override def run(
      resources: JobResourceBase,
      resultsWriter: JobResultsWriter,
      dao: JobsDao,
      config: SystemJobConfig): Either[ResultFailed, PacBioDataStore] = {

    val timeout: FiniteDuration = opts.ids.length * opts.TIMEOUT_PER_RECORD
    val paths: Seq[Path] = resolvePathsAndWriteEntryPoints(dao,
                                                           resources.path,
                                                           timeout,
                                                           opts.datasetType,
                                                           opts.ids)

    val oldOpts = ExportDataSetsOptions(opts.datasetType,
                                        paths,
                                        opts.outputPath,
                                        opts.getProjectId())
    val job = oldOpts.toJob
    val result = job.run(resources, resultsWriter)
    if (result.isRight && opts.deleteAfterExport.getOrElse(false)) {
      resultsWriter.writeLine("Export succeeded - creating delete job")
      val deleteJob = createDeleteJob(resources, dao)
      resultsWriter.writeLine(s"Dataset delete job ${deleteJob.id} started")
    }
    result
  }
}
