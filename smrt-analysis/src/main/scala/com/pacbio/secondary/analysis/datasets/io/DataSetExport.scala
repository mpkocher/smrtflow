
package com.pacbio.secondary.analysis.datasets.io

import java.nio.file.{Files, Path, Paths}
import java.io._
import java.net.URI
import java.util.UUID
import java.util.zip._

import scala.collection.mutable

import com.typesafe.scalalogging.LazyLogging
import collection.JavaConversions._
import collection.JavaConverters._

import com.pacbio.secondary.analysis.datasets._
import com.pacificbiosciences.pacbiobasedatamodel.InputOutputDataType


class ExportDataSets(
      datasets: Seq[Path],
      dsType: DataSetMetaTypes.DataSetMetaType,
      zipPath: Path)
    extends LazyLogging {

  val dest = new FileOutputStream(zipPath.toFile)
  val out = new ZipOutputStream(new BufferedOutputStream(dest))
  private val BUFFER_SIZE = 2048
  private val haveFiles = mutable.Set.empty[String]

  private def writeFile(path: Path, zipOutPath: String): Int = {
    val ze = new ZipEntry(zipOutPath)
    out.putNextEntry(ze)
    val input = new FileInputStream(path.toString)
    val data = new Array[Byte](BUFFER_SIZE)
    var nRead = -1
    var nWritten = 0
    while ({nRead = input.read(data); nRead > 0}) {
      out.write(data, 0, nRead)
      nWritten += nRead
    }
    nWritten
  }

  private def writeResourceFile(dsId: UUID,
                                res: InputOutputDataType,
                                basePath: Path,
                                ignoreMissing: Boolean = false): Int = {
    val rid = res.getResourceId
    val uri = URI.create(rid.replaceAll(" ", "%20"))
    val rawPath = if (uri.getScheme == null) Paths.get(rid) else Paths.get(uri)
    val resourcePath = if (rawPath.isAbsolute) {
      rawPath.toAbsolutePath
    } else {
      basePath.resolve(rawPath).normalize().toAbsolutePath
    }
    if (! resourcePath.toFile.exists) {
      val msg = s"resource ${resourcePath.toString} is missing"
      if (ignoreMissing) {
        logger.error(msg); 0
      } else {
        throw new Exception(msg)
      }
    } else {
      val finalPath = if (rawPath.isAbsolute) {
        if (rawPath.startsWith(basePath)) {
          // if the resourceId is an absolute path, but a subdirectory of the
          // base path, we convert it to the corresponding relative path first
          basePath.relativize(rawPath)
        } else {
          Paths.get(s".${rawPath}")
        }
      } else {
        rawPath
      }
      val destPath = Paths.get(dsId.toString).resolve(finalPath.toString).toString
      if (haveFiles contains destPath) {
        logger.info(s"skipping duplicate file $destPath"); 0
      } else {
        logger.info(s"writing $resourcePath")
        haveFiles += destPath
        res.setResourceId(finalPath.toString)
        writeFile(resourcePath, destPath)
      }
    }
  }

  def write(skipMissingFiles: Boolean = false): Int = {
    val n = datasets.map { dsPath =>
      val basePath = dsPath.getParent
      val ds = ImplicitDataSetLoader.loaderAndResolveType(dsType, dsPath)
      val dsId = UUID.fromString(ds.getUniqueId)
      val dsOutPath = s"${dsId}/${dsPath.getFileName.toString}"
      val dsTmp = Files.createTempFile(s"relativized-${dsId}", ".xml")
      val nbytes = Option(ds.getExternalResources).map { extRes =>
        extRes.getExternalResource.map { er =>
          writeResourceFile(dsId, er, basePath, skipMissingFiles) +
          Option(er.getExternalResources).map { extRes2 =>
            extRes2.getExternalResource.map { rr =>
              writeResourceFile(dsId, rr, basePath, skipMissingFiles) +
              Option(rr.getFileIndices).map { fi =>
                fi.getFileIndex.map {
                  writeResourceFile(dsId, _, basePath, skipMissingFiles)
                }.sum
              }.getOrElse(0)
            }.sum
          }.getOrElse(0) + Option(er.getFileIndices).map { fi =>
            fi.getFileIndex.map {
              writeResourceFile(dsId, _, basePath, skipMissingFiles)
            }.sum
          }.getOrElse(0)
        }.sum
      }.getOrElse(0)
      DataSetWriter.writeDataSet(dsType, ds, dsTmp)
      writeFile(dsTmp, dsOutPath) + nbytes
    }.sum
    out.close
    logger.info(s"wrote $n bytes")
    n
  }
}

object ExportDataSets {
  def apply(datasets: Seq[Path],
            dsType: DataSetMetaTypes.DataSetMetaType,
            zipPath: Path,
            skipMissingFiles: Boolean = false): Int = {
    val writer = new ExportDataSets(datasets, dsType, zipPath)
    writer.write(skipMissingFiles)
  }
}
