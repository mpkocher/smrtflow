import java.nio.file.Paths
import java.util.UUID

import com.pacbio.secondary.smrtlink.analysis.constants.FileTypes
import com.pacbio.secondary.smrtlink.analysis.jobs.{
  AnalysisJobStates,
  JobModels,
  OptionTypes
}
import com.pacbio.secondary.smrtlink.analysis.tools.timeUtils
import com.pacbio.secondary.smrtlink.jobtypes.ImportFastaJobOptions
import org.joda.time.{DateTime => JodaDateTime}
import com.pacbio.secondary.smrtlink.jsonprotocols.SmrtLinkJsonProtocols
import org.apache.commons.io.FileUtils
import org.specs2.mutable.Specification
import spray.json._

import scala.io.Source

// XXX note that PipelineTemplate is tested in PipelineSpec.scala
class JobModelsSpec extends Specification with timeUtils {

  import JobModels._
  import OptionTypes._
  import SmrtLinkJsonProtocols._

  sequential

  val o1 = PipelineBooleanOption("id-a", "Boolean", true, "Boolean Option")
  val o2 = PipelineIntOption("id-b", "Int", 2001, "Integer Option")
  val o3 = PipelineDoubleOption("id-c", "Double", 3.14, "Double  Option")
  val o4 = PipelineStrOption("id-d", "String", "asdf", "String Option")
  val o5 = PipelineChoiceStrOption("id-e",
                                   "String Choice",
                                   "B",
                                   "String Choice Option",
                                   Seq("A", "B", "C"))
  val o6 = PipelineChoiceIntOption("id-f",
                                   "Int Choice",
                                   2,
                                   "Int Choice Option",
                                   Seq(1, 2, 3))
  val o7 = PipelineChoiceDoubleOption("id-g",
                                      "Double",
                                      0.1,
                                      "Double Choice Option",
                                      Seq(0.01, 0.1, 1.0))
  val pipelineTaskOpts = Seq(o1, o2, o3, o4, o5, o6, o7)

  "Test basic functionality and serialization of job models" should {
    "Service task options" in {
      var serviceOpts = Seq(
        ServiceTaskBooleanOption("id-a", true, BOOL.optionTypeId),
        ServiceTaskIntOption("id-b", 2001, INT.optionTypeId),
        ServiceTaskDoubleOption("id-c", 3.14, FLOAT.optionTypeId),
        ServiceTaskStrOption("id-d", "asdf", STR.optionTypeId),
        ServiceTaskStrOption("id-e", "B", CHOICE.optionTypeId),
        ServiceTaskIntOption("id-f", 2, CHOICE_INT.optionTypeId),
        ServiceTaskDoubleOption("id-g", 0.1, CHOICE_FLOAT.optionTypeId)
      )
      val j = serviceOpts.map(_.asInstanceOf[ServiceTaskOptionBase].toJson)
      val o = j.map(_.convertTo[ServiceTaskOptionBase])
      o.size must beEqualTo(7)
      val opt1 = o(0).asInstanceOf[ServiceTaskBooleanOption]
      opt1.optionTypeId must beEqualTo(BOOL.optionTypeId)
      opt1.value must beTrue
      val opt2 = o(1).asInstanceOf[ServiceTaskIntOption]
      opt2.optionTypeId must beEqualTo(INT.optionTypeId)
      opt2.value must beEqualTo(2001)
      val opt3 = o(2).asInstanceOf[ServiceTaskDoubleOption]
      opt3.optionTypeId must beEqualTo(FLOAT.optionTypeId)
      opt3.value must beEqualTo(3.14)
      val opt4 = o(3).asInstanceOf[ServiceTaskStrOption]
      opt4.optionTypeId must beEqualTo(STR.optionTypeId)
      opt4.value must beEqualTo("asdf")
      val opt5 = o(4).asInstanceOf[ServiceTaskStrOption]
      opt5.optionTypeId must beEqualTo(CHOICE.optionTypeId)
      opt5.value must beEqualTo("B")
      val opt6 = o(5).asInstanceOf[ServiceTaskIntOption]
      opt6.optionTypeId must beEqualTo(CHOICE_INT.optionTypeId)
      opt6.value must beEqualTo(2)
      val opt7 = o(6).asInstanceOf[ServiceTaskDoubleOption]
      opt7.optionTypeId must beEqualTo(CHOICE_FLOAT.optionTypeId)
      opt7.value must beEqualTo(0.1)
    }
    "Pipeline options" in {
      // boolean
      // we have to do a lot of type conversion for this to even compile
      var j = o1.asInstanceOf[PipelineBaseOption].toJson
      val oj1 =
        j.convertTo[PipelineBaseOption].asInstanceOf[PipelineBooleanOption]
      oj1.value must beTrue
      j = o1.asServiceOption.asInstanceOf[ServiceTaskOptionBase].toJson
      val oj1b = j
        .convertTo[ServiceTaskOptionBase]
        .asInstanceOf[ServiceTaskBooleanOption]
      oj1b.value must beTrue
      // integer
      j = o2.asInstanceOf[PipelineBaseOption].toJson
      val oj2 = j.convertTo[PipelineBaseOption].asInstanceOf[PipelineIntOption]
      oj2.value must beEqualTo(2001)
      // double
      j = o3.asInstanceOf[PipelineBaseOption].toJson
      val oj3 =
        j.convertTo[PipelineBaseOption].asInstanceOf[PipelineDoubleOption]
      oj3.value must beEqualTo(3.14)
      j = o3.asServiceOption.asInstanceOf[ServiceTaskOptionBase].toJson
      val oj3b = j
        .convertTo[ServiceTaskOptionBase]
        .asInstanceOf[ServiceTaskDoubleOption]
      oj3b.value must beEqualTo(3.14)
      // string
      j = o4.asInstanceOf[PipelineBaseOption].toJson
      val oj4 = j.convertTo[PipelineBaseOption].asInstanceOf[PipelineStrOption]
      oj4.value must beEqualTo("asdf")
      // string choice
      j = o5.asInstanceOf[PipelineBaseOption].toJson
      val oj5 =
        j.convertTo[PipelineBaseOption].asInstanceOf[PipelineChoiceStrOption]
      oj5.value must beEqualTo("B")
      oj5.choices must beEqualTo(Seq("A", "B", "C"))
      val oj5b = oj5.applyValue("C")
      oj5b.value must beEqualTo("C")
      oj5.applyValue("D") must throwA[UnsupportedOperationException]
      // failure mode
      PipelineChoiceStrOption(
        "id-1",
        "Name 1",
        "asdf",
        "Desc 1",
        Seq("A", "B", "C")) must throwA[UnsupportedOperationException]
      // int choice
      j = o6.asInstanceOf[PipelineBaseOption].toJson
      val oj6 =
        j.convertTo[PipelineBaseOption].asInstanceOf[PipelineChoiceIntOption]
      oj6.value must beEqualTo(2)
      oj6.choices must beEqualTo(Seq(1, 2, 3))
      val oj6b = oj6.applyValue(3)
      oj6b.value must beEqualTo(3)
      oj6.applyValue(0) must throwA[UnsupportedOperationException]
      PipelineChoiceIntOption("id-1", "Name 1", 4, "Desc 1", Seq(1, 2, 3)) must throwA[
        UnsupportedOperationException]
      // double choice
      j = o7.asInstanceOf[PipelineBaseOption].toJson
      val oj7 = j
        .convertTo[PipelineBaseOption]
        .asInstanceOf[PipelineChoiceDoubleOption]
      oj7.value must beEqualTo(0.1)
      oj7.choices must beEqualTo(Seq(0.01, 0.1, 1.0))
      val oj7b = oj7.applyValue(1.0)
      oj7b.value must beEqualTo(1.0)
      oj7.applyValue(0.9) must throwA[UnsupportedOperationException]
      PipelineChoiceDoubleOption(
        "id-1",
        "Name 1",
        10.0,
        "Desc 1",
        Seq(0.01, 0.1, 1.0)) must throwA[UnsupportedOperationException]
    }
    "PipelineTemplatePreset" in {
      val opts = Seq(ServiceTaskBooleanOption("id-a", true, BOOL.optionTypeId),
                     ServiceTaskIntOption("id-b", 2001, INT.optionTypeId))
      val taskOpts = Seq(
        ServiceTaskDoubleOption("id-c", 3.14, FLOAT.optionTypeId),
        ServiceTaskStrOption("id-d", "Hello, world", STR.optionTypeId),
        ServiceTaskStrOption("id-e", "A", CHOICE.optionTypeId)
      )
      val pp =
        PipelineTemplatePreset("preset-id-01",
                               "pipeline-id-01",
                               opts,
                               taskOpts)
      val j = pp.toJson
      val ppp = j.convertTo[PipelineTemplatePreset]
      //ppp must beEqualTo(pp)
      pp.presetId must beEqualTo(ppp.presetId)
      pp.pipelineId must beEqualTo(ppp.pipelineId)
      ppp.options.toList must beEqualTo(opts)
      ppp.taskOptions.toList must beEqualTo(taskOpts)
    }
    "PipelineDirectJobOptions" in {
      val opts = Seq(ServiceTaskBooleanOption("id-a", true, BOOL.optionTypeId),
                     ServiceTaskIntOption("id-b", 2001, INT.optionTypeId))
      val taskOpts = Seq(
        ServiceTaskDoubleOption("id-c", 3.14, FLOAT.optionTypeId),
        ServiceTaskStrOption("id-d", "Hello, world", STR.optionTypeId),
        ServiceTaskStrOption("id-e", "A", CHOICE.optionTypeId)
      )
      val entryPoints = Seq(
        BoundEntryPoint("eid_ref_dataset",
                        Paths.get("/var/tmp/referenceset.xml")),
        BoundEntryPoint("eid_subread", Paths.get("/var/tmp/subreadset.xml")))
      val jobOpts = PbsmrtpipeDirectJobOptions(1,
                                               "pipeline-id-01",
                                               entryPoints,
                                               taskOpts,
                                               opts)
      val jobOpts2 = jobOpts.toJson.convertTo[PbsmrtpipeDirectJobOptions]
      jobOpts2 must beEqualTo(jobOpts)
    }
    "DataStore models" in {
      val dsf = DataStoreFile(
        UUID.randomUUID(),
        "pbcommand.tasks.dev_mixed_app",
        FileTypes.JSON.fileTypeId,
        1000,
        JodaDateTime.now(),
        JodaDateTime.now(),
        "/var/tmp/report.json",
        false,
        "JSON file",
        "JSON file"
      )
      val dsf2 = dsf.toJson.convertTo[DataStoreFile]
      dsf.toString must beEqualTo(dsf2.toString)
      val dsf3 = dsf2.relativize(Paths.get("/var"))
      dsf3.path must beEqualTo("tmp/report.json")
      val dsf4 = dsf3.absolutize(Paths.get("/data/smrtlink"))
      dsf4.path must beEqualTo("/data/smrtlink/tmp/report.json")
      val dsjf = DataStoreJobFile(UUID.randomUUID(), dsf)
      val dsjf2 = dsjf.toJson.convertTo[DataStoreJobFile]
      dsjf.toString must beEqualTo(dsjf2.toString)
      val ds = PacBioDataStore(JodaDateTime.now(),
                               JodaDateTime.now(),
                               "0.1.0",
                               Seq(dsf))
      val ds2 = ds.toJson.convertTo[PacBioDataStore]
      ds2.toString must beEqualTo(ds.toString)
      val ds3 = ds.relativize(Paths.get("/var"))
      ds3.files(0).path must beEqualTo("tmp/report.json")
      val ds4 = ds3.absolutize(Paths.get("/data/smrtlink"))
      ds4.files(0).path must beEqualTo("/data/smrtlink/tmp/report.json")
    }
    "PipelineDataStoreRules" in {
      val rules = Seq(
        DataStoreFileViewRule("pbsmrtpipe.tasks.task1-out-0",
                              FileTypes.DS_REFERENCE.fileTypeId,
                              false,
                              None,
                              None),
        DataStoreFileViewRule("pbsmrtpipe.tasks.task1-out-1",
                              FileTypes.JSON.fileTypeId,
                              true,
                              Some("A file"),
                              Some("Hidden file"))
      )
      val prule =
        PipelineDataStoreViewRules("pbsmrtpipe.pipelines.id-1", rules, "4.0.0")
      prule.toJson.convertTo[PipelineDataStoreViewRules] must beEqualTo(prule)
    }
    "JobTask" in {
      val now = JodaDateTime.now()
      val t = JobTask(
        UUID.randomUUID(),
        1,
        "pbsmrtpipe.tasks.task1",
        "pbsmrtpipe.tasks.task1_tool_contract",
        "Task 1",
        AnalysisJobStates.SUCCESSFUL.toString,
        now,
        now,
        Some("Task 1 failed")
      )
      val tj = t.toJson.convertTo[JobTask]

      // Time parsing is not exactly lossy
      val tju = tj.copy(updatedAt = now, createdAt = now)
      tju === t

      val dt = computeTimeDelta(tj.updatedAt, t.updatedAt)
      dt must beLessThan(1)

      val ut = UpdateJobTask(1,
                             t.uuid,
                             AnalysisJobStates.SUCCESSFUL.toString,
                             "Task 1 succeeded",
                             None)
      val utj = ut.toJson.convertTo[UpdateJobTask]
      utj.toString must beEqualTo(ut.toString)
    }
  }

  private def getPath(name: String) = {
    Paths.get(getClass.getResource(s"misc-json/$name").toURI)
  }

  "Testing EngineJob serialization including previous versions" should {
    "Load model from SL 510 data model" in {
      val p = getPath("engine-job-5.1.0.26412.json")

      val sx: String = FileUtils.readFileToString(p.toFile, "UTF-8")
      val job = sx.parseJson.convertTo[EngineJob]

      job.uuid === UUID.fromString("4be8a6ca-a0ea-48a0-ade3-3b7e40653958")
      job.name === "Bauhaus2_Job_m54010_180210_225555"
      job.subJobTypeId must beNone
    }
    "Serialize model to JSON and recycle" in {

      val now = JodaDateTime.now()

      val job = EngineJob(
        1,
        UUID.randomUUID(),
        "My job",
        "Test job",
        now,
        now,
        now,
        AnalysisJobStates.CREATED,
        "pbsmrtpipe",
        "/tmp/0001",
        "{}",
        Some("smrtlinktest"),
        None,
        Some("4.0.0"),
        projectId = 10
      )
      val job2 = job.toJson.convertTo[EngineJob]
      job2.toString must beEqualTo(job.toString)
      job2.isRunning must beFalse
      job2.isSuccessful must beFalse
      job2.isComplete must beFalse
      job2.projectId must beEqualTo(10)
      val job3 = job2.copy(state = AnalysisJobStates.RUNNING)
      job3.isRunning must beTrue
      val job4 = job2.copy(state = AnalysisJobStates.SUCCESSFUL)
      job4.isSuccessful must beTrue
    }
  }

  private def getJson(name: String) =
    Source.fromFile(getPath(name).toFile).getLines.mkString.parseJson
}
