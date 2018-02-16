package com.pacbio.secondary.smrtlink.jobtypes

import java.nio.file.{Path, Paths}
import java.util.UUID

import org.joda.time.{DateTime => JodaDateTime}
import com.pacbio.secondary.smrtlink.actors.JobsDao
import com.pacbio.secondary.smrtlink.analysis.converters.MovieMetadataConverter._
import com.pacbio.secondary.smrtlink.analysis.datasets.validators.ValidateHdfSubreadSet
import com.pacbio.secondary.smrtlink.analysis.datasets.{
  DataSetMetaTypes,
  HdfSubreadSetIO
}

import com.pacbio.secondary.smrtlink.analysis.jobs.JobModels._
import com.pacbio.secondary.smrtlink.analysis.jobs.{
  AnalysisJobStates,
  JobResultsWriter
}
import com.pacbio.secondary.smrtlink.analysis.jobs.CoreJobUtils
import com.pacbio.secondary.smrtlink.models.ConfigModels.SystemJobConfig
import com.pacbio.secondary.smrtlink.analysis.converters.DatasetConvertError

/**
  * Created by mkocher on 8/17/17.
  */
case class RsConvertMovieToDataSetJobOptions(
    path: String,
    name: Option[String],
    description: Option[String],
    projectId: Option[Int] = Some(JobConstants.GENERAL_PROJECT_ID))
    extends ServiceJobOptions {
  override def jobTypeId = JobTypeIds.CONVERT_RS_MOVIE
  override def validate(dao: JobsDao, config: SystemJobConfig) = None
  override def toJob() = new RsConvertMovieToDataSetJob(this)
}

class RsConvertMovieToDataSetJob(opts: RsConvertMovieToDataSetJobOptions)
    extends ServiceCoreJob(opts)
    with CoreJobUtils {
  type Out = PacBioDataStore

  private def toDataStoreFile(dsPath: Path, uuid: UUID): DataStoreFile = {
    val sourceId = s"pbscala::${jobTypeId.id}"
    val now = JodaDateTime.now()
    DataStoreFile(
      uuid,
      sourceId,
      DataSetMetaTypes.typeToIdString(DataSetMetaTypes.HdfSubread),
      dsPath.toFile.length(),
      now,
      now,
      dsPath.toAbsolutePath.toString,
      isChunked = false,
      "HdfSubreadSet",
      "RS movie XML converted to PacBio HdfSubreadSet XML"
    )
  }

  private def validate(
      hset: HdfSubreadSetIO): Either[DatasetConvertError, HdfSubreadSetIO] = {
    ValidateHdfSubreadSet.validator(hset.dataset).toEither match {
      case Right(_) => Right(hset)
      case Left(nel) => Left(DatasetConvertError(s"$nel"))
    }
  }

  override def run(
      resources: JobResourceBase,
      resultsWriter: JobResultsWriter,
      dao: JobsDao,
      config: SystemJobConfig): Either[ResultFailed, PacBioDataStore] = {
    val startedAt = JodaDateTime.now()
    val name = opts.name.getOrElse("RS-to-HdfSubreadSet")
    val logPath = resources.path.resolve(JobConstants.JOB_STDOUT)
    val logFile =
      toSmrtLinkJobLog(
        logPath,
        Some(
          s"${JobConstants.DATASTORE_FILE_MASTER_DESC} of the Import Dataset job"))
    val dsPath = resources.path.resolve("rs_movie.hdfsubreadset.xml")
    val datastoreJson = resources.path.resolve("datastore.json")

    convertMovieOrFofnToHdfSubread(Paths.get(opts.path), dsPath, name)
      .flatMap(validate)
      .map { hset =>
        val dataStoreFile =
          toDataStoreFile(hset.path, UUID.fromString(hset.dataset.getUniqueId))
        val ds = PacBioDataStore.fromFiles(Seq(dataStoreFile, logFile))
        writeDataStore(ds, datastoreJson)
        ds
      }
      .left
      .map(
        ex =>
          ResultFailed(resources.jobId,
                       opts.jobTypeId.toString,
                       s"Failed to convert ${opts.path}. ${ex.msg}",
                       computeTimeDeltaFromNow(startedAt),
                       AnalysisJobStates.FAILED,
                       host))
  }
}
