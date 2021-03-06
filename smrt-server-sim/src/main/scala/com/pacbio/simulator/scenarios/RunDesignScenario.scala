package com.pacbio.simulator.scenarios

import java.net.URL
import java.nio.file.{Path, Paths}
import java.util.UUID

import akka.actor.ActorSystem
import com.typesafe.config.Config

import com.pacbio.secondary.smrtlink.client.SmrtLinkServiceClient
import com.pacbio.secondary.smrtlink.models.{RunSummary, Run}
import com.pacbio.simulator.steps._
import com.pacbio.simulator.{Scenario, ScenarioLoader}

object RunDesignScenarioLoader extends ScenarioLoader {
  override def load(config: Option[Config])(
      implicit system: ActorSystem): Scenario = {
    require(config.isDefined,
            "Path to config file must be specified for RunDesignScenario")
    val c: Config = config.get

    new RunDesignScenario(getHost(c),
                          getPort(c),
                          Paths.get(c.getString("run-xml-path")))
  }
}

class RunDesignScenario(host: String, port: Int, runXmlFile: Path)
    extends Scenario
    with VarSteps
    with ConditionalSteps
    with IOSteps
    with SmrtLinkSteps {

  override val name = "RunDesignScenario"

  override val smrtLinkClient = new SmrtLinkServiceClient(host, port)

  val runXmlPath: Var[String] = Var(runXmlFile.toString)
  val runXml: Var[String] = Var()
  val runId: Var[UUID] = Var()
  val runDesign: Var[Run] = Var()
  val runDesigns: Var[Seq[RunSummary]] = Var()

  override val steps = Seq(
    runDesigns := GetRuns,
    //fail("Run database should be initially empty") IF runDesigns ? (_.nonEmpty),
    runXml := ReadFileFromTemplate(runXmlPath),
    runId := CreateRun(runXml),
    runDesign := GetRun(runId),
    fail("Wrong uniqueId found") IF runDesign.mapWith(_.uniqueId) !=? runId,
    fail("Expected reserved to be false") IF runDesign
      .mapWith(_.reserved) !=? false,
    runDesigns := GetRuns,
    fail("Expected only a single run") IF runDesigns.mapWith(_.size) !=? 1,
    fail("Wrong uniqueId found") IF runDesigns
      .mapWith(_.head.uniqueId) !=? runId,
    fail("Expected reserved to be false") IF runDesigns.mapWith(
      _.head.reserved) !=? false,
    UpdateRun(runId, reserved = Some(Var(true))),
    runDesign := GetRun(runId),
    fail("Expected reserved to be true") IF runDesign
      .mapWith(_.reserved) !=? true,
    DeleteRun(runId),
    runDesigns := GetRuns,
    fail("Failed to delete run") IF runDesigns ? (_.nonEmpty)
  )
}
