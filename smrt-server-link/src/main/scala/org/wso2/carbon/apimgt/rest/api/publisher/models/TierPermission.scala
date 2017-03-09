/**
 * NOTE: This class is auto generated by the akka-scala (beta) swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen
 * For any issue or feedback, please open a ticket via https://github.com/swagger-api/swagger-codegen/issues/new
 */

package org.wso2.carbon.apimgt.rest.api.publisher.models

import org.joda.time.DateTime


case class TierPermission (
  permissionType: TierPermissionEnums.PermissionType,
  roles: Seq[String])

object TierPermissionEnums {

  type PermissionType = PermissionType.Value
  
  object PermissionType extends Enumeration {
    val Allow = Value("allow")
    val Deny = Value("deny")
  }

  
}
