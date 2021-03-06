package com.pacbio.secondary.smrtlink.analysis

import java.io.{File, FileWriter}
import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import com.pacbio.secondary.smrtlink.analysis.tools.timeUtils
import org.apache.commons.io.FileUtils

import scala.sys.process._
import scala.util.{Failure, Success, Try}
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.{DateTime => JodaDateTime}

import scala.collection.JavaConversions._

/**
  * Utils to Shell out to external Process
  * Created by mkocher on 9/26/15.
  *
  * This needs to be rethought and cleanedup. There's too many degenerative models here.
  *
  */
package object externaltools {

  sealed trait ExternalCmdResult {
    val cmd: Seq[String]
    val runTime: Long
  }

  case class ExternalCmdSuccess(cmd: Seq[String], runTime: Long)
      extends ExternalCmdResult

  case class ExternalCmdFailure(cmd: Seq[String], runTime: Long, msg: String)
      extends Exception(msg)
      with ExternalCmdResult

  trait ExternalToolsUtils extends LazyLogging with timeUtils {

    val SUFFIX_STDOUT = "stdout"
    val SUFFIX_STDERR = "stderr"

    private def toTmp(suffix: String, uuid: Option[UUID]): Path = {
      val ix = uuid.getOrElse(UUID.randomUUID())
      Files.createTempFile(s"cmd-$ix", suffix).toAbsolutePath
    }

    /**
      * This is a similar model to python's checkcall in Popen.
      *
      * This will ignore writing to stderr, stdout, or
      * logging.
      *
      * @param cmd Command to run
      * @return
      */
    def runCheckCall(cmd: Seq[String]): Option[ExternalCmdFailure] = {
      val startedAt = JodaDateTime.now()
      Process(cmd).! match {
        case 0 => None
        case x =>
          val msg = s"Failed to run cmd with exit code $x"
          Some(
            ExternalCmdFailure(cmd, computeTimeDeltaFromNow(startedAt), msg))
      }
    }

    /**
      * Run External Command and cleanup (deleted) stderr and stdout
      *
      * This should be used very sparingly as the stdout and stderr are deleted.
      *
      */
    def runCmd(
        cmd: Seq[String]): Either[ExternalCmdFailure, ExternalCmdSuccess] = {
      val cmdId = UUID.randomUUID()
      val stdout = toTmp(SUFFIX_STDOUT, Some(cmdId))
      val stderr = toTmp(SUFFIX_STDERR, Some(cmdId))
      val tmpFiles = Seq(stdout, stderr)

      val results = runCmd(cmd,
                           toTmp(SUFFIX_STDOUT, Some(cmdId)),
                           toTmp(SUFFIX_STDERR, Some(cmdId)))
      tmpFiles.map(_.toFile).foreach(FileUtils.deleteQuietly)
      results
    }

    /**
      * Core util to run external command
      *
      * @param cmd      Command as a seq of Strings
      * @param stdout   Path to stdout
      * @param stderr   Path to Stderr
      * @param extraEnv Env to be added to the process env
      * @return
      */
    def runUnixCmd(cmd: Seq[String],
                   stdout: Path,
                   stderr: Path,
                   extraEnv: Option[Map[String, String]] = None,
                   cwd: Option[File] = None): (Int, String) = {

      val startedAt = JodaDateTime.now()
      val fout = new FileWriter(stdout.toAbsolutePath.toString, true)
      val ferr = new FileWriter(stderr.toAbsolutePath.toString, true)

      // Write the subprocess standard error to propagate error message up.
      val errStr = new StringBuilder

      val pxl = ProcessLogger(
        (o: String) => {
          fout.write(o + "\n")
        },
        (e: String) => {
          logger.error(e + "\n")
          ferr.write(e + "\n")
          errStr.append(e + "\n")
        }
      )

      logger.info(s"Starting cmd $cmd")

      val px = extraEnv
        .map(x => Process(cmd, cwd = None, extraEnv = x.toSeq: _*))
        .getOrElse(Process(cmd, cwd = None))

      val rcode = px.!(pxl)

      val completedAt = JodaDateTime.now()
      val runTime = computeTimeDelta(completedAt, startedAt)
      logger.info(
        s"completed running with exit-code $rcode in $runTime sec. Command -> $cmd")

      if (rcode != 0) {
        val emsg = s"Cmd $cmd failed with exit code $rcode"
        logger.error(
          s"completed running with exit-code $rcode in $runTime sec. Command -> $cmd")
        ferr.write(emsg)
        errStr.append(emsg + "\n")
      }

      fout.close()
      ferr.close()

      (rcode, errStr.toString())

    }

    def runCmd(cmd: Seq[String],
               stdout: Path,
               stderr: Path,
               cwd: Option[File] = None)
      : Either[ExternalCmdFailure, ExternalCmdSuccess] = {
      val startedAt = JodaDateTime.now()
      val (exitCode, errorMessage) = runUnixCmd(cmd, stdout, stderr, cwd = cwd)
      val runTime = computeTimeDelta(JodaDateTime.now(), startedAt)
      exitCode match {
        case 0 => Right(ExternalCmdSuccess(cmd, runTime))
        case _ => Left(ExternalCmdFailure(cmd, runTime, errorMessage))
      }
    }

    /**
      * Resolve commandline exe path to absolute path.
      *
      * Filters out "." from path.
      *
      * @param cmd base name of exe (Example "samtools")
      * @return
      */
    def which(cmd: String): Option[Path] = {
      // The runCmd needs to resolve the Exe to provide a good error
      // If the external tool is not found in the path
      System
        .getenv("PATH")
        .split(":")
        .filter(_ != ".")
        .map(a => Paths.get(a).toAbsolutePath.resolve(cmd))
        .find(x => Files.exists(x))
    }

    def isExeAvailable(args: Seq[String]): Boolean =
      Try { runCheckCall(args).isEmpty }.getOrElse(false)
  }

  object ExternalToolsUtils extends ExternalToolsUtils

}
