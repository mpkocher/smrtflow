package com.pacbio.secondary.smrtlink.tools

import java.io.File
import java.net.URL
import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.language.postfixOps
import scala.math._
import scala.util.control.NonFatal
import scala.util.{Failure, Properties, Success, Try}

import akka.actor.ActorSystem
import com.typesafe.scalalogging.LazyLogging
import scopt.OptionParser
import spray.json._

import com.pacbio.common.models._
import com.pacbio.secondary.smrtlink.services.PacBioServiceErrors.{
  ResourceNotFoundError,
  UnprocessableEntityError
}
import com.pacbio.common.logging.{LoggerConfig, LoggerOptions}
import com.pacbio.secondary.smrtlink.analysis.constants.FileTypes
import com.pacbio.secondary.smrtlink.analysis.converters._
import com.pacbio.secondary.smrtlink.analysis.datasets.DataSetMetaTypes
import com.pacbio.secondary.smrtlink.actors.CommonMessages.MessageResponse
import com.pacbio.secondary.smrtlink.analysis.jobs.JobModels._
import com.pacbio.secondary.smrtlink.analysis.jobs.{
  AnalysisJobStates,
  JobModels
}
import com.pacbio.secondary.smrtlink.analysis.pbsmrtpipe.PbsmrtpipeConstants
import com.pacbio.secondary.smrtlink.analysis.pipelines._
import com.pacbio.secondary.smrtlink.analysis.tools._
import com.pacbio.secondary.smrtlink.actors.DaoFutureUtils
import com.pacbio.secondary.smrtlink.client._
import com.pacbio.secondary.smrtlink.models._

object Modes {
  sealed trait Mode {
    val name: String
  }
  case object STATUS extends Mode { val name = "status" }
  case object IMPORT_DS extends Mode { val name = "import-dataset" }
  case object IMPORT_FASTA extends Mode { val name = "import-fasta" }
  case object IMPORT_BARCODES extends Mode { val name = "import-barcodes" }
  case object ANALYSIS extends Mode { val name = "run-analysis" }
  case object TEMPLATE extends Mode { val name = "emit-analysis-template" }
  case object PIPELINE extends Mode { val name = "run-pipeline" }
  case object SHOW_PIPELINES extends Mode { val name = "show-pipelines" }
  case object IMPORT_MOVIE extends Mode { val name = "import-rs-movie" }
  case object JOB extends Mode { val name = "get-job" }
  case object JOBS extends Mode { val name = "get-jobs" }
  case object TERMINATE_JOB extends Mode { val name = "terminate-job" } // This currently ONLY supports Analysis Jobs
  case object DELETE_JOB extends Mode { val name = "delete-job" } // also only analysis jobs
  case object EXPORT_JOB extends Mode { val name = "export-job" }
  case object IMPORT_JOB extends Mode { val name = "import-job" }
  case object DATASET extends Mode { val name = "get-dataset" }
  case object DATASETS extends Mode { val name = "get-datasets" }
  case object DELETE_DATASET extends Mode { val name = "delete-dataset" }
  case object CREATE_PROJECT extends Mode { val name = "create-project" }
  case object MANIFESTS extends Mode { val name = "get-manifests" }
  case object MANIFEST extends Mode { val name = "get-manifest" }
  case object BUNDLES extends Mode { val name = "get-bundles" }
  case object TS_STATUS extends Mode { val name = "ts-status" }
  case object TS_JOB extends Mode { val name = "ts-failed-job" }
  case object ALARMS extends Mode { val name = "get-alarms" }
  case object UNKNOWN extends Mode { val name = "unknown" }
}

object PbServiceParser extends CommandLineToolVersion {
  import CommonModelImplicits._
  import CommonModels._

  val VERSION = "0.2.1"
  var TOOL_ID = "pbscala.tools.pbservice"

  // DataSet XML filename ending extension
  final val DS_FILE_EXT = "set.xml"

  private def getSizeMb(fileObj: File): Double = {
    fileObj.length / 1024.0 / 1024.0
  }

  def showDefaults(c: CustomConfig): Unit = {
    println(s"Defaults $c")
  }

  def showVersion: Unit = showToolVersion(TOOL_ID, VERSION)

  // is there a cleaner way to do this?
  private def entityIdOrUuid(entityId: String): IdAble = {
    try {
      IntIdAble(entityId.toInt)
    } catch {
      case e: Exception => {
        try {
          UUIDIdAble(UUID.fromString(entityId))
        } catch {
          case e: Exception => 0
        }
      }
    }
  }

  private def getToken(token: String): String = {
    if (Paths.get(token).toFile.isFile) {
      Source.fromFile(token).getLines.take(1).toList.head
    } else token
  }

  case class CustomConfig(
      mode: Modes.Mode = Modes.UNKNOWN,
      host: String,
      port: Int,
      block: Boolean = false,
      command: CustomConfig => Unit = showDefaults,
      datasetId: IdAble = 0,
      jobId: IdAble = 0,
      path: Path = null,
      name: String = "",
      organism: String = "",
      ploidy: String = "",
      maxItems: Int = 25,
      datasetType: DataSetMetaTypes.DataSetMetaType = DataSetMetaTypes.Subread,
      jobType: String = "pbsmrtpipe",
      jobState: Option[String] = None,
      nonLocal: Option[DataSetMetaTypes.DataSetMetaType] = None,
      asJson: Boolean = false,
      dumpJobSettings: Boolean = false,
      pipelineId: String = "",
      jobTitle: String = "",
      entryPoints: Seq[String] = Seq(),
      presetXml: Option[Path] = None,
      taskOptions: Option[Map[String, String]] = None,
      maxTime: FiniteDuration = 30.minutes, // This probably needs to be tuned on a per subparser.
      project: Option[String] = None,
      description: String = "",
      authToken: Option[String] = Properties.envOrNone("PB_SERVICE_AUTH_TOKEN"),
      manifestId: String = "smrtlink",
      showReports: Boolean = false,
      searchName: Option[String] = None,
      searchPath: Option[String] = None,
      force: Boolean = false,
      user: String = System.getProperty("user.name"),
      password: Option[String] =
        Properties.envOrNone("PB_SERVICE_AUTH_PASSWORD"),
      usePassword: Boolean = false,
      comment: String = "Sent via pbservice",
      includeEntryPoints: Boolean = false,
      blockImportDataSet: Boolean = true // this is duplicated with "block". This should be collapsed to have consistent behavior within pbservice
  ) extends LoggerConfig

  lazy val defaultHost: String =
    Properties.envOrElse("PB_SERVICE_HOST", "localhost")
  lazy val defaultPort: Int =
    Properties.envOrElse("PB_SERVICE_PORT", "8070").toInt
  lazy val defaults = CustomConfig(null, defaultHost, defaultPort)

  lazy val parser = new OptionParser[CustomConfig]("pbservice") {

    private val DS_META_TYPE_NAME =
      "Dataset Meta type name (e.g., subreads, references, PacBio.DataSet.SubreadSet, etc.)"

    private def validateId(entityId: String,
                           entityType: String): Either[String, Unit] = {
      entityIdOrUuid(entityId) match {
        case IntIdAble(x) =>
          if (x > 0) success
          else
            failure(
              s"${entityType} ID must be a positive integer or a UUID string")
        case UUIDIdAble(x) => success
      }
    }

    private def validateJobType(jobType: String): Either[String, Unit] = {
      JobTypeIds
        .fromString(jobType)
        .map(_ => success)
        .getOrElse(failure(
          s"Unrecognized job type '$jobType' Known Jobs types ${JobTypeIds.ALL
            .map(_.id)
            .reduce(_ + "," + _)}"))
    }

    private def validateDataSetMetaType(dsType: String): Either[String, Unit] = {
      val errorMsg =
        s"Invalid DataSet type '$dsType'. Allowed DataSet types ${DataSetMetaTypes.ALL
          .map(_.toString)
          .reduce(_ + "," + _)}"
      DataSetMetaTypes
        .fromAnyName(dsType)
        .map(_ => success)
        .getOrElse(failure(errorMsg))
    }

    head("PacBio SMRTLINK Analysis Services Client", VERSION)

    opt[String]("host") action { (x, c) =>
      c.copy(host = x)
    } text s"Hostname of smrtlink server (default: $defaultHost).  Override the default with env PB_SERVICE_HOST."

    opt[Int]("port") action { (x, c) =>
      c.copy(port = x)
    } text s"Services port on smrtlink server (default: $defaultPort).  Override default with env PB_SERVICE_PORT."

    opt[String]('u', "user") action { (u, c) =>
      c.copy(user = u)
    } text "User ID (requires password if used for authentication)"

    /*opt[String]("password") action { (p, c) =>
      c.copy(password = Some(p))
    } text "Authentication password"

    opt[Unit]('p', "ask-pass") action { (_, c) =>
      c.copy(usePassword = true)
    } text "Prompt for authentication password"*/

    opt[String]('t', "token") action { (t, c) =>
      c.copy(authToken = Some(getToken(t)))
    } text "Authentication token (required for project services)"

    // This needs to be folded back into each subparser for clarity. --json isn't supported by every subparser
    opt[Unit]("json") action { (_, c) =>
      c.copy(asJson = true)
    } text "Display output as JSON"

    opt[Unit]('h', "help") action { (x, c) =>
      showUsage
      sys.exit(0)
    } text "Show options and exit"

    opt[Unit]("version") action { (x, c) =>
      showVersion
      sys.exit(0)
    } text "Show tool version and exit"

    // This will handling the adding the logging specific options (e.g., --debug) as well as logging configuration setup
    LoggerOptions.add(this.asInstanceOf[OptionParser[LoggerConfig]])

    // System Status
    note("\nGET SMRTLINK SYSTEM STATUS\n")
    cmd(Modes.STATUS.name)
      .text("Get SMRT Link System Status")
      .action { (_, c) =>
        c.copy(command = (c) => println("with " + c), mode = Modes.STATUS)
      }

    // Import Datasets
    note("\nIMPORT DATASET\n")
    cmd(Modes.IMPORT_DS.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.IMPORT_DS)
    } children (
      arg[File]("dataset-path")
        .required()
        .text(s"""
           |DataSet XML path, a FOFN  (File of File Names) of DataSet XML Files, or a directory containing datasets with files ending with '$DS_FILE_EXT'.
           |When running in directory mode, or FOFN mode with a large number of datasets, it's strongly recommended to run with --non-block
         """.stripMargin)
        .action((p, c) => c.copy(path = p.toPath)),
      opt[Int]("timeout")
        .text(
          s"Maximum time to poll for running job status in seconds (Default ${defaults.maxTime})")
        .action((t, c) => c.copy(maxTime = t.seconds)),
      opt[String]("non-local")
        .validate(validateDataSetMetaType)
        .text("Import non-local dataset require specified the dataset metatype (e.g. PacBio.DataSet.SubreadSet)")
        .action { (t, c) =>
          c.copy(nonLocal = DataSetMetaTypes.fromAnyName(t))
        },
      opt[Unit]("block")
        .text(
          s"Enable blocking mode to poll for job to completion (Default ${defaults.blockImportDataSet}). Mutually exclusive with --no-block")
        .action((t, c) => c.copy(blockImportDataSet = true)),
      opt[Unit]("non-block")
        .text(
          s"Disable blocking mode to poll for job to completion. Import Job will only be submitted. (Default ${!defaults.blockImportDataSet}). Mutually exclusive with --block")
        .action((t, c) => c.copy(blockImportDataSet = false)),
      opt[String]("project") action { (p, c) =>
        c.copy(project = Some(p))
      } text "Name of project associated with this dataset"
    ) text "Import PacBio DataSet(s) into SMRTLink"

    note("\nIMPORT FASTA\n")
    cmd(Modes.IMPORT_FASTA.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.IMPORT_FASTA)
    } children (
      arg[File]("fasta-path") required () action { (p, c) =>
        c.copy(path = p.toPath)
      } text "FASTA path",
      opt[String]("name") action { (name, c) =>
        c.copy(name = name) // do we need to check that this is non-blank?
      } text "Name of ReferenceSet",
      opt[String]("organism") action { (organism, c) =>
        c.copy(organism = organism)
      } text "Organism",
      opt[String]("ploidy") action { (ploidy, c) =>
        c.copy(ploidy = ploidy)
      } text "Ploidy",
      opt[Int]("timeout") action { (t, c) =>
        c.copy(maxTime = t.seconds)
      } text s"Maximum time to poll for running job status in seconds (Default ${defaults.maxTime})",
      opt[String]("project") action { (p, c) =>
        c.copy(project = Some(p))
      } text "Name of project associated with this reference"
    ) text "Import Reference FASTA"

    note("\nIMPORT BARCODE\n")
    cmd(Modes.IMPORT_BARCODES.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.IMPORT_BARCODES)
    } children (
      arg[File]("fasta-path") required () action { (p, c) =>
        c.copy(path = p.toPath)
      } text "FASTA path",
      arg[String]("name") required () action { (name, c) =>
        c.copy(name = name)
      } text "Name of BarcodeSet",
      opt[String]("project") action { (p, c) =>
        c.copy(project = Some(p))
      } text "Name of project associated with these barcodes"
    ) text "Import Barcodes FASTA"

    note("\nIMPORT RSII MOVIE\n")
    cmd(Modes.IMPORT_MOVIE.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.IMPORT_MOVIE)
    } children (
      arg[File]("metadata-xml-path") required () action { (p, c) =>
        c.copy(path = p.toPath)
      } text "Path to RS II movie metadata XML file (or directory)",
      opt[String]("name") action { (name, c) =>
        c.copy(name = name)
      } text "Name of imported HdfSubreadSet",
      opt[String]("project") action { (p, c) =>
        c.copy(project = Some(p))
      } text "Name of project associated with this dataset"
    ) text "Import RS II movie metadata XML legacy format as HdfSubreadSet"

    note("\nRUN ANALYSIS JOB\n")
    cmd(Modes.ANALYSIS.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.ANALYSIS)
    } children (
      arg[File]("json-file") required () action { (p, c) =>
        c.copy(path = p.toPath)
      } text "JSON config file", // TODO validate json format
      opt[Unit]("block") action { (_, c) =>
        c.copy(block = true)
      } text "Block until job completes",
      opt[Int]("timeout") action { (t, c) =>
        c.copy(maxTime = t.seconds)
      } text s"Maximum time (in seconds) to poll for running job status (Default ${defaults.maxTime})"
    ) text "Run a pbsmrtpipe analysis pipeline from a JSON config file"

    note("\nTERMINATE ANALYSIS JOB\n")
    cmd(Modes.TERMINATE_JOB.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.TERMINATE_JOB)
    } children (
      arg[Int]("job-id") required () action { (p, c) =>
        c.copy(jobId = p)
      } text "SMRT Link Analysis Job Id"
    ) text "Terminate a SMRT Link Analysis Job By Int Id in the RUNNING state"

    note("\nDELETE JOB\n")
    cmd(Modes.DELETE_JOB.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.DELETE_JOB)
    } children (
      arg[String]("job-id") required () action { (i, c) =>
        c.copy(jobId = entityIdOrUuid(i))
      } validate { i =>
        validateId(i, "Job")
      } text "Job ID",
      opt[Unit]("force") action { (_, c) =>
        c.copy(force = true)
      } text "Force job delete even if it is still running or has active child jobs (NOT RECOMMENDED)"
    ) text "Delete a pbsmrtpipe job, including all output files"

    note("\nEXPORT JOB\n")
    cmd(Modes.EXPORT_JOB.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.EXPORT_JOB)
    } children (
      arg[String]("job-id") required () action { (i, c) =>
        c.copy(jobId = entityIdOrUuid(i))
      } validate { i =>
        validateId(i, "Job")
      } text "Job ID",
      opt[String]("output-dir") action { (p, c) =>
        c.copy(path = Paths.get(p))
      } text "Output directory for job ZIP file; must be writable by smrtlink",
      opt[Unit]("include-entry-points") action { (_, c) =>
        c.copy(includeEntryPoints = true)
      } text "Include input datasets in exported job ZIP file"
    ) text "Export a job to a ZIP file"

    note("\nIMPORT JOB\n")
    cmd(Modes.IMPORT_JOB.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.IMPORT_JOB)
    } children (
      arg[File]("zip-path") required () action { (f, c) =>
        c.copy(path = f.toPath)
      } text "Path to ZIP file exported by a SMRT Link server"
    ) text "Import a SMRT Link job from a ZIP file"

    note("\nEMIT ANALYSIS JSON TEMPLATE\n")
    cmd(Modes.TEMPLATE.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.TEMPLATE)
    } children () text "Emit an analysis.json template to stdout that can be run using 'run-analysis'"

    note("\nSHOW ANALYSIS PIPELINE TEMPLATES\n")
    cmd(Modes.SHOW_PIPELINES.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.SHOW_PIPELINES)
    } text "Display a list of available pbsmrtpipe pipelines"

    note("\nRUN ANALYSIS PIPELINE\n")
    cmd(Modes.PIPELINE.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.PIPELINE)
    } children (
      arg[String]("pipeline-id") required () action { (p, c) =>
        c.copy(pipelineId = p)
      } text "Pipeline ID to run",
      opt[String]('e', "entry-point") minOccurs (1) maxOccurs (1024) action {
        (e, c) =>
          c.copy(entryPoints = c.entryPoints :+ e)
      } text "Entry point (must be valid PacBio DataSet)",
      opt[File]("preset-xml") action { (x, c) =>
        c.copy(presetXml = Some(x.toPath))
      } text "XML file specifying pbsmrtpipe options",
      opt[String]("job-title") action { (t, c) =>
        c.copy(jobTitle = t)
      } text "Job title (will be displayed in UI)",
      opt[Unit]("block") action { (_, c) =>
        c.copy(block = true)
      } text "Block until job completes",
      opt[Int]("timeout") action { (t, c) =>
        c.copy(maxTime = t.seconds)
      } text s"Maximum time (in seconds) to poll for running job status (Default ${defaults.maxTime})",
      opt[Map[String, String]]("task-options")
        .valueName("k1=v1,k2=v2...")
        .action { (x, c) =>
          c.copy(taskOptions = Some(x))
        }
        .text("Pipeline task options as comma-separated option_id=value list")
    ) text "Run a pbsmrtpipe pipeline by name on the server"

    note("\nGET SMRTLINK JOB\n")
    cmd(Modes.JOB.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.JOB)
    } children (
      arg[String]("job-id") required () action { (i, c) =>
        c.copy(jobId = entityIdOrUuid(i))
      } validate { i =>
        validateId(i, "Job")
      } text "Job ID",
      opt[Unit]("show-settings") action { (_, c) =>
        c.copy(dumpJobSettings = true)
      } text "Print JSON settings for job, suitable for input to 'pbservice run-analysis'",
      opt[Unit]("show-reports") action { (_, c) =>
        c.copy(showReports = true)
      } text "Display job report attributes"
    ) text "Show job details"

    note("\nGET SMRTLINK JOB LIST\n")
    cmd(Modes.JOBS.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.JOBS)
    } children (
      opt[Int]('m', "max-items") action { (m, c) =>
        c.copy(maxItems = m)
      } text s"Max number of jobs to show (Default: ${defaults.maxItems})",
      opt[String]('t', "job-type") action { (t, c) =>
        c.copy(jobType = t)
      } validate { t =>
        validateJobType(t)
      } text "Only retrieve jobs of specified type",
      opt[String]('s', "job-state") action { (s, c) =>
        c.copy(jobState = Some(s)) // This should validate the job state.
      } text "Only display jobs in specified state (e.g., CREATED, SUCCESSFUL, RUNNING, FAILED)"
    )

    note("\nGET SMRTLINK DATASET DETAILS\n")
    cmd(Modes.DATASET.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.DATASET)
    } children (
      arg[String]("dataset-id") required () action { (i, c) =>
        c.copy(datasetId = entityIdOrUuid(i))
      } validate { i =>
        validateId(i, "Dataset")
      } text "Dataset ID"
    ) text "Show dataset details"

    note("\nGET SMRTLINK DATASET LIST\n")
    cmd(Modes.DATASETS.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.DATASETS)
    } children (
      arg[String]("dataset-type")
        .required()
        .text(DS_META_TYPE_NAME)
        .validate(t => validateDataSetMetaType(t))
        .action { (t, c) =>
          c.copy(datasetType = DataSetMetaTypes.fromAnyName(t).get)
        },
      opt[Int]('m', "max-items") action { (m, c) =>
        c.copy(maxItems = m)
      } text "Max number of Datasets to show",
      opt[String]("search-name") action { (n, c) =>
        c.copy(searchName = Some(n))
      } text "Search for datasets whose 'name' field matches the specified string",
      opt[String]("search-path") action { (p, c) =>
        c.copy(searchPath = Some(p))
      } text "Search for datasets whose 'path' field matches the specified string"
    )

    note("\nDELETE SMRTLINK DATASET\n")
    cmd(Modes.DELETE_DATASET.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.DELETE_DATASET)
    } children (
      arg[String]("dataset-id") required () action { (i, c) =>
        c.copy(datasetId = entityIdOrUuid(i))
      } validate { i =>
        validateId(i, "Dataset")
      } text "Dataset ID"
    ) text "Soft-delete of a dataset (won't remove files)"

    note("\nGET SMRTLINK MANIFESTS\n")
    cmd(Modes.MANIFESTS.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.MANIFESTS)
    } text "Get a List of SMRT Link PacBio Component Versions"

    note("\nGET SMRTLINK MANIFEST DETAILS\n")
    cmd(Modes.MANIFEST.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.MANIFEST)
    } children (
      opt[String]('i', "manifest-id") action { (t, c) =>
        c.copy(manifestId = t)
      } text s"Manifest By Id (Default: ${defaults.manifestId})"
    ) text "Get PacBio Component Manifest version by Id."

    note("\nGET PACBIO LOADED BUNDLES\n")
    cmd(Modes.BUNDLES.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.BUNDLES)
    } text "Get a List of PacBio Data Bundles registered to SMRT Link"

    cmd(Modes.CREATE_PROJECT.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.CREATE_PROJECT)
    } children (
      arg[String]("name") required () action { (n, c) =>
        c.copy(name = n)
      } text "Project name",
      arg[String]("description") required () action { (d, c) =>
        c.copy(description = d)
      } text "Project description"
    ) text "Start a new project"

    note("\nTECH SUPPORT SYSTEM STATUS REQUEST\n")
    cmd(Modes.TS_STATUS.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.TS_STATUS)
    } children (
      opt[String]("comment") action { (s, c) =>
        c.copy(comment = s)
      } text s"Comments to include (default: ${defaults.comment})"
    ) text "Send system status report to PacBio Tech Support"

    note("\nTECH SUPPORT FAILED JOB REQUEST\n")
    cmd(Modes.TS_JOB.name) action { (_, c) =>
      c.copy(command = (c) => println(c), mode = Modes.TS_JOB)
    } children (
      arg[String]("job-id") required () action { (i, c) =>
        c.copy(jobId = entityIdOrUuid(i))
      } text "ID of job whose details should be sent to tech support",
      opt[String]("comment") action { (s, c) =>
        c.copy(comment = s)
      } text s"Comments to include (default: ${defaults.comment})"
    ) text "Send failed job information to PacBio Tech Support"

    note("\nSMRT Link ALARMS\n")
    cmd(Modes.ALARMS.name)
      .action { (_, c) =>
        c.copy(command = (c) => println(c), mode = Modes.ALARMS)
      }
      .text("Get a List of SMRT Link System Alarm")

    // Don't show the help if validation error
    override def showUsageOnError = false
  }
}

// TODO consolidate Try behavior
class PbService(val sal: SmrtLinkServiceClient, val maxTime: FiniteDuration)
    extends LazyLogging
    with ClientUtils
    with DaoFutureUtils {
  import CommonModelImplicits._
  import CommonModels._
  import com.pacbio.secondary.smrtlink.jsonprotocols.SmrtLinkJsonProtocols._

  // the is the default for timeout for common tasks
  protected val TIMEOUT = 30 seconds
  private lazy val defaultPresets = PipelineTemplatePreset(
    "default",
    "any",
    Seq[ServiceTaskOptionBase](),
    Seq[ServiceTaskOptionBase]())
  private lazy val rsMovieName =
    """m([0-9]{6})_([0-9a-z]{5,})_([0-9a-z]{5,})_c([0-9]{16,})_(\w\d)_(\w\d)""".r

  private def matchRsMovieName(file: File): Boolean =
    rsMovieName.findPrefixMatchOf(file.getName).isDefined

  // FIXME this is crude
  private def printAndExit(msg: String, exitCode: Int): Int = {
    println(msg)
    exitCode
  }

  protected def errorExit(msg: String, exitCode: Int = 1) = {
    System.err.println(msg)
    exitCode
  }

  protected def printMsg(msg: String) = printAndExit(msg, 0)

  protected def logMsg(msg: String) = {
    logger.info(msg)
    0
  }

  protected def showNumRecords(label: String,
                               numPad: Int,
                               fn: () => Future[Int]): Unit = {
    Try { Await.result(fn(), TIMEOUT) } match {
      case Success(nrecords) =>
        println(s"${label.padTo(numPad, ' ')} $nrecords")
      case Failure(err) => println(s"ERROR: couldn't retrieve $label")
    }
  }

  protected def runAndSummary[T](fx: Try[T], summary: (T => String)): Int = {
    fx match {
      case Success(result) => printMsg(summary(result))
      case Failure(ex) => errorExit(ex.getMessage, 1)
    }
  }

  protected def runAndBlock[T](fx: => Future[T],
                               summary: (T => String),
                               timeout: FiniteDuration): Int = {
    runAndSummary(Try(Await.result[T](fx, timeout)), summary)
  }

  def statusSummary(status: ServiceStatus): String = {
    val headers = Seq("ID", "UUID", "Version", "Message")
    val table = Seq(
      Seq(status.id.toString,
          status.uuid.toString,
          status.version,
          status.message))
    printTable(table, headers)
    ""
  }

  protected def isCompatibleVersion(): Boolean = {
    val fx = for {
      status <- sal.getStatus
      _ <- isVersionGteSystemVersion(status)
    } yield true

    Try(Await.result(fx, TIMEOUT)).getOrElse(false)
  }

  def systemDataSetSummary(): Future[String] = {
    for {
      numSubreadSets <- sal.getSubreadSets.map(_.length)
      numHdfSubreadSets <- sal.getHdfSubreadSets.map(_.length)
      numReferenceSets <- sal.getReferenceSets.map(_.length)
      numGmapReferenceSets <- sal.getGmapReferenceSets.map(_.length)
      numAlignmenSets <- sal.getAlignmentSets.map(_.length)
      numBarcodeSets <- sal.getBarcodeSets.map(_.length)
      numConsensusReadSets <- sal.getConsensusReadSets.map(_.length)
      numConsensusAlignmentSets <- sal.getConsensusAlignmentSets.map(_.length)
      numContigSets <- sal.getContigSets.map(_.length)
    } yield s"""
        |DataSet Summary (active datasets) :
        |
        |SubreadSets            : $numSubreadSets
        |HdfSubreadSets         : $numHdfSubreadSets
        |ReferenceSets          : $numReferenceSets
        |GmapReferenceSets      : $numGmapReferenceSets
        |BarcodeSets            : $numBarcodeSets
        |AlignmentSets          : $numAlignmenSets
        |ConsensusAlignmentSets : $numConsensusAlignmentSets
        |ConsensusReadSets      : $numConsensusReadSets
        |ContigSets             : $numContigSets
      """.stripMargin
  }

  def systemJobSummary(): Future[String] = {
    for {
      numImportJobs <- sal.getImportJobs.map(_.length)
      numMergeJobs <- sal.getMergeJobs.map(_.length)
      numAnalysisJobs <- sal.getAnalysisJobs.map(_.length)
      numConvertFastaJobs <- sal.getFastaConvertJobs.map(_.length)
      numConvertFastaBarcodeJobs <- sal.getBarcodeConvertJobs.map(_.length)
      numTsSystemBundleJobs <- Future.successful(0) // These need to be added
      numTsFailedJobBundleJob <- Future.successful(0)
      numDbBackUpJobs <- Future.successful(0)
    } yield s"""
        |System Job Summary by job type:
        |
        |Import DataSet                    : $numImportJobs
        |Merge DataSet                     : $numMergeJobs
        |Analysis                          : $numAnalysisJobs
        |Convert Fasta to ReferenceSet     : $numConvertFastaJobs
        |Convert Fasta to BarcodeSet       : $numConvertFastaBarcodeJobs
      """.stripMargin
  }

  protected def printStatus(status: ServiceStatus,
                            asJson: Boolean = false): Int = {
    if (asJson) {
      println(status.toJson.prettyPrint)
    } else {
      statusSummary(status)
      println("")
      println("DataSet Summary:")
      // To have the clear summary of the output
      val numPad = 30
      val showRecords = showNumRecords(_: String, numPad, _: () => Future[Int])

      showRecords("SubreadSets", () => sal.getSubreadSets.map(_.length))
      showRecords("HdfSubreadSets", () => sal.getHdfSubreadSets.map(_.length))
      showRecords("ReferenceSets", () => sal.getReferenceSets.map(_.length))
      showRecords("BarcodeSets", () => sal.getBarcodeSets.map(_.length))
      showRecords("AlignmentSets", () => sal.getAlignmentSets.map(_.length))
      showRecords("ConsensusReadSets",
                  () => sal.getConsensusReadSets.map(_.length))
      showRecords("ConsensusAlignmentSets",
                  () => sal.getConsensusAlignmentSets.map(_.length))
      showRecords("ContigSets", () => sal.getContigSets.map(_.length))
      showRecords("GmapReferenceSets",
                  () => sal.getGmapReferenceSets.map(_.length))
      println("\nSMRT Link Job Summary:")
      showRecords("import-dataset Jobs", () => sal.getImportJobs.map(_.length))
      showRecords("merge-dataset Jobs", () => sal.getMergeJobs.map(_.length))
      showRecords("convert-fasta-reference Jobs",
                  () => sal.getFastaConvertJobs.map(_.length))
      showRecords("pbsmrtpipe Jobs", () => sal.getAnalysisJobs.map(_.length))
    }
    0
  }

  /**
    * Util to get the Pbservice Client and Server compatibility status
    *
    * @param status Remote SMRT Link Server status
    * @return
    */
  def compatibilitySummary(status: ServiceStatus): Future[String] = {
    def msg(sx: String) =
      s"Pbservice ${Constants.SMRTFLOW_VERSION} $sx compatible with Server ${status.version}"

    val fx = isVersionGteSystemVersion(status).map(_ => msg("IS"))

    fx.recover { case NonFatal(_) => msg("IS NOT") }
  }

  /**
    * Core Summary for the Server Status
    *
    * If asJson is provided, the system summary is no longer relevent and will
    * be skipped.
    *
    * @param asJson to emit the System Status as JSON.
    * @return
    */
  def exeStatus(asJson: Boolean = false): Future[String] = {

    // This isn't quite correct, but statusSummary is doing the printing
    def statusFullSummary(status: ServiceStatus): Future[String] = {
      for {
        statusSummaryMsg <- Future
          .successful(status)
          .map(status => statusSummary(status))
        compatSummaryMsg <- compatibilitySummary(status)
        dataSetSummaryMsg <- systemDataSetSummary()
        jobSummaryMsg <- systemJobSummary()
      } yield
        s"$statusSummaryMsg\n$compatSummaryMsg\n$dataSetSummaryMsg\n$jobSummaryMsg"
    }

    def statusJsonSummary(status: ServiceStatus): Future[String] =
      Future.successful(status.toJson.prettyPrint.toString)

    val summary: (ServiceStatus => Future[String]) =
      if (asJson) statusJsonSummary else statusFullSummary
    sal.getStatus.flatMap(summary)
  }

  def runStatus(asJson: Boolean = false): Int = {
    def printer(sx: ServiceStatus): String = {
      printStatus(sx, asJson)
      ""
    }
    runAndBlock[ServiceStatus](sal.getStatus, printer, TIMEOUT)
  }

  def getDataSet(datasetId: IdAble): Try[DataSetMetaDataSet] = Try {
    Await.result(sal.getDataSet(datasetId), TIMEOUT)
  }

  def runGetDataSetInfo(datasetId: IdAble, asJson: Boolean = false): Int = {
    getDataSet(datasetId) match {
      case Success(ds) => printDataSetInfo(ds, asJson)
      case Failure(err) =>
        errorExit(s"Could not retrieve existing dataset record: $err")
    }
  }

  def runGetDataSets(dsType: DataSetMetaTypes.DataSetMetaType,
                     maxItems: Int,
                     asJson: Boolean = false,
                     searchName: Option[String] = None,
                     searchPath: Option[String] = None): Future[String] = {
    def isMatching(ds: ServiceDataSetMetadata): Boolean = {
      val qName = searchName.map(n => ds.name contains n).getOrElse(true)
      val qPath = searchPath.map(p => ds.path contains p).getOrElse(true)
      qName && qPath
    }
    def fx: Future[Seq[(ServiceDataSetMetadata, JsValue)]] = dsType match {
      case DataSetMetaTypes.Subread =>
        sal.getSubreadSets.map(_.map(ds => (ds, ds.toJson)))
      case DataSetMetaTypes.HdfSubread =>
        sal.getHdfSubreadSets.map(_.map(ds => (ds, ds.toJson)))
      case DataSetMetaTypes.Barcode =>
        sal.getBarcodeSets.map(_.map(ds => (ds, ds.toJson)))
      case DataSetMetaTypes.Reference =>
        sal.getReferenceSets.map(_.map(ds => (ds, ds.toJson)))
      case DataSetMetaTypes.GmapReference =>
        sal.getGmapReferenceSets.map(_.map(ds => (ds, ds.toJson)))
      case DataSetMetaTypes.Contig =>
        sal.getContigSets.map(_.map(ds => (ds, ds.toJson)))
      case DataSetMetaTypes.Alignment =>
        sal.getAlignmentSets.map(_.map(ds => (ds, ds.toJson)))
      case DataSetMetaTypes.AlignmentCCS =>
        sal.getConsensusAlignmentSets.map(_.map(ds => (ds, ds.toJson)))
      case DataSetMetaTypes.CCS =>
        sal.getConsensusReadSets.map(_.map(ds => (ds, ds.toJson)))
    }
    for {
      records <- fx
      filtered <- Future.successful(records.filter(r => isMatching(r._1)))
      outstr <- Future.successful {
        if (asJson) {
          filtered.map(_._2).map(_.prettyPrint).mkString(",\n")
        } else {
          val table = filtered.map(_._1).reverse.take(maxItems).map { ds =>
            Seq(ds.id.toString, ds.uuid.toString, ds.name, ds.path)
          }
          toTable(table, Seq("ID", "UUID", "Name", "Path"))
        }
      }
    } yield outstr
  }

  def runGetJobInfo(jobId: IdAble,
                    asJson: Boolean = false,
                    dumpJobSettings: Boolean = false,
                    showReports: Boolean = false): Int = {
    Try { Await.result(sal.getJob(jobId), TIMEOUT) } match {
      case Success(job) =>
        var rc = printJobInfo(job, asJson, dumpJobSettings)
        if (showReports && (rc == 0)) {
          rc = Try {
            Await.result(sal.getJobReports(job.uuid), TIMEOUT)
          } match {
            case Success(rpts) =>
              rpts
                .map { dsr =>
                  Try {
                    Await.result(sal.getJobReport(job.uuid,
                                                  dsr.dataStoreFile.uuid),
                                 TIMEOUT)
                  } match {
                    case Success(rpt) => showReportAttributes(rpt)
                    case Failure(err) =>
                      errorExit(
                        s"Couldn't retrieve report ${dsr.dataStoreFile.uuid} for job ${job.uuid}: $err")
                  }
                }
                .reduceLeft(_ max _)
            case Failure(err) => errorExit(s"Could not retrieve reports: $err")
          }
        }
        rc
      case Failure(err) => errorExit(s"Could not retrieve job record: $err")
    }
  }

  def jobsSummary(maxItems: Int,
                  asJson: Boolean,
                  engineJobs: Seq[EngineJob],
                  jobState: Option[String] = None): String = {
    if (asJson) {
      println(engineJobs.take(maxItems).toJson.prettyPrint)
      ""
    } else {
      val table = engineJobs
        .sortBy(_.id)
        .reverse
        .filter(job => jobState.map(_ == job.state.toString).getOrElse(true))
        .take(maxItems)
        .map(
          job =>
            Seq(job.id.toString,
                job.state.toString,
                job.name,
                job.uuid.toString,
                job.createdBy.getOrElse("")))
      printTable(table, Seq("ID", "State", "Name", "UUID", "CreatedBy"))
      ""
    }
  }

  def runGetJobs(maxItems: Int,
                 asJson: Boolean = false,
                 jobType: String = "pbsmrtpipe",
                 jobState: Option[String] = None): Int = {
    def printer(jobs: Seq[EngineJob]) =
      jobsSummary(maxItems, asJson, jobs, jobState)

    runAndBlock[Seq[EngineJob]](sal.getJobsByType(jobType), printer, TIMEOUT)
  }

  protected def waitForJob(jobId: IdAble): Int = {
    logger.info(s"waiting for job ${jobId.toIdString} to complete...")
    sal.pollForSuccessfulJob(jobId, Some(maxTime)) match {
      case Success(msg) => runGetJobInfo(jobId)
      case Failure(err) => {
        runGetJobInfo(jobId)
        errorExit(err.getMessage)
      }
    }
  }

  private def importFasta(path: Path,
                          dsType: FileTypes.DataSetBaseType,
                          runJob: => Future[EngineJob],
                          projectName: Option[String],
                          barcodeMode: Boolean = false): Int = {
    val projectId = getProjectIdByName(projectName)
    if (projectId < 0)
      return errorExit("Can't continue with an invalid project.")
    val tx = for {
      contigs <- Try { PacBioFastaValidator.validate(path, barcodeMode) }
      job <- Try { Await.result(runJob, TIMEOUT) }
      successfulJob <- sal.pollForSuccessfulJob(job.id, Some(maxTime))
      dataStoreFiles <- Try {
        Await.result(sal.getJobDataStore(successfulJob.id), TIMEOUT)
      }
    } yield dataStoreFiles

    tx match {
      case Success(dataStoreFiles) =>
        dataStoreFiles.find(_.fileTypeId == dsType.fileTypeId) match {
          case Some(ds) =>
            runGetDataSetInfo(ds.uuid)
            if (projectId > 0) addDataSetToProject(ds.uuid, projectId) else 0
          case None => errorExit(s"Couldn't find ${dsType.dsName}")
        }
      case Failure(err) => errorExit(s"Import job error: ${err.getMessage}")
    }
  }

  def runImportFasta(path: Path,
                     name: String,
                     organism: String,
                     ploidy: String,
                     projectName: Option[String] = None): Int = {
    // this really shouldn't be optional
    val nameFinal = if (name.isEmpty) "unknown" else name
    importFasta(path,
                FileTypes.DS_REFERENCE,
                sal.importFasta(path, nameFinal, organism, ploidy),
                projectName)
  }

  def runImportBarcodes(path: Path,
                        name: String,
                        projectName: Option[String] = None): Int =
    importFasta(path,
                FileTypes.DS_BARCODE,
                sal.importFastaBarcodes(path, name),
                projectName,
                barcodeMode = true)

  private def importXmlRecursive(path: Path,
                                 listFilesOfType: File => Array[File],
                                 doImportOne: Path => Int,
                                 doImportMany: Path => Int): Int = {
    val f = path.toFile
    if (f.isDirectory) {
      val xmlFiles = listFilesOfType(f)
      if (xmlFiles.isEmpty) {
        errorExit(s"No valid XML files found in ${f.getAbsolutePath}")
      } else {
        println(s"Found ${xmlFiles.length} matching XML files")
        val failed: ListBuffer[String] = ListBuffer()
        xmlFiles.foreach { xmlFile =>
          println(s"Importing ${xmlFile.getAbsolutePath}...")
          val rc = doImportMany(xmlFile.toPath)
          if (rc != 0) failed.append(xmlFile.getAbsolutePath.toString)
        }
        if (failed.nonEmpty) {
          println(s"${failed.size} import(s) failed:")
          failed.foreach { println }
          1
        } else 0
      }
    } else if (f.isFile) doImportOne(f.toPath)
    else errorExit(s"${f.getAbsolutePath} is not readable")
  }

  def runImportDataSetSafe(path: Path, projectId: Int = 0): Int = {
    val dsMiniMeta = getDataSetMiniMeta(path)
    def importDs = {
      val rc = runImportDataSet(path, dsMiniMeta.metatype)
      if (rc == 0) {
        runGetDataSetInfo(dsMiniMeta.uuid)
        if (projectId > 0) addDataSetToProject(dsMiniMeta.uuid, projectId)
        else 0
      } else rc
    }
    logger.info(s"UUID: ${dsMiniMeta.uuid.toString}")
    def errorIfNewPath(dsInfo: DataSetMetaDataSet) =
      println(s"ERROR: The dataset UUID (${dsInfo.uuid.toString}) is already " +
        "present in the database but associated with a different path; if you " +
        "want to re-import with the new path, run this command:\n\n  dataset " +
        s"newuuid ${path.toString}\n\nthen run the pbservice command again to " +
        "import the modified dataset.\n")
    def warnIfNewPath(dsInfo: DataSetMetaDataSet) = {
      println(
        s"WARNING: The dataset UUID (${dsInfo.uuid.toString}) is already " +
          "present in the database but associated with a different path; will " +
          "re-import with a different path, but this will overwrite the " +
          "existing record.")
      Thread.sleep(5000)
    }
    getDataSet(dsMiniMeta.uuid) match {
      case Success(dsInfo) => {
        if (Paths.get(dsInfo.path) != path) {
          if (isCompatibleVersion()) {
            warnIfNewPath(dsInfo)
            importDs
          } else errorIfNewPath(dsInfo)
        } else
          logger.info(s"Dataset ${dsMiniMeta.uuid.toString} already imported.")
        // printDataSetInfo(dsInfo)
        logger.info(toDataSetInfoSummary(dsInfo))
        0
      }
      case Failure(err) => {
        println(s"No existing dataset record found")
        importDs
      }
    }
  }

  def runImportDataSet(path: Path,
                       dsType: DataSetMetaTypes.DataSetMetaType,
                       asJson: Boolean = false): Int = {
    logger.info(s"DataSet Metatype $dsType")
    Try { Await.result(sal.importDataSet(path, dsType), TIMEOUT) } match {
      case Success(jobInfo: EngineJob) => waitForJob(jobInfo.uuid)
      case Failure(err) => errorExit(s"Dataset import failed: $err")
    }
  }

  /**
    * List all well-formed PacBio DataSet XML Files ending with *set.xml
    *
    * @param f Root Directory
    * @return
    */
  private def listDataSetFiles(f: File): Array[File] = {

    def metaDataFilter(fn: File): Boolean =
      Try { getDataSetMiniMeta(fn.toPath) }.isSuccess

    val allFiles = f.listFiles

    allFiles
      .filter(_.isFile)
      .filter(_.getName.endsWith("set.xml"))
      .filter(metaDataFilter) ++ allFiles
      .filter(_.isDirectory)
      .flatMap(listDataSetFiles)
  }

  /**
    * Get Project by (unique) name or Fail future.
    *
    * @param name Project Name
    * @return
    */
  def getProjectByName(name: String): Future[Project] = {
    sal.getProjects.flatMap(
      projects =>
        failIfNone(s"Unable to find project $name")(
          projects.find(_.name == name)))
  }

  /**
    * Engine Job Summary
    *
    * @param job    Engine Job
    * @param asJson Convert summary to JSON
    * @return
    */
  def jobSummary(job: EngineJob, asJson: Boolean): String = {
    def jobJsonSummary(job: EngineJob) = job.toJson.prettyPrint.toString

    if (asJson) jobJsonSummary(job)
    else toJobSummary(job)
  }

  private def jobFailure(job: EngineJob) = {
    logger.error(s"Job ${job.id} failed!")
    Future.failed(
      new RuntimeException(s"Error running job: ${job.errorMessage}"))
  }

  def jobSummaryOrFailure(job: EngineJob, asJson: Boolean): Future[String] = {
    if (!job.isSuccessful) {
      jobFailure(job)
    } else {
      Future.successful(jobSummary(job, asJson))
    }
  }

  def engineDriver(job: EngineJob,
                   maxTime: Option[FiniteDuration]): Future[EngineJob] = {
    // This is blocking polling call is wrapped in a Future
    maxTime
      .map(t => Future.fromTry(sal.pollForCompletedJob(job.id, Some(t))))
      .getOrElse(Future.successful(job))
  }

  /**
    * Run a single DataSet from a file that exists on the server
    *
    * @param path       Path to the remote dataset
    * @param metatype   DataSet metatype
    * @param asJson     summary of the job format
    * @param maxTimeOut If provided, the job will be polled for maxTime until a completed state
    * @return
    */
  def runSingleNonLocalDataSetImport(
      path: Path,
      metatype: DataSetMetaTypes.DataSetMetaType,
      asJson: Boolean,
      maxTimeOut: Option[FiniteDuration]): Future[String] = {
    for {
      job <- sal.importDataSet(path, metatype)
      completedJob <- engineDriver(job, maxTimeOut)
      summary <- jobSummaryOrFailure(completedJob, asJson)
    } yield summary
  }

  /**
    * Get Existing dataset and companion job, or create a new import dataset job.
    *
    * @param uuid       UUID of dataset
    * @param metatype   DataSet metatype
    * @param path       Path to dataset
    * @param maxTimeOut If provided, the job will be polled for maxTime until a completed state
    * @return
    */
  def getDataSetJobOrImport(
      uuid: UUID,
      metatype: DataSetMetaTypes.DataSetMetaType,
      path: Path,
      maxTimeOut: Option[FiniteDuration]): Future[EngineJob] = {

    def logIfPathIsDifferent(ds: DataSetMetaDataSet): DataSetMetaDataSet = {
      if (ds.path != path.toString) {
        val msg =
          s"DataSet Path on Server will attempted to be updated to $path from ${ds.path}"
        logger.warn(msg)
        System.err.println(msg)
      }
      ds
    }

    // The dataset has already been imported. Skip the entire job creation process.
    // This assumes that the Job was successful (because the datastore was imported)
    val fx = for {
      ds <- sal.getDataSet(uuid)
      _ <- Future.successful(logIfPathIsDifferent(ds))
      job <- sal.getJob(ds.jobId)
      completedJob <- engineDriver(job, maxTimeOut)
    } yield completedJob

    // Default to creating new Job if the dataset wasn't already imported into the system
    val orCreate = for {
      job <- sal.importDataSet(path, metatype)
      completedJob <- engineDriver(job, maxTimeOut)
    } yield completedJob

    // This should have a tighter exception case
    fx.recoverWith { case NonFatal(_) => orCreate }
  }

  /**
    * Import or Get Already imported dataset from DataSet XML Path
    *
    * @param path Path to PacBio DataSet XML file
    * @param asJson emit the Import DataSet Job as JSON
    * @param maxTimeOut Max time to Poll for blocking job
    * @return
    */
  def runSingleLocalDataSetImport(
      path: Path,
      asJson: Boolean,
      maxTimeOut: Option[FiniteDuration]): Future[EngineJob] = {
    logger.debug(s"Attempting to import dataset from $path")

    for {
      m <- Future.fromTry(Try(getDataSetMiniMeta(path)))
      job <- getDataSetJobOrImport(m.uuid, m.metatype, path, maxTimeOut)
      completedJob <- engineDriver(job, maxTimeOut)
    } yield completedJob
  }

  /**
    * The Import Summary is dependent on the input provided and if the job is blocking.
    *
    * @param job     Engine Job
    * @param dataset DataSet metadata (only if the job is blocking)
    * @param asJson  emit results as JSON. When asJson is provided, ONLY the Job entity is returned.
    * @return
    */
  def importSummary(job: EngineJob,
                    dataset: Option[DataSetMetaDataSet],
                    asJson: Boolean): String = {
    if (asJson) {
      jobSummary(job, asJson)
    } else {
      dataset
        .map(d => s"${toDataSetInfoSummary(d)}${jobSummary(job, asJson)}")
        .getOrElse(jobSummary(job, asJson))
    }
  }

  def runSingleLocalDataSetImportWithSummary(
      path: Path,
      asJson: Boolean,
      maxTimeOut: Option[FiniteDuration]): Future[String] = {

    // Only when the maxTimeOut is provided will the job poll and complete. Then the dataset summary can
    // be displayed
    def generateSummary(job: EngineJob, dsUUID: UUID): Future[String] = {
      if (job.isSuccessful) {
        maxTimeOut
          .map(
            t =>
              sal
                .getDataSet(dsUUID)
                .map(m => importSummary(job, Some(m), asJson)))
          .getOrElse(Future.successful(importSummary(job, None, asJson)))
      } else {
        jobFailure(job)
      }
    }

    for {
      m <- Future.fromTry(Try(getDataSetMiniMeta(path)))
      completedJob <- runSingleLocalDataSetImport(path, asJson, maxTimeOut)
      summary <- generateSummary(completedJob, m.uuid)
    } yield summary
  }

  def validateJobWasSuccessful(job: EngineJob) = job.isSuccessful
  def validateJobNotFailed(job: EngineJob) = !job.hasFailed

  /**
    * Recursively import from a Directory of all dataset files ending in *.xml and that are valid
    * PacBio DataSets.
    *
    * This is not the greatest idea. This should really be a streaming model
    *
    * @param files      List of files to import
    * @param maxTimeOut Max time out PER dataset import
    * @return
    */
  def runMultiImportDataSet(
      files: Seq[File],
      maxTimeOut: Option[FiniteDuration]): Future[String] = {

    // If maxTimeOut is provided, we poll the job to completion and expect the Job to be in a successful state
    // If not provided, we only expect the job is not in a failure state (e.g, CREATED, RUNNING)
    val jobFilter: (EngineJob => Boolean) = maxTimeOut match {
      case Some(_) => validateJobWasSuccessful
      case _ => validateJobNotFailed
    }

    logger.info(s"Attempting to import ${files.length} PacBio DataSet(s)")
    if (files.isEmpty) {
      // Not sure if this should raise
      Future.failed(
        throw new UnprocessableEntityError(
          s"No valid XML files found to process"))
    } else {
      // Note, these futures will be run in parallel. This needs a better error communication model.
      val fx = for {
        jobs <- Future.sequence(
          files.map(
            f =>
              runSingleLocalDataSetImport(f.toPath.toAbsolutePath,
                                          asJson = false,
                                          maxTimeOut)))
        summary <- multiJobSummary(jobs, jobFilter)
      } yield summary

      fx
    }
  }

  /**
    * Get a Summary from a List of Jobs
    *
    * @param jobs List of Jobs
    * @param jobValidator Function to determine if the job (and hence the job list) was in the expected job state(s)
    * @return
    */
  def multiJobSummary(jobs: Seq[EngineJob],
                      jobValidator: (EngineJob => Boolean)): Future[String] = {
    val wasSuccessful = jobs.map(jobValidator).reduce(_ && _)
    val failedJobs = jobs.filter(j => !j.isSuccessful)

    if (wasSuccessful) {
      // Should this print to stdout?
      jobs.foreach(job => logger.info(jobSummary(job, false)))
      Future.successful(s"Successfully ran ${jobs.length} import-dataset jobs")
    } else {
      failedJobs.foreach { job =>
        val summary = jobSummary(job, false)
        logger.error(summary)
        System.err.println(summary)
      }
      Future.failed(throw new Exception(
        s"${failedJobs.length} out of ${jobs.length} import-dataset jobs failed."))
    }

  }

  /**
    * Central Location to import datasets into SL
    *
    * Four cases
    *
    * 1. A single XML file local to where pbservice is executed is provided
    * 2. A FOFN of dataset XML paths is provided
    * 3. A directory is provided that is local to where pbservice is executed is provided
    * 4. A non-local import (i.e., referencing a dataset that is local to file system where the server is running
    * but not to where pbservice is exed. This requires the dataset type to be provided). The path must be an XML
    * file, not a directory.
    *
    *
    * Note, the non-local case also can not try to see if the dataset has already been imported. Whereas the
    * local case, the dataset can be read in and checked to see if was already imported (This really should be
    * encapsulated on the server side to return an previously run job?)
    *
    * @param path         Path to the DataSet XML
    * @param datasetType  DataSet metadata for non-local imports
    * @param asJson       emit the response as JSON (only when a single XML file is provided)
    * @param blockingMode Block until job is completed (failed, or successful)
    * @return A summary of the import process
    */
  def execImportDataSets(path: Path,
                         datasetType: Option[DataSetMetaTypes.DataSetMetaType],
                         asJson: Boolean = false,
                         blockingMode: Boolean = true): Future[String] = {

    // In blocking model, set the default timeout to None
    val maxTimeOut = if (blockingMode) Some(maxTime) else None

    if (path.toAbsolutePath.toFile.isDirectory) {

      val files: Seq[File] =
        if (path.toFile.isDirectory) listDataSetFiles(path.toFile).toSeq
        else Seq.empty[File]

      logger.debug(
        s"Found ${files.length} PacBio XML files from root dir $path")

      runMultiImportDataSet(files, maxTimeOut)
    } else if (path.toAbsolutePath.toFile.isFile && path.toAbsolutePath
                 .endsWith(".fofn")) {

      logger.debug(s"Detected file of file names (FOFN) mode from $path")

      val files = Utils
        .fofnToFiles(path.toAbsolutePath)
        .filter(p => Try { getDataSetMiniMeta(p) }.isSuccess)
        .map(_.toFile)

      runMultiImportDataSet(files, maxTimeOut)
    } else {
      datasetType match {
        case Some(dsType) =>
          runSingleNonLocalDataSetImport(path.toAbsolutePath,
                                         dsType,
                                         asJson,
                                         maxTimeOut)
        case _ =>
          runSingleLocalDataSetImportWithSummary(path.toAbsolutePath,
                                                 asJson,
                                                 maxTimeOut)
      }
    }
  }

  def runImportRsMovie(path: Path, name: String, projectId: Int = 0): Int = {
    val fileName = path.toAbsolutePath
    if (fileName.endsWith(".fofn") && (name == "")) {
      return errorExit(s"--name argument is required when an FOFN is input")
    }
    val tx = for {
      finalName <- Try { if (name == "") dsNameFromRsMetadata(path) else name }
      job <- Try { Await.result(sal.convertRsMovie(path, finalName), TIMEOUT) }
      job <- sal.pollForSuccessfulJob(job.uuid)
      dataStoreFiles <- Try {
        Await.result(sal.getJobDataStore(job.uuid), TIMEOUT)
      }
    } yield dataStoreFiles

    tx match {
      case Success(dataStoreFiles) =>
        dataStoreFiles.find(
          _.fileTypeId == FileTypes.DS_HDF_SUBREADS.fileTypeId) match {
          case Some(ds) =>
            runGetDataSetInfo(ds.uuid)
            if (projectId > 0) addDataSetToProject(ds.uuid, projectId) else 0
          case None => errorExit(s"Couldn't find HdfSubreadSet")
        }
      case Failure(err) =>
        errorExit(s"RSII movie import failed: ${err.getMessage}")
    }
  }

  private def listMovieMetadataFiles(f: File): Array[File] = {
    f.listFiles
      .filter((fn) =>
        matchRsMovieName(fn) && Try { dsNameFromRsMetadata(fn.toPath) }.isSuccess)
      .toArray ++ f.listFiles
      .filter(_.isDirectory)
      .flatMap(listMovieMetadataFiles)
  }

  def runImportRsMovies(path: Path,
                        name: String,
                        projectName: Option[String] = None): Int = {
    val projectId = getProjectIdByName(projectName)
    if (projectId < 0)
      return errorExit("Can't continue with an invalid project.")
    def doImportMany(p: Path): Int = {
      if (name != "")
        errorExit("--name option not allowed when path is a directory")
      else runImportRsMovie(p, name, projectId)
    }
    importXmlRecursive(path,
                       listMovieMetadataFiles,
                       (p) => runImportRsMovie(p, name, projectId),
                       (p) => doImportMany(p))
  }

  def addDataSetToProject(dsId: IdAble,
                          projectId: Int,
                          verbose: Boolean = false): Int = {
    val tx = for {
      project <- Try { Await.result(sal.getProject(projectId), TIMEOUT) }
      ds <- getDataSet(dsId)
      request <- Try { project.asRequest.appendDataSet(ds.id) }
      projectWithDataSet <- Try {
        Await.result(sal.updateProject(projectId, request), TIMEOUT)
      }
    } yield projectWithDataSet

    tx match {
      case Success(p) =>
        if (verbose) printProjectInfo(p)
        else printMsg(s"Added dataset to project ${p.name}")
      case Failure(err) =>
        errorExit(s"Couldn't add dataset to project: ${err.getMessage}")
    }
  }

  def runDeleteDataSet(datasetId: IdAble): Int = {
    Try { Await.result(sal.deleteDataSet(datasetId), TIMEOUT) } match {
      case Success(response) => printMsg(s"${response.message}")
      case Failure(err) =>
        errorExit(s"Couldn't delete dataset: ${err.getMessage}")
    }
  }

  protected def getProjectIdByName(projectName: Option[String]): Int = {
    if (!projectName.isDefined) return 0
    Try { Await.result(sal.getProjects, TIMEOUT) } match {
      case Success(projects) =>
        projects.map(p => (p.name, p.id)).toMap.get(projectName.get) match {
          case Some(projectId) => projectId
          case None =>
            errorExit(s"Can't find project named '${projectName.get}'", -1)
        }
      case Failure(err) =>
        errorExit(s"Couldn't retrieve projects: ${err.getMessage}", -1)
    }
  }

  def runCreateProject(name: String, description: String): Int = {
    Try { Await.result(sal.createProject(name, description), TIMEOUT) } match {
      case Success(project) => printProjectInfo(project)
      case Failure(err) =>
        errorExit(s"Couldn't create project: ${err.getMessage}")
    }
  }

  /**
    * Emit a template/example JSON file to supply to run-pipeline
    *
    * This uses the dev_diagnostic pipeline. Changing the reference entry point id
    * to a positive integer should
    *
    * @return
    */
  def runEmitAnalysisTemplate: Int = {

    val analysisOpts = {

      // FIXME. WTF is up with this 0 usage?
      val ep = BoundServiceEntryPoint("eid_ref_dataset",
                                      DataSetMetaTypes.Reference.toString,
                                      IntIdAble(0))

      val taskOptions: Seq[ServiceTaskOptionBase] =
        Seq(
          ServiceTaskIntOption("pbsmrtpipe.task_options.test_int", 1),
          ServiceTaskBooleanOption("pbsmrtpipe.task_options.raise_exception",
                                   false),
          ServiceTaskStrOption("pbsmrtpipe.task_options.test_str",
                               "example-string")
        )

      PbSmrtPipeServiceOptions("My-job-name",
                               "pbsmrtpipe.pipelines.dev_diagnostic",
                               Seq(ep),
                               taskOptions,
                               Nil)
    }

    val jx = analysisOpts.toJson.asJsObject

    val msg =
      "datasetId should be an positive integer; to obtain the datasetId from a UUID, run 'pbservice get-dataset {UUID}'. The entryId(s) can be obtained by running 'pbsmrtpipe show-pipeline-templates {PIPELINE-ID}'"
    val comment = JsObject("_comment" -> JsString(msg))

    val result = JsObject(comment.fields ++ jx.fields)

    println(result.prettyPrint)
    0
  }

  def runShowPipelines: Int = {
    Await
      .result(sal.getPipelineTemplates, TIMEOUT)
      .sortWith(_.id > _.id)
      .foreach { pt =>
        println(s"${pt.id}: ${pt.name}")
      }
    0
  }

  def runAnalysisPipeline(jsonPath: Path, block: Boolean): Int = {
    val jsonSrc = Source.fromFile(jsonPath.toFile).getLines.mkString
    val jsonAst = jsonSrc.parseJson
    val analysisOptions = jsonAst.convertTo[PbSmrtPipeServiceOptions]
    runAnalysisPipelineImpl(analysisOptions, block)
  }

  protected def validateEntryPoints(
      entryPoints: Seq[BoundServiceEntryPoint]): Int = {
    for (entryPoint <- entryPoints) {
      getDataSet(entryPoint.datasetId) match {
        case Success(dsInfo) => {
          // TODO check metatype against input
          println(
            s"Found entry point ${entryPoint.entryId} (datasetId = ${entryPoint.datasetId})")
          printDataSetInfo(dsInfo)
        }
        case Failure(err) => {
          return errorExit(s"can't retrieve datasetId ${entryPoint.datasetId}")
        }
      }
    }
    0
  }

  protected def validatePipelineId(pipelineId: String): Int = {
    Try {
      Await.result(sal.getPipelineTemplate(pipelineId), TIMEOUT)
    } match {
      case Success(x) => logMsg(s"Found pipeline template $pipelineId")
      case Failure(err) =>
        errorExit(
          s"Can't find pipeline template ${pipelineId}: ${err.getMessage}\nUse 'pbsmrtpipe show-templates' to display a list of available pipelines")
    }
  }

  protected def validatePipelineOptions(
      analysisOptions: PbSmrtPipeServiceOptions): Int = {
    max(validatePipelineId(analysisOptions.pipelineId),
        validateEntryPoints(analysisOptions.entryPoints))
  }

  protected def runAnalysisPipelineImpl(
      analysisOptions: PbSmrtPipeServiceOptions,
      block: Boolean = true,
      validate: Boolean = true): Int = {
    //println(analysisOptions)
    var xc = 0
    if (validate) {
      xc = validatePipelineOptions(analysisOptions)
      if (xc != 0) return errorExit("Analysis options failed validation")
    }
    Try {
      Await.result(sal.runAnalysisPipeline(analysisOptions), TIMEOUT)
    } match {
      case Success(jobInfo) => {
        println(s"Job ${jobInfo.id} UUID ${jobInfo.uuid} started")
        printJobInfo(jobInfo)
        if (block) waitForJob(jobInfo.uuid) else 0
      }
      case Failure(err) => errorExit(err.getMessage)
    }
  }

  protected def importEntryPoint(eid: String,
                                 xmlPath: Path): BoundServiceEntryPoint = {
    var dsMeta = getDataSetMiniMeta(xmlPath)
    var xc = runImportDataSetSafe(xmlPath)
    if (xc != 0)
      throw new Exception(s"Could not import dataset ${eid}:${xmlPath}")
    // this is stupidly inefficient
    val dsId: IdAble = getDataSet(dsMeta.uuid) match {
      case Success(ds) => ds.id
      case Failure(err) => throw new Exception(err.getMessage)
    }
    BoundServiceEntryPoint(eid, dsMeta.metatype.toString, dsId)
  }

  protected def importEntryPointAutomatic(
      entryPoint: String): BoundServiceEntryPoint = {
    logger.info(s"Importing entry point '$entryPoint'")
    val epFields = entryPoint.split(':')
    if (epFields.length == 2) {
      importEntryPoint(epFields(0), Paths.get(epFields(1)))
    } else if (epFields.length == 1) {
      val xmlPath = Paths.get(epFields(0))
      val dsMeta = getDataSetMiniMeta(xmlPath)
      val eid = PbsmrtpipeConstants
        .metaTypeToEntryId(dsMeta.metatype.toString)
        .getOrElse(throw new Exception(
          s"Can't determine entryId for ${dsMeta.metatype.toString}"))
      importEntryPoint(eid, xmlPath)
    } else throw new Exception(s"Can't interpret argument ${entryPoint}")
  }

  protected def getPipelinePresets(
      presetXml: Option[Path]): PipelineTemplatePreset = {
    presetXml match {
      case Some(path) => PipelineTemplatePresetLoader.loadFrom(path)
      case _ => defaultPresets
    }
  }

  private def validateEntryPointIds(
      entryPoints: Seq[BoundServiceEntryPoint],
      pipeline: PipelineTemplate): Try[Seq[BoundServiceEntryPoint]] = Try {
    val eidsInput = entryPoints.map(_.entryId).sorted.mkString(", ")
    val eidsTemplate =
      pipeline.entryPoints.map(_.entryId).sorted.mkString(", ")
    if (eidsInput != eidsTemplate)
      throw new Exception(
        "Mismatch between supplied and expected entry points: the input " +
          s"datasets correspond to entry points ($eidsInput), while the pipeline " +
          s"${pipeline.id} requires entry points ($eidsTemplate)")
    entryPoints
  }

  // XXX there is a bit of a disconnect between how preset.xml is handled and
  // how options are actually passed to services, so we need to convert them
  // here
  protected def getPipelineServiceOptions(
      jobTitle: String,
      pipelineId: String,
      entryPoints: Seq[BoundServiceEntryPoint],
      presets: PipelineTemplatePreset,
      userTaskOptions: Option[Map[String, String]] = None)
    : Try[PbSmrtPipeServiceOptions] = {
    val workflowOptions = Seq[ServiceTaskOptionBase]()
    val userOptions: Seq[ServiceTaskOptionBase] = presets.taskOptions ++
      userTaskOptions
        .getOrElse(Map[String, String]())
        .map {
          case (k, v) => k -> ServiceTaskStrOption(k, v)
        }
        .values
    for {
      _ <- Try { logger.debug("Getting pipeline options from server") }
      pipeline <- Try {
        Await.result(sal.getPipelineTemplate(pipelineId), TIMEOUT)
      }
      eps <- validateEntryPointIds(entryPoints, pipeline)
      taskOptions <- Try {
        PipelineUtils.getPresetTaskOptions(pipeline, userOptions)
      }
    } yield
      PbSmrtPipeServiceOptions(jobTitle,
                               pipelineId,
                               entryPoints,
                               taskOptions,
                               workflowOptions)
  }

  def runPipeline(pipelineId: String,
                  entryPoints: Seq[String],
                  jobTitle: String,
                  presetXml: Option[Path] = None,
                  block: Boolean = true,
                  validate: Boolean = true,
                  taskOptions: Option[Map[String, String]] = None,
                  asJson: Boolean = false): Int = {
    if (entryPoints.isEmpty)
      return errorExit("At least one entry point is required")

    val pipelineIdFull =
      if (pipelineId.split('.').length != 3)
        s"pbsmrtpipe.pipelines.$pipelineId"
      else pipelineId

    logger.info(s"pipeline ID: $pipelineIdFull")
    if (validatePipelineId(pipelineIdFull) != 0) return errorExit("Aborting")
    var jobTitleTmp = jobTitle
    if (jobTitle.length == 0) jobTitleTmp = s"pbservice-$pipelineIdFull"
    val tx = for {
      eps <- Try { entryPoints.map(importEntryPointAutomatic) }
      presets <- Try { getPipelinePresets(presetXml) }
      opts <- getPipelineServiceOptions(jobTitleTmp,
                                        pipelineIdFull,
                                        eps,
                                        presets,
                                        taskOptions)
      job <- Try { Await.result(sal.runAnalysisPipeline(opts), TIMEOUT) }
    } yield job

    tx match {
      case Success(job) =>
        if (block) waitForJob(job.uuid) else printJobInfo(job, asJson)
      case Failure(err) =>
        errorExit(s"Failed to run pipeline: ${err}") //.getMessage}")
    }
  }

  def runTerminateAnalysisJob(jobId: IdAble): Int = {
    def toSummary(m: MessageResponse) = m.message
    println(s"Attempting to terminate Analysis Job ${jobId.toIdString}")
    val fx = for {
      job <- sal.getJob(jobId)
      messageResponse <- sal.terminatePbsmrtpipeJob(job.id)
    } yield messageResponse
    runAndBlock(fx, toSummary, TIMEOUT)
  }

  def runDeleteJob(jobId: IdAble, force: Boolean = true): Int = {
    def deleteJob(job: EngineJob, nChildren: Int): Future[EngineJob] = {
      if (!job.isComplete) {
        if (force) {
          println("WARNING: job did not complete - attempting to terminate")
          if (runTerminateAnalysisJob(jobId) != 0) {
            println(
              "Job termination failed; will delete anyway, but this may have unpredictable side effects")
          }
        } else {
          throw new Exception(
            s"Can't delete this job because it hasn't completed - try 'pbservice terminate-job ${jobId.toIdString} ...' first, or add the argument --force if you are absolutely certain the job is okay to delete")
        }
      } else if (nChildren > 0) {
        if (force) {
          println(
            "WARNING: job output was used by $nChildren active jobs - deleting it may have unintended side effects")
          Thread.sleep(5000)
        } else {
          throw new Exception(
            s"Can't delete job ${job.id} because ${nChildren} active jobs used its results as input; add --force if you are absolutely certain the job is okay to delete")
        }
      }
      sal.deleteJob(job.uuid, force = force)
    }
    println(s"Attempting to delete job ${jobId.toIdString}")
    val fx = for {
      job <- Try { Await.result(sal.getJob(jobId), TIMEOUT) }
      children <- Try { Await.result(sal.getJobChildren(job.uuid), TIMEOUT) }
      deleteJob <- Try { Await.result(deleteJob(job, children.size), TIMEOUT) }
      _ <- sal.pollForSuccessfulJob(deleteJob.uuid, Some(maxTime))
    } yield deleteJob

    fx match {
      case Success(j) => println(s"Job ${jobId.toIdString} deleted."); 0
      case Failure(ex) => errorExit(ex.getMessage, 1)
    }
  }

  /**
    * Run export of a single job, returning a Path to the zip file
    */
  protected def runExportJobImpl(jobId: IdAble,
                                 destPath: Path,
                                 includeEntryPoints: Boolean): Try[Path] = {
    for {
      job <- Try { Await.result(sal.getJob(jobId), TIMEOUT) }
      exportJob <- Try {
        Await.result(
          sal.exportJobs(Seq(job.id),
                         destPath,
                         includeEntryPoints,
                         Some(s"pbservice-export-job-$jobId"),
                         Some(s"Run from 'pbservice export-job $jobId'")),
          TIMEOUT)
      }
      _ <- sal.pollForSuccessfulJob(exportJob.uuid, Some(maxTime))
      ds <- Try {
        Await.result(sal.getJobDataStore(exportJob.id), TIMEOUT)
      }
    } yield
      ds.filter(_.fileTypeId == FileTypes.ZIP.fileTypeId)
        .headOption
        .map(f => Paths.get(f.path))
        .getOrElse {
          throw new Exception(s"Can't get ZIP file from export job datastore")
        }
  }

  def runExportJob(jobId: IdAble,
                   destPath: Path,
                   includeEntryPoints: Boolean): Int = {
    val actualDestPath = Option(destPath).getOrElse {
      val defaultPath = Paths.get("").toAbsolutePath
      val msg =
        s"No output path supplied, defaulting to current working directory ($defaultPath); this will fail if SMRT Link is unable to write here"
      logger.warn(msg)
      println(msg)
      defaultPath
    }
    runExportJobImpl(jobId, actualDestPath, includeEntryPoints) match {
      case Success(p) => println(s"Job exported to ${p.toString}"); 0
      case Failure(ex) => errorExit(ex.getMessage, 1)
    }
  }

  def runImportJob(path: Path): Try[String] = {
    for {
      job <- Try { Await.result(sal.importJob(path), TIMEOUT) }
      _ <- sal.pollForSuccessfulJob(job.id, Some(maxTime))
      children <- Try { Await.result(sal.getJobChildren(job.id), TIMEOUT) }
      imported <- Try {
        children.headOption.getOrElse {
          throw new RuntimeException("No job children found")
        }
      }
    } yield
      s"Job ${imported.uuid} ('${imported.name}') imported with ID ${imported.id}"
  }

  def manifestSummary(m: PacBioComponentManifest) =
    s"Component name:${m.name} id:${m.id} version:${m.version}"

  def manifestsSummary(manifests: Seq[PacBioComponentManifest]): String = {
    val headers = Seq("id", "version", "name")
    val table = manifests.map(m => Seq(m.id, m.version, m.name))
    printTable(table, headers)
    ""
  }

  def runGetPacBioManifests(): Int = {
    runAndBlock[Seq[PacBioComponentManifest]](sal.getPacBioComponentManifests,
                                              manifestsSummary,
                                              TIMEOUT)
  }

  // This is to make it backward compatiblity. Remove this when the other systems are updated
  private def getManifestById(
      manifestId: String): Future[PacBioComponentManifest] = {
    sal.getPacBioComponentManifests.flatMap { manifests =>
      manifests.find(x => x.id == manifestId) match {
        case Some(m) => Future { m }
        case _ =>
          Future.failed(
            new ResourceNotFoundError(s"Unable to find $manifestId"))
      }
    }
  }

  def runGetPacBioManifestById(ix: String): Int =
    runAndBlock[PacBioComponentManifest](getManifestById(ix),
                                         manifestSummary,
                                         TIMEOUT)

  def pacBioDataBundlesSummary(bundles: Seq[PacBioDataBundle]): String = {
    val headers: Seq[String] =
      Seq("Bundle Id", "Version", "Imported At", "Is Active")
    val table = bundles.map(b =>
      Seq(b.typeId, b.version, b.importedAt.toString(), b.isActive.toString))
    printTable(table, headers)
    // The printTable func should probalby return a string, not an Int
    ""
  }

  def runGetPacBioDataBundles(timeOut: FiniteDuration): Int =
    runAndBlock[Seq[PacBioDataBundle]](sal.getPacBioDataBundles(),
                                       pacBioDataBundlesSummary,
                                       timeOut)

  def runTsSystemStatus(user: String, comment: String): Int = {
    def toSummary(job: EngineJob) =
      s"Tech support bundle sent.\nUser = $user\nComments: $comment"
    println(s"Attempting to send tech support status bundle")
    val fx = for {
      job <- Try {
        Await.result(sal.runTsSystemStatus(user, comment), TIMEOUT)
      }
      _ <- sal.pollForSuccessfulJob(job.uuid, Some(maxTime))
    } yield job
    runAndSummary(fx, toSummary)
  }

  def runTsJobBundle(jobId: IdAble, user: String, comment: String): Int = {
    def toSummary(job: EngineJob) =
      s"Tech support job bundle sent.\nJob = ${job.id}; name = ${job.name}\nUser = $user\nComments: $comment"
    println(s"Attempting to send tech support failed job bundle")
    val fx = for {
      failedJob <- Try { Await.result(sal.getJob(jobId), TIMEOUT) }
      job <- Try {
        Await.result(sal.runTsJobBundle(failedJob.id, user, comment), TIMEOUT)
      }
      _ <- sal.pollForSuccessfulJob(job.uuid, Some(maxTime))
    } yield failedJob
    runAndSummary(fx, toSummary)
  }

  def alarmsSummary(alarms: Seq[AlarmStatus]): String = {
    val headers: Seq[String] =
      Seq("Id", "Severity", "Updated At", "Value", "Message")
    val table = alarms.map(
      a =>
        Seq(a.id,
            a.severity.toString,
            a.updatedAt.toString(),
            a.value.toString,
            a.message.getOrElse("")))
    printTable(table, headers)
    ""
  }

  def runGetAlarms(timeOut: FiniteDuration) =
    runAndBlock[Seq[AlarmStatus]](sal.getAlarms(), alarmsSummary, timeOut)

}

object PbService extends LazyLogging {

  // Introducing a new pattern to remove duplication. Each Subparser
  // should return a Future[String] where the string is the terse summary of the output
  // and use recoverWith or recover to handle any handle-able exceptions
  // and add local context to the error. The Future Should encapsulate all the necessary
  // steps.

  // These are the ONLY place that should have a blocking call
  // and explicit case match to Success/Failure handing for Try

  def executeBlockAndSummary(fx: Future[String],
                             timeout: FiniteDuration): Int = {
    executeAndSummary(Try(Await.result(fx, timeout)))
  }

  def executeAndSummary(tx: Try[String]): Int = {
    tx match {
      case Success(sx) =>
        println(sx)
        0
      case Failure(ex) =>
        logger.error(s"${ex.getMessage}")
        System.err.println(s"${ex.getMessage} $ex")
        1
    }
  }

  protected def getPass = "foo"

  def apply(c: PbServiceParser.CustomConfig): Int = {
    implicit val actorSystem = ActorSystem("pbservice")

    def toClient = new SmrtLinkServiceClient(c.host, c.port)(actorSystem)
    def toAuthClient(t: String) =
      new AuthenticatedServiceAccessLayer(c.host, c.port, t)(actorSystem)
    def toAuthClientLogin(u: String, p: String) =
      Try {
        AuthenticatedServiceAccessLayer(c.host, c.port, u, p)(actorSystem)
      } match {
        case Success(s) => s
        case Failure(err) =>
          System.err.println(s"${err.getMessage}")
          System.err.println("Will fall back on unauthenticated client")
          toClient
      }
    val sal = c.authToken match {
      case Some(t) => toAuthClient(t)
      case None =>
        c.password match {
          case Some(password) => toAuthClientLogin(c.user, password)
          case None =>
            if (c.usePassword) toAuthClientLogin(c.user, getPass) else toClient
        }
    }
    val ps = new PbService(sal, c.maxTime)

    try {
      c.mode match {
        case Modes.STATUS =>
          executeBlockAndSummary(ps.exeStatus(c.asJson), c.maxTime)
        case Modes.IMPORT_DS =>
          executeBlockAndSummary(ps.execImportDataSets(c.path,
                                                       c.nonLocal,
                                                       c.asJson,
                                                       c.blockImportDataSet),
                                 c.maxTime)
        case Modes.IMPORT_FASTA =>
          ps.runImportFasta(c.path, c.name, c.organism, c.ploidy, c.project)
        case Modes.IMPORT_BARCODES =>
          ps.runImportBarcodes(c.path, c.name, c.project)
        case Modes.IMPORT_MOVIE =>
          ps.runImportRsMovies(c.path, c.name, c.project)
        case Modes.ANALYSIS => ps.runAnalysisPipeline(c.path, c.block)
        case Modes.TEMPLATE => ps.runEmitAnalysisTemplate
        case Modes.PIPELINE =>
          ps.runPipeline(c.pipelineId,
                         c.entryPoints,
                         c.jobTitle,
                         c.presetXml,
                         c.block,
                         taskOptions = c.taskOptions,
                         asJson = c.asJson)
        case Modes.SHOW_PIPELINES => ps.runShowPipelines
        case Modes.JOB =>
          ps.runGetJobInfo(c.jobId, c.asJson, c.dumpJobSettings, c.showReports)
        case Modes.JOBS =>
          ps.runGetJobs(c.maxItems, c.asJson, c.jobType, c.jobState)
        case Modes.TERMINATE_JOB => ps.runTerminateAnalysisJob(c.jobId)
        case Modes.DELETE_JOB => ps.runDeleteJob(c.jobId, c.force)
        case Modes.EXPORT_JOB =>
          ps.runExportJob(c.jobId, c.path, c.includeEntryPoints)
        case Modes.IMPORT_JOB => executeAndSummary(ps.runImportJob(c.path))
        case Modes.DATASET => ps.runGetDataSetInfo(c.datasetId, c.asJson)
        case Modes.DATASETS =>
          executeBlockAndSummary(ps.runGetDataSets(c.datasetType,
                                                   c.maxItems,
                                                   c.asJson,
                                                   c.searchName,
                                                   c.searchPath),
                                 c.maxTime)
        case Modes.DELETE_DATASET => ps.runDeleteDataSet(c.datasetId)
        case Modes.MANIFEST => ps.runGetPacBioManifestById(c.manifestId)
        case Modes.MANIFESTS => ps.runGetPacBioManifests
        case Modes.BUNDLES => ps.runGetPacBioDataBundles(20.seconds)
        case Modes.TS_STATUS => ps.runTsSystemStatus(c.user, c.comment)
        case Modes.TS_JOB => ps.runTsJobBundle(c.jobId, c.user, c.comment)
        case Modes.ALARMS => ps.runGetAlarms(c.maxTime)
        case Modes.CREATE_PROJECT => ps.runCreateProject(c.name, c.description)
        case x => {
          System.err.println(s"Unsupported action '$x'")
          1
        }
      }
    } finally {
      actorSystem.terminate()
    }
  }
}

object PbServiceApp extends App with LazyLogging {
  def run(args: Seq[String]) = {
    val xc = PbServiceParser.parser
      .parse(args.toSeq, PbServiceParser.defaults) match {
      case Some(config) =>
        logger.debug(s"Args $config")
        PbService(config)
      case _ => 1
    }
    sys.exit(xc)
  }
  run(args)
}
