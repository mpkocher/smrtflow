package com.pacbio.simulator.scenarios

import java.io.File
import java.net.URL
import java.nio.file.{Paths, Path}
import java.util.UUID

import akka.actor.ActorSystem
import com.pacbio.secondary.smrtlink.client.SmrtLinkServiceAccessLayer
import com.pacbio.secondary.smrtlink.database.DatabaseConfig
import com.pacbio.secondary.smrtlink.database.legacy.{SqliteToPostgresConverter, SqliteToPostgresConverterOptions}
import com.pacbio.secondary.smrtlink.models._
import com.pacbio.secondary.smrtlink.testkit.TestUtils
import com.pacbio.simulator.StepResult.{FAILED, SUCCEEDED, Result}
import com.pacbio.simulator.steps.{BasicSteps, ConditionalSteps, VarSteps, SmrtLinkSteps}
import com.pacbio.simulator.{Scenario, ScenarioLoader}
import com.typesafe.config.{ConfigException, Config}

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.Try

object SqliteToPostgresScenarioLoader extends ScenarioLoader {
  override def load(config: Option[Config])(implicit system: ActorSystem): Scenario = {
    require(config.isDefined, "Path to config file must be specified for SqliteToPostgresScenario")
    val c: Config = config.get

    // Resolve overrides with String
    def getInt(key: String): Int =
      try {
        c.getInt(key)
      } catch {
        case e: ConfigException.WrongType => c.getString(key).trim.toInt
      }

    val opts = SqliteToPostgresConverterOptions(
      // TODO(smcclellan): Construct golden sqlite db file (See SL-985)
      new File(c.getString("sqlite-file")),
      c.getString("user"),
      c.getString("password"),
      c.getString("db-name"),
      c.getString("server"),
      getInt("port"))

    new SqliteToPostgresScenario(Paths.get(c.getString("smrt-link-exe-file")), opts)
  }
}

class SqliteToPostgresScenario(smrtLinkExe: Path, opts: SqliteToPostgresConverterOptions)
  extends Scenario with BasicSteps with VarSteps with ConditionalSteps with SmrtLinkSteps with TestUtils {

  override val name = "SqliteToPostgresScenario"

  override val smrtLinkClient = new SmrtLinkServiceAccessLayer(new URL("http", "localhost", 8070, ""), Some("jsnow"))

  // TODO(smcclellan): Move these steps into ...simulator.steps package?

  case class RunSqliteToPostgresConverterStep(opts: Var[SqliteToPostgresConverterOptions]) extends Step {
    override val name = "RunSqliteToPostgresConverter"
    override def run: Future[Result] = Future {
      SqliteToPostgresConverter.runImporter(opts.get)
      SUCCEEDED
    }
  }

  case object LaunchSmrtLinkStep extends VarStep[Process] {
    override val name = "LaunchSmrtLink"
    override def run: Future[Result] = Future {
      val argBase = "-Dsmrtflow.db.properties"
      val args = Seq(
        "JAVA_OPTS=",
        s"$argBase.databaseName=${opts.pgDbName}",
        s"$argBase.user=${opts.pgUsername}",
        s"$argBase.password=${opts.pgPassword}",
        s"$argBase.portNumber=${opts.pgPort}",
        s"$argBase.serverName=${opts.pgServer}"
      ).reduce(_ + " " + _)

      val process = Runtime.getRuntime.exec(Array("env", args, smrtLinkExe.toAbsolutePath.toString))
      output(process)

      Try(process.exitValue())
        .map(e => FAILED(s"SMRT Link exited with code $e"))
        .recover { case e: IllegalThreadStateException => SUCCEEDED }
        .get
    }
  }

  override def setUp() = {
    // TODO(smcclellan): Make maxConnections configurable?
    val dbConfig = DatabaseConfig(
      opts.pgDbName,
      opts.pgUsername,
      opts.pgPassword,
      opts.pgServer,
      opts.pgPort,
      maxConnections = 10)
    setupDb(dbConfig)
  }

  val converterOpts: Var[SqliteToPostgresConverterOptions] = Var(opts)
  val smrtLinkProcess: Var[Process] = Var()

  val project: Var[FullProject] = Var()

  val runSummaries: Var[Seq[RunSummary]] = Var()
  val runId: Var[UUID] = runSummaries.mapWith(_.head.uniqueId)
  val runDesign: Var[Run] = Var()

  override val steps = Seq(
    RunSqliteToPostgresConverterStep(converterOpts),

    smrtLinkProcess := LaunchSmrtLinkStep,
    SleepStep(30.seconds), // Wait for server to start

    // TODO(smcclellan): Add more steps to verify that SMRTLink endpoints contain data from SQLite
    project := GetProject(Var(2)),
    fail ("Wrong name for project ") IF project ? (_.name != "name"),
    fail ("Wrong project users") IF project ? (!_.members.exists(_.login == "jsnow")),

    runSummaries := GetRuns,
    fail ("Expected 1 run summary") IF runSummaries ? (_.size != 1),
    runDesign := GetRun(runId),
    fail ("Wrong name for run summary") IF runDesign ? (_.name != "name"),
    fail ("Wrong xml for run summary") IF runDesign ? (_.dataModel != "<xml></xml>")
  )

  override def tearDown() = {
    // Kill SMRT Link
    if (smrtLinkProcess.isDefined) {
      try {
        smrtLinkProcess.get.exitValue()
      } catch {
        case e: IllegalThreadStateException => smrtLinkProcess.get.destroy()
      }
    }
  }
}