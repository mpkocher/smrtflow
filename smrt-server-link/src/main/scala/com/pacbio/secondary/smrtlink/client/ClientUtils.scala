package com.pacbio.secondary.smrtlink.client

import java.io.File

import scala.math._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import scala.util.{Try, Failure, Success}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import com.pacbio.secondary.smrtlink.analysis.datasets.DataSetFileUtils
import com.pacbio.secondary.smrtlink.models._
import com.pacbio.secondary.smrtlink.analysis.jobs.JobModels._
import com.pacbio.secondary.smrtlink.analysis.reports.ReportModels._
import com.pacbio.common.models.Constants
import com.pacbio.common.semver.SemVersion
import com.pacbio.secondary.smrtlink.analysis.jobs.AnalysisJobStates
import com.pacbio.secondary.smrtlink.analysis.tools.timeUtils

trait ClientUtils extends timeUtils with DataSetFileUtils {

  import com.pacbio.secondary.smrtlink.jsonprotocols.SmrtLinkJsonProtocols._

  def listFilesByExtension(f: File, ext: String): Array[File] = {
    if (!f.isDirectory)
      throw new IllegalArgumentException(s"${f.toString} is not a directory")
    f.listFiles
      .filter((fn) => fn.toString.endsWith(ext))
      .toArray ++ f.listFiles
      .filter(_.isDirectory)
      .flatMap(d => listFilesByExtension(d, ext))
  }

  def toDataSetInfoSummary(ds: DataSetMetaDataSet): String = {
    val active = if (ds.isActive) "" else "(INACTIVE/SOFT-DELETED)"
    s"""
      |*DATASET SUMMARY* $active
      |          id: ${ds.id}
      |        uuid: ${ds.uuid}
      |        name: ${ds.name}
      |  numRecords: ${ds.numRecords}
      | totalLength: ${ds.totalLength}
      |       jobId: ${ds.jobId}
      |         md5: ${ds.md5}
      |   createdAt: ${ds.createdAt}
      |   updatedAt: ${ds.updatedAt}
      |        tags: ${ds.tags}
      |        path: ${ds.path}
    """.stripMargin
  }

  def printDataSetInfo(ds: DataSetMetaDataSet, asJson: Boolean = false): Int = {
    if (asJson) println(ds.toJson.prettyPrint)
    else println(toDataSetInfoSummary(ds))
    0
  }

  /**
    * Generate a Human readable summary of an Engine Job
    *
    * @param job Engine Job
    * @return
    */
  def toJobSummary(job: EngineJob): String = {
    val header = if (job.isActive) "" else "(INACTIVE/DELETED)"
    val runTimeSec = computeTimeDelta(job.updatedAt, job.createdAt)
    val body =
      s"""
        |*JOB SUMMARY* $header
        |            id: ${job.id}
        |          uuid: ${job.uuid}
        |          name: ${job.name}
        |         state: ${job.state}
        |    project id: ${job.projectId}
        |     jobTypeId: ${job.jobTypeId}
        |     is active: ${job.isActive}
        |     createdAt: ${job.createdAt}
        |     updatedAt: ${job.updatedAt}
        |      run time: $runTimeSec sec
        |    SL version: ${job.smrtlinkVersion.getOrElse("Unknown")}
        |    created by: ${job.createdBy.getOrElse("none")}
        |       comment: ${job.comment}
        |          path: ${job.path}
      """.stripMargin

    val errorMessage =
      if (AnalysisJobStates.FAILURE_STATES contains job.state) {
        job.errorMessage.getOrElse("Unknown")
      } else { "" }

    Seq(header, body, errorMessage).reduce(_ + "\n" + _)
  }

  def formatJobInfo(job: EngineJob,
                    asJson: Boolean = false,
                    dumpJobSettings: Boolean = false): String = {
    if (dumpJobSettings) {
      job.jsonSettings.parseJson.prettyPrint
    } else if (asJson) {
      job.toJson.prettyPrint
    } else {
      toJobSummary(job)
    }
  }

  def printJobInfo(job: EngineJob,
                   asJson: Boolean = false,
                   dumpJobSettings: Boolean = false): Int = {
    println(formatJobInfo(job, asJson, dumpJobSettings))
    0
  }

  def formatProjectInfo(project: FullProject): String = {
    Seq(
      "PROJECT SUMMARY:",
      s"  id: ${project.id}",
      s"  name: ${project.name}",
      s"  description: ${project.description}",
      s"  createdAt: ${project.createdAt}",
      s"  updatedAt: ${project.updatedAt}",
      s"  datasets: ${project.datasets.size}",
      s"  members: ${project.members.size}"
    ).mkString("\n")
  }

  def printProjectInfo(project: FullProject): Int = {
    println(formatProjectInfo(project))
    0
  }

  // Create a Table as String. This should be better model with a streaming
  // solution that passes in the "printer"
  def toTable(table: Seq[Seq[String]], headers: Seq[String]): String = {

    val columns = table.transpose
    val widths = (columns zip headers).map {
      case (col, header) =>
        max(header.length, col.map(_.length).max)
    }

    val mkline = (row: Seq[String]) =>
      (row zip widths).map { case (c, w) => c.padTo(w, ' ') }

    mkline(headers).mkString(" ") ++ "\n" ++
      table
        .map(row => mkline(row).mkString(" ") + "\n")
        .reduceLeftOption(_ + _)
        .getOrElse("NO DATA FOUND")
  }

  def printTable(table: Seq[Seq[String]], headers: Seq[String]): Int = {
    println(toTable(table, headers))
    0
  }

  def formatReportAttributes(r: Report, prefix: String = ""): String = {
    (Seq(s"${prefix}${r.title}:") ++ r.attributes.map { a =>
      s"  ${prefix}${a.name} = ${a.value}"
    }).mkString("\n")
  }

  def showReportAttributes(r: Report, prefix: String = ""): Int = {
    println(formatReportAttributes(r, prefix))
    0
  }

  /**
    * Check V1 gte V2 and return v1
    *
    * @param v1
    * @param v2
    * @return
    */
  private def versionGte(v1: SemVersion, v2: SemVersion): Future[SemVersion] = {
    if (v1.gte(v2)) Future.successful(v1)
    else
      Future.failed(throw new Exception(
        s"Incompatible versions ${v1.toSemVerString()} < ${v2.toSemVerString}"))
  }

  def isVersionGte(status: ServiceStatus, v: SemVersion): Future[SemVersion] = {
    for {
      remoteSystemVersion <- Future.successful(
        SemVersion.fromString(status.version))
      validatedRemoteSystemVersion <- versionGte(remoteSystemVersion, v)
    } yield validatedRemoteSystemVersion
  }

  def isVersionGteSystemVersion(status: ServiceStatus): Future[SemVersion] =
    isVersionGte(status, SemVersion.fromString(Constants.SMRTFLOW_VERSION))

}

// FIXME this is a pattern we should move away from in the core client, but
// it is difficult to avoid blocking calls entirely
trait ClientRuntimeUtils {
  private def printAndExit(msg: String, exitCode: Int): Int = {
    println(msg)
    exitCode
  }

  protected def errorExit(msg: String, exitCode: Int = 1) = {
    System.err.println(msg)
    exitCode
  }

  protected def printMsg(msg: String) = printAndExit(msg, 0)

  private def runAndSummary[T](fx: Try[T], summary: (T => String)): Int = {
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
}
