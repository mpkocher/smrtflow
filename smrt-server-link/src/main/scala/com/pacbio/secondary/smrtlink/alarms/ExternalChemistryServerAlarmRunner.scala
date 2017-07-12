package com.pacbio.secondary.smrtlink.alarms

import java.net.URL

import akka.actor.ActorSystem
import com.pacbio.common.actors.ActorSystemProvider
import com.pacbio.common.alarms.{AlarmComposer, AlarmRunner}
import com.pacbio.common.dependency.Singleton
import com.pacbio.common.models.{Alarm, AlarmSeverity, AlarmUpdate}
import com.pacbio.secondary.smrtlink.app.SmrtLinkConfigProvider
import com.pacbio.secondary.smrtlink.client.PacBioDataBundleClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

/**
  * Created by mkocher on 7/11/17.
  */
class ExternalChemistryServerAlarmRunner(url: URL)(implicit actorSystem: ActorSystem) extends AlarmRunner {
  val client = new PacBioDataBundleClient(url)(actorSystem)

  def getStatus(maxRetries: Int): Future[AlarmUpdate] =
    client.getStatusWithRetry(maxRetries)
        .map(sx => AlarmUpdate(0.0, Some(s"Update server ${sx.message}"), AlarmSeverity.CLEAR))
        .recover { case NonFatal(ex) => AlarmUpdate(1.0, Some(s"Chemistry Update server at $url is unreachable ${ex.getMessage}"), AlarmSeverity.ERROR)}

  override val alarm = Alarm(
    "smrtlink.alarms.chemistry_update_server",
    "Chemistry Update Server Status",
    "Monitor External Chemistry Update Server Status")

  override protected def update(): Future[AlarmUpdate] = getStatus(3)
}

trait ExternalChemistryServerAlarmRunnerProvider {
  this: SmrtLinkConfigProvider with ActorSystemProvider with AlarmComposer =>

  val externalChemistryServerAlarmRunner: Singleton[ExternalChemistryServerAlarmRunner] =
    Singleton {() =>
      implicit val system = actorSystem()
      new ExternalChemistryServerAlarmRunner(externalBundleUrl().get)
    }

  addAlarm(externalChemistryServerAlarmRunner)
}
