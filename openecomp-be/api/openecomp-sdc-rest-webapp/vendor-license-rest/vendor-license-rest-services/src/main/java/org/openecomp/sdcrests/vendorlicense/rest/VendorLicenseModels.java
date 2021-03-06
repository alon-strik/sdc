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

package org.openecomp.sdcrests.vendorlicense.rest;

import static org.openecomp.sdcrests.common.RestConstants.USER_MISSING_ERROR_MSG;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.common.RestConstants;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelActionRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelRequestDto;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1.0/vendor-license-models")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Vendor License Models")
@Validated
public interface VendorLicenseModels {

  @GET
  @Path("/")
  @ApiOperation(value = "List vendor license models",
      response = VendorLicenseModelEntityDto.class,
      responseContainer = "List")
  Response listLicenseModels(@ApiParam(
      value = "Currently supported value: 'Final' - only vendor License models with final versions "
          + "will be return - with their latest final version")
                             @QueryParam("versionFilter") String versionFilter,
                             @NotNull(message = USER_MISSING_ERROR_MSG)
                             @HeaderParam(RestConstants.USER_HEADER_PARAM) String user);

  @POST
  @Path("/")
  @ApiOperation(value = "Create vendor license model")
  Response createLicenseModel(@Valid VendorLicenseModelRequestDto request,
                              @NotNull(message = USER_MISSING_ERROR_MSG)
                              @HeaderParam(RestConstants.USER_HEADER_PARAM) String user);

  @PUT
  @Path("/{vlmId}")
  @ApiOperation(value = "Update vendor license model")
  Response updateLicenseModel(@Valid VendorLicenseModelRequestDto request,
                              @ApiParam(value = "Vendor license model Id") @PathParam("vlmId")
                                  String vlmId, @NotNull(message = USER_MISSING_ERROR_MSG)
                              @HeaderParam(RestConstants.USER_HEADER_PARAM) String user);

  @GET
  @Path("/{vlmId}")
  @ApiOperation(value = "Get vendor license model",
      response = VendorLicenseModelEntityDto.class)
  Response getLicenseModel(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @Pattern(regexp = Version.VERSION_REGEX, message = Version.VERSION_STRING_VIOLATION_MSG)
      @QueryParam("version") String version,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_HEADER_PARAM)
          String user);

  @DELETE
  @Path("/{vlmId}")
  @ApiOperation(value = "Delete vendor license model")
  Response deleteLicenseModel(
      @ApiParam(value = "Vendor license model Id") @PathParam("vlmId") String vlmId,
      @NotNull(message = USER_MISSING_ERROR_MSG) @HeaderParam(RestConstants.USER_HEADER_PARAM)
          String user);

  @PUT
  @Path("/{vlmId}/actions")
  @ApiOperation(value = "Update vendor license model")
  Response actOnLicenseModel(@Valid VendorLicenseModelActionRequestDto request,
                             @ApiParam(value = "Vendor license model Id") @PathParam("vlmId")
                                 String vlmId, @NotNull(message = USER_MISSING_ERROR_MSG)
                             @HeaderParam(RestConstants.USER_HEADER_PARAM) String user);
}
