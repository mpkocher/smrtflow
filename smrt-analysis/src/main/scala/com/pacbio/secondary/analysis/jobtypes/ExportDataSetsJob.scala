
package com.pacbio.secondary.analysis.jobtypes

import java.nio.file.{Path, Paths}
import java.util.UUID
import com.pacbio.secondary.analysis.constants.FileTypes
import com.pacbio.secondary.analysis.datasets.DataSetMetaTypes
import com.pacbio.secondary.analysis.reports.DataSetReports
import com.pacbio.secondary.analysis.tools.timeUtils
import org.joda.time.{DateTime => JodaDateTime}

import com.pacbio.secondary.analysis.datasets.io._
import com.pacbio.secondary.analysis.jobs._
import com.pacbio.secondary.analysis.jobs.JobModels._
import com.pacificbiosciences.pacbiodatasets._


case class ExportDataSetsOptions(
    datasetType: String,
    paths: Seq[String],
    outputPath: String) extends BaseJobOptions {
  def toJob = new ExportDataSetsJob(this)

  override def validate = {
    for {
      v1 <- Validators.filesExists(paths)
      v2 <- Validators.validateDataSetType(datasetType)
    } yield v2
  }
}

class ExportDataSetsJob(opts: ExportDataSetsOptions)
    extends BaseCoreJob(opts: ExportDataSetsOptions)
    with MockJobUtils with timeUtils {

  type Out = PacBioDataStore
  val jobTypeId = JobTypeId("export_datasets")

  def run(job: JobResourceBase, resultsWriter: JobResultWriter): Either[ResultFailed, Out] = {

    val startedAt = JodaDateTime.now()

    resultsWriter.writeLineStdout(s"Starting export of ${opts.paths.length} ${opts.datasetType} Files at ${startedAt.toString}")

    resultsWriter.writeLineStdout(s"DataSet Export options: $opts")
    opts.paths.foreach(x => resultsWriter.writeLineStdout(s"File $x"))

    val datastoreJson = job.path.resolve("datastore.json")
    val outputPath = Paths.get(opts.outputPath)

    val paths = opts.paths.map(x => Paths.get(x))
    val mType = DataSetMetaTypes.toDataSetType(opts.datasetType)

    val result = mType match {
      case Some(dsType) => Some(ExportDataSets(paths, dsType, outputPath))
      case x =>
        resultsWriter.writeLineStderr(s"Unsupported DataSet type $x")
        None
    }

    result match {
      case Some(nbytes) =>
        resultsWriter.writeStdout(s"Successfully exported datasets to ${outputPath.toAbsolutePath}")
        val now = JodaDateTime.now()
        val dataStoreFile = DataStoreFile(
          UUID.randomUUID(),
          s"pbscala::${jobTypeId.id}",
          FileTypes.ZIP.fileTypeId,
          outputPath.toFile.length,
          now,
          now,
          outputPath.toAbsolutePath.toString,
          isChunked = false,
          "ZIP file",
          s"ZIP file containing ${opts.paths.length} datasets")
        val logPath = job.path.resolve("pbscala-job.stdout")

        val logFile = DataStoreFile(
          UUID.randomUUID(),
          s"master.log",
          FileTypes.LOG.fileTypeId,
          // probably wrong; the file isn't closed yet.  But it won't get
          // closed until after this method completes.
          logPath.toFile.length,
          now,
          now,
          logPath.toString,
          isChunked = false,
          "Master Log",
          "Master log of the Merge Dataset job")
        val ds = PacBioDataStore(now, now, "0.2.1", Seq(dataStoreFile, logFile))
        writeDataStore(ds, datastoreJson)
        resultsWriter.writeStdout(s"Successfully wrote datastore to ${datastoreJson.toAbsolutePath}")
        Right(ds)
      case _ =>
        Left(ResultFailed(job.jobId, jobTypeId.id, s"Failed to export datasets. Unsupported dataset type '${opts.datasetType}'", computeTimeDeltaFromNow(startedAt), AnalysisJobStates.FAILED, host))
    }
  }
}
