/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.errors;

import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;

/**
 * The type Mib upload error builder.
 */
public class MibUploadErrorBuilder extends BaseErrorBuilder {
  private static final String UPLOAD_INVALID_DETAILED_MSG =
      "MIB uploaded for vendor software product with Id %s and version %s is invalid: %s";


  /**
   * Instantiates a new Mib upload error builder.
   *
   * @param vendorSoftwareProductId the vendor software product id
   * @param version                 the version
   * @param error                   the error
   */
  public MibUploadErrorBuilder(String vendorSoftwareProductId, org.openecomp.sdc.versioning.dao
      .types.Version version, String error) {
    getErrorCodeBuilder().withId(VendorSoftwareProductErrorCodes.MIB_UPLOAD_INVALID);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(String
        .format(UPLOAD_INVALID_DETAILED_MSG, vendorSoftwareProductId, version.toString(), error));
  }

  /**
   * Instantiates a new Mib upload error builder.
   *
   * @param errorMessage the error message
   */
  public MibUploadErrorBuilder(String errorMessage) {
    getErrorCodeBuilder().withId(VendorSoftwareProductErrorCodes.MIB_UPLOAD_INVALID);
    getErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION);
    getErrorCodeBuilder().withMessage(errorMessage);
  }
}
