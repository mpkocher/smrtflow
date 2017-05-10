package com.pacbio.secondary.analysis

import java.nio.file.{Files, Path, Paths}
import java.util.UUID
import java.io.File

import scala.xml.{Elem,XML}
import scala.util.{Try,Failure,Success}

import org.apache.commons.io.{FileUtils,FilenameUtils}

import com.pacbio.secondary.analysis.datasets.DataSetMetaTypes
import com.pacbio.secondary.analysis.datasets.io._
import com.pacbio.secondary.analysis.externaltools.PacBioTestData

/**
 *
 * Created by mkocher on 9/29/15.
 */
package object datasets {

  case class InValidDataSetError(msg: String) extends Exception(msg)

  trait DataSetFileUtils {
    private def parseXml(path: Path) = {
      Try { scala.xml.XML.loadFile(path.toFile) } match {
        case Success(x) => x
        case Failure(err) => throw new IllegalArgumentException(s"Couldn't parse ${path.toString} as an XML file: ${err.getMessage}")
      }
    }

    private def getAttribute(e: Elem, attr: String): String = {
      Try { e.attributes(attr).toString } match {
        case Success(a) => a
        case Failure(err) => throw new Exception(s"Can't retrieve $attr attribute from XML: ${err.getMessage}.  Please make sure this is a valid PacBio DataSet XML file.")
      }
    }

    // FIXME this should probably return a DataSetMetaType
    def dsMetaTypeFromPath(path: Path): String =
      getAttribute(parseXml(path), "MetaType")

    def dsUuidFromPath(path: Path): UUID =
      java.util.UUID.fromString(getAttribute(parseXml(path), "UniqueId"))

  /**
   * Parse an RSII metadata.xml file to extract the run name.
   *
   * @param path RSII movie.metadata.xml
   * @return
   */
    def dsNameFromRsMetadata(path: Path): String = {
      if (! path.toString.endsWith(".metadata.xml")) throw new Exception(s"File {p} lacks the expected extension (.metadata.xml)")
      val md = scala.xml.XML.loadFile(path.toFile)
      if (md.label != "Metadata") throw new Exception(s"The file ${path.toString} does not appear to be an RS II metadata XML")
      (md \ "Run" \ "Name").text
    }
  }

  /** Utilities for setting up test datasets that can be safely manipulated or
   *  deleted
   *
   */
  object MockDataSetUtils {

    // copy all files associated with a dataset to the destination directory
    // based on file-name prefix, e.g. movie name.  if copyAll is true, it
    // will copy everything in the source directory.
    private def copyResources(dsPath: Path, destDir: File, copyAll: Boolean = false) = {
      val dsDir = dsPath.getParent.toFile
      val prefix = FilenameUtils.getName(dsPath.toString).split('.')(0)
      for (f <- dsDir.listFiles) {
        val filename = FilenameUtils.getName(f.toString)
        if (copyAll || filename.startsWith(prefix)) {
          val dest = new File(destDir.toString + "/" + filename)
          FileUtils.copyFile(f, dest)
        }
      }
    }

    def makeBarcodedSubreads = {
      val pbdata = PacBioTestData()
      val targetDir = Files.createTempDirectory("dataset-contents")
      val subreadsDestDir = new File(targetDir.toString + "/SubreadSet")
      val barcodesDestDir = new File(targetDir.toString + "/BarcodeSet")
      val subreadsSrc = pbdata.getFile("barcoded-subreadset")
      val subreadsDir = subreadsSrc.getParent.toFile
      val barcodesSrc = pbdata.getFile("barcodeset")
      val barcodesDir = barcodesSrc.getParent.toFile
      // only copy the files we need for this SubreadSet, that way we can check
      // for an empty directory
      copyResources(subreadsSrc, subreadsDestDir)
      FileUtils.copyDirectory(barcodesDir, barcodesDestDir)
      val subreads = Paths.get(subreadsDestDir.toString + "/" +
                               FilenameUtils.getName(subreadsSrc.toString))
      var barcodes = Paths.get(barcodesDestDir.toString + "/" +
                               FilenameUtils.getName(barcodesSrc.toString))
      val dsSubreads = DataSetLoader.loadSubreadSet(subreads)
      val dsBarcodes = DataSetLoader.loadBarcodeSet(barcodes)
      // set new UUIDs
      dsSubreads.setUniqueId(UUID.randomUUID().toString)
      dsBarcodes.setUniqueId(UUID.randomUUID().toString)
      DataSetWriter.writeSubreadSet(dsSubreads, subreads)
      DataSetWriter.writeBarcodeSet(dsBarcodes, barcodes)
      (subreads, barcodes)
    }

    def makeTmpDataset(dsPath: Path,
                       metaType: DataSetMetaTypes.DataSetMetaType,
                       setNewUuid: Boolean = true,
                       copyFiles: Boolean = true): Path = {
      val targetDir = Files.createTempDirectory("dataset-contents")
      val dsTmp = Paths.get(targetDir.toString + "/" +
                            FilenameUtils.getName(dsPath.toString))
      val ds = if (!copyFiles) {
        ImplicitDataSetLoader.loaderAndResolveType(metaType, dsPath)
      } else {
        DataSetLoader.loadType(metaType, dsPath)
      }
      // NOTE this assumes that external resources are located in the same
      // directory
      if (copyFiles) copyResources(dsPath, targetDir.toFile, true)
      if (setNewUuid) ds.setUniqueId(UUID.randomUUID().toString)
      DataSetWriter.writeDataSet(metaType, ds, dsTmp)
      dsTmp
    }
  }
}
