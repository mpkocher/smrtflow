package com.pacbio.secondary.smrtlink.mail

import scalatags.Text.all._

/**
  * Created by mkocher on 7/21/17.
  */
object Templates {

  trait EmailTemplate[T] {
    def apply(input: T): EmailTemplateResult
  }


  object EmailJobSuccessTemplate extends EmailTemplate[SmrtLinkEmailInput] {
    def apply(input: SmrtLinkEmailInput) = {
      val html = s"""
         |Dear ${input.emailAddress},
         |
         |Your analysis job has successfully completed.
         |
         |Job ID: ${input.jobId}
         |Job name: ${input.jobName}
         |Start time: ${input.createdAt}
         |Finish time: ${input.completedAt}
         |
         |Please visit the following link to view the results: [${input.jobURL}]
         |
         |Powered by SMRT Link ${input.smrtLinkVersion.getOrElse("")}
         |Pacific Biosciences of California, Inc.
         |
      """.stripMargin

      EmailTemplateResult(s"SMRT Link Job ${input.jobId} Successfully Completed: ${input.jobName}", html)
    }
  }

  object EmailJobFailedTemplate extends EmailTemplate[SmrtLinkEmailInput] {
    def apply(input: SmrtLinkEmailInput) = {
      val html =
        s"""
         |Dear ${input.emailAddress},
         |
         |Your analysis job has Failed.
         |
         |Job ID: ${input.jobId}
         |Job name: ${input.jobName}
         |Start time: ${input.createdAt}
         |Finish time: ${input.completedAt}
         |
         |Please visit the following link to view the results: [${input.jobURL}]
         |
         |For troubleshooting assistance with this run:
         |
         |1. File a case by emailing support@pacb.com. You will receive an autogenerated PacBio customer portal case number.
         |2. Click Send Log Files at the SMRT Link page of the failed analysis to send the log files and case number to PacBio technical support.
         |
         |Powered by SMRT Link ${input.smrtLinkVersion.getOrElse("")}
         |Pacific Biosciences of California, Inc.
         |

    """.stripMargin
    EmailTemplateResult(s"SMRT Link Job ${input.jobId} Failed ${input.jobName}", html)
    }
  }

}
