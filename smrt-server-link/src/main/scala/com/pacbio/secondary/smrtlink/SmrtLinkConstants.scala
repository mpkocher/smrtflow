package com.pacbio.secondary.smrtlink

import com.pacbio.secondary.analysis.jobs.JobModels.JobConstants


trait SmrtLinkConstants {
  val BASE_PREFIX = "smrt-link"

  // Default project ID; all datasets that aren't
  // in more specific projects get this ID
  val GENERAL_PROJECT_ID = JobConstants.GENERAL_PROJECT_ID
}

trait JobServiceConstants {
  val ROOT_SERVICE_PREFIX = "secondary-analysis"
  val SERVICE_PREFIX = "job-manager"
  val JOB_ROOT_PREFIX = "jobs"
  // Per Job Option/Settings
  val JOB_OPTIONS = "options"
  // Job tasks
  val JOB_TASK_PREFIX = "tasks"
  // Blurring the lines between the job and engine
  val ENGINE_CONFIG_PREFIX = "config"
  // Enum of Job Types
  val JOB_TYPES_PREFIX = "job-types"
  // Job Events
  val JOB_EVENT_PREFIX = "events"
  // Datastore (and files in Datastore)
  val JOB_DATASTORE_PREFIX = "datastore"
  // Reports generated from a Job
  val JOB_REPORT_PREFIX = "reports"
  // DataSet entry-points (if any) for a job
  val ENTRY_POINTS_PREFIX = "entry-points"
  // All Datastore files for the system
  val DATASTORE_FILES_PREFIX = "datastore-files"

  // Not completely sure about wrapping the logger service.
  // The motivation is that the sourceId can be set/modified here so that the jobId isn't leaked to
  // the tool
  final val LOG_PREFIX = "log"
  // passed to the pbsmrtpipe process to communicate back to the services to log events/updates
  final val LOG_PB_SMRTPIPE_RESOURCE_ID = "pbsmrtpipe"
}
