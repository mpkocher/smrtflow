package com.pacbio.secondary.smrtserverbundle.app

import java.net.{BindException, URL}
import java.nio.file.{Files, Path, Paths}

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.io.IO
import akka.util.Timeout
import akka.pattern._
import com.pacbio.common.models.Constants
import com.pacbio.secondary.smrtlink.services.utils.StatusGenerator
import com.pacbio.secondary.smrtlink.services._
import com.pacbio.secondary.smrtlink.time.SystemClock
import com.pacbio.common.logging.LoggerOptions
import com.pacbio.common.semver.SemVersion
import com.pacbio.secondary.smrtlink.analysis.tools.timeUtils
import com.pacbio.secondary.smrtlink.actors.{
  DaoFutureUtils,
  EventManagerActor,
  PacBioBundleDaoActor,
  PacBioDataBundlePollExternalActor,
  PacBioBundleDao => LegacyPacBioBundleDao
}
import com.pacbio.secondary.smrtlink.io.PacBioDataBundleIOUtils
import com.pacbio.secondary.smrtlink.app.{
  ActorSystemCakeProvider,
  BaseServiceConfigCakeProvider
}
import com.pacbio.secondary.smrtlink.jsonprotocols.SmrtLinkJsonProtocols
import com.pacbio.secondary.smrtlink.models.{
  PacBioComponentManifest,
  PacBioDataBundleIO
}
import com.pacbio.secondary.smrtserverbundle.dao.BundleUpdateDao
import com.typesafe.scalalogging.LazyLogging
import spray.json._
import spray.httpx.SprayJsonSupport._
import spray.can.Http
import spray.http.HttpHeaders
import spray.routing._
import spray.routing.directives.FileAndResourceDirectives

import concurrent.duration._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

trait SmrtBundleBaseMicroService extends PacBioService with LazyLogging {

  implicit val timeout = Timeout(30.seconds)

  // Note, Using a single prefix of "api/v1" will not work as "expected"
  override def prefixedRoutes = pathPrefix("api" / "v2") {
    super.prefixedRoutes
  }
}

/**
  * Original (5.0.X) Legacy Update Bundle Service.
  *
  * This mirrored the SMRT Link Server bundle API.
  *
  * Note, this should only be used with 5.0.X bundles.
  *
  */
class LegacyChemistryUpdateBundleService(daoActor: ActorRef,
                                         rootBundle: Path,
                                         externalPollActor: ActorRef,
                                         eventManagerActor: ActorRef)(
    implicit override val actorSystem: ActorSystem)
    extends PacBioBundleService(daoActor,
                                rootBundle,
                                externalPollActor,
                                eventManagerActor)(actorSystem) {

  /**
    * Removed any PUT, POST routes yield only GET
    *
    * And remove the Remote bundle Status routes (this system should never be configured with an external
    * bundle service to update from.
    */
  override val routes = bundleRoutes
}

class BundleUpdateService(dao: BundleUpdateDao)(
    implicit actorSystem: ActorSystem)
    extends SmrtBundleBaseMicroService
    with DaoFutureUtils
    with PacBioDataBundleIOUtils
    with FileAndResourceDirectives {

  // Json Serialization
  import SmrtLinkJsonProtocols._
  // for getFromFile to work
  implicit val routing = RoutingSettings.default

  val ROUTE_PREFIX = "bundles"

  val manifest = PacBioComponentManifest(
    toServiceId("pacbio_bundles"),
    "PacBio Bundle Service",
    "2.0.0",
    "PacBio Update Service for PacBio Data Bundles"
  )

  def routeGetAllBundles: Route = pathPrefix(ROUTE_PREFIX) {
    pathEndOrSingleSlash {
      get {
        complete {
          dao.allBundles().map(bs => bs.map(_.bundle))
        }
      }
    }
  }

  def routeGetBySystemVersion =
    pathPrefix(ROUTE_PREFIX / Segment) { systemVersion =>
      pathEndOrSingleSlash {
        get {
          complete {
            dao
              .getBundlesBySystem(systemVersion)
              .map(bs => bs.map(_.bundle))
          }
        }
      }
    }

  def routeGetBySystemVersionAndBundleType: Route =
    pathPrefix(ROUTE_PREFIX / Segment / Segment) {
      (systemVersion, bundleType) =>
        pathEndOrSingleSlash {
          get {
            complete {
              dao
                .getBundlesBySystemAndBundleType(systemVersion, bundleType)
                .map(bs => bs.map(_.bundle))
            }
          }
        }
    }

  def routeGetBySystemVersionAndBundleTypeAndVersion: Route =
    pathPrefix(ROUTE_PREFIX / Segment / Segment / Segment) {
      (systemVersion, bundleType, bundleVersion) =>
        pathEndOrSingleSlash {
          get {
            complete {
              dao
                .getBundleByVersion(systemVersion, bundleType, bundleVersion)
                .map(_.bundle)
            }
          }
        }
    }

  def routeDownloadBundle: Route =
    pathPrefix(ROUTE_PREFIX / Segment / Segment / Segment / "download") {
      (systemVersion, bundleTypeId, bundleVersion) =>
        pathEndOrSingleSlash {
          get {
            onSuccess(
              dao.getBundleByVersion(systemVersion,
                                     bundleTypeId,
                                     bundleVersion)) {
              case b: PacBioDataBundleIO =>
                val fileName = s"$bundleTypeId-$bundleVersion.tar.gz"
                logger.info(s"Downloading bundle $b to $fileName")
                respondWithHeader(
                  HttpHeaders.`Content-Disposition`(
                    "attachment; filename=" + fileName)) {
                  getFromFile(b.tarGzPath.toFile)
                }
            }
          }
        }
    }

  def swaggerRoute: Route =
    pathPrefix("swagger") {
      pathEndOrSingleSlash {
        get {
          getFromResource("bundleserver_swagger.json")
        }
      }
    }

  override def routes: Route =
    routeGetAllBundles ~ routeGetBySystemVersion ~ routeGetBySystemVersionAndBundleType ~ routeGetBySystemVersionAndBundleTypeAndVersion ~ routeDownloadBundle ~ swaggerRoute
}

/**
  * Thin PacBio Data Bundle Only Server
  *
  */
trait PacBioDataBundleConfigCakeProvider
    extends BaseServiceConfigCakeProvider
    with LazyLogging {
  override lazy val systemName = "bundle-server"

  lazy val pacBioBundleRoot =
    Paths.get(conf.getString("smrtflow.server.bundleDir")).toAbsolutePath()

  lazy val dnsName = Try { conf.getString("smrtflow.server.dnsName") }.toOption

  /**
    * This will load the key and convert to URL.
    * Any errors will *only* be logged. This is probably not the best model.
    *
    * @return
    */
  private def loadUrl(key: String): Option[URL] = {
    Try { new URL(conf.getString(key)) } match {
      case Success(url) =>
        logger.info(s"Converted $key to URL $url")
        Some(url)
      case Failure(ex) =>
        logger.error(s"Failed to load URL from key '$key' ${ex.getMessage}")
        None
    }
  }
}

trait PacBioDataBundleServicesCakeProvider {
  this: ActorSystemCakeProvider with PacBioDataBundleConfigCakeProvider =>

  lazy val statusGenerator = new StatusGenerator(new SystemClock(),
                                                 systemName,
                                                 systemUUID,
                                                 Constants.SMRTFLOW_VERSION)

  def isValidSemVer(sx: String): Boolean =
    Try(SemVersion.fromString(sx)).isSuccess

  /**
    * Load all bundles with a system version in the relative dir
    *
    *
    * @param rootDir Root level bundle directory
    * @return
    */
  def loadSystemBundlesByVersion(
      rootDir: Path): Map[String, Seq[PacBioDataBundleIO]] = {

    if (Files.exists(rootDir)) {
      rootDir.toFile
        .listFiles()
        .filter(_.isDirectory)
        .filter(x => isValidSemVer(x.getName))
        .map { p =>
          val bundles = PacBioDataBundleIOUtils.loadBundlesFromRoot(p.toPath)
          (SemVersion.fromString(p.getName).toSemVerString(), bundles)
        }
        .toMap
    } else {
      logger.error(
        s"Directory $rootDir does not exists. Unable to load bundles.")
      Map.empty[String, Seq[PacBioDataBundleIO]]
    }
  }

  // Load Legacy 5.0.X Bundles and warn if a non 5.0.X version is loaded
  lazy val loaded500Bundles =
    PacBioDataBundleIOUtils.loadBundlesFromRoot(
      pacBioBundleRoot.resolve("5.0.0"))

  // V2 Bundles with the system version
  lazy val bundleDao = new BundleUpdateDao(
    loadSystemBundlesByVersion(pacBioBundleRoot))

  lazy val dao = new LegacyPacBioBundleDao(loaded500Bundles)
  lazy val daoActor =
    actorSystem.actorOf(Props(new PacBioBundleDaoActor(dao, pacBioBundleRoot)))

  // This is not the greatest model. If the URL is None, then none of the calls will be made
  lazy val externalUpdateUrl: Option[URL] = None

  lazy val externalPollActor = actorSystem.actorOf(
    Props(
      new PacBioDataBundlePollExternalActor(pacBioBundleRoot,
                                            externalUpdateUrl,
                                            12.hours,
                                            daoActor)))
  // V2 Bundle API service.

  // Events (this was to have the interface comply with the SMRT Link Server interface
  val externalEveUrl: Option[URL] = None
  lazy val eventManagerActor = actorSystem.actorOf(
    Props(
      new EventManagerActor(systemUUID, dnsName, externalEveUrl, apiSecret)),
    "EventManagerActor")

  lazy val services: Seq[PacBioService] = Seq(
    new LegacyChemistryUpdateBundleService(daoActor,
                                           pacBioBundleRoot,
                                           externalPollActor,
                                           eventManagerActor),
    new BundleUpdateService(bundleDao)(actorSystem),
    new StatusService(statusGenerator)
  )
}

trait RootPacBioDataBundleServerCakeProvider extends RouteConcatenation {
  this: ActorSystemCakeProvider with PacBioDataBundleServicesCakeProvider =>

  lazy val allRoutes: Route = services.map(_.prefixedRoutes).reduce(_ ~ _)

  lazy val rootService =
    actorSystem.actorOf(Props(new RoutedHttpService(allRoutes)))
}

trait PacBioDataBundleServerCakeProvider extends LazyLogging with timeUtils {
  this: RootPacBioDataBundleServerCakeProvider
    with PacBioDataBundleConfigCakeProvider
    with ActorSystemCakeProvider =>

  implicit val timeout = Timeout(10.seconds)

  //FIXME(mpkocher)(2017-4-11) Add validation on startup
  def startServices(): Future[String] = {
    (IO(Http)(actorSystem) ? Http.Bind(rootService,
                                       systemHost,
                                       port = systemPort)) flatMap {
      case r: Http.CommandFailed =>
        Future.failed(
          new BindException(s"Failed to bind to $systemHost:$systemPort"))
      case _ =>
        Future { s"Successfully started up on $systemHost:$systemPort" }
    }
  }
}

object PacBioDataBundleServer
    extends PacBioDataBundleConfigCakeProvider
    with ActorSystemCakeProvider
    with PacBioDataBundleServicesCakeProvider
    with RootPacBioDataBundleServerCakeProvider
    with PacBioDataBundleServerCakeProvider {}

object BundleUpdateServerApp extends App {
  import PacBioDataBundleServer._
  LoggerOptions.parseAddDebug(args)
  startServices()
}