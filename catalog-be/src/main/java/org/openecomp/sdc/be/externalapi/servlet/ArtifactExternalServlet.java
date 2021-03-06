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

package org.openecomp.sdc.be.externalapi.servlet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.servlets.BeGenericServlet;
import org.openecomp.sdc.be.servlets.RepresentationUtils;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.Loggable;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import fj.data.Either;

/**
 * This Servlet serves external users operations on artifacts.
 * 
 * @author mshitrit
 *
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Singleton
public class ArtifactExternalServlet extends BeGenericServlet {

	@Context
	private HttpServletRequest request;

	private static Logger log = LoggerFactory.getLogger(ArtifactExternalServlet.class.getName());

	/**
	 * Uploads an artifact to resource or service
	 * 
	 * @param assetType
	 * @param uuid
	 * @return
	 */
	@POST
	@Path("/{assetType}/{uuid}/artifacts")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "uploads of artifact to a resource or service", httpMethod = "POST", notes = "uploads of artifact to a resource or service", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact uploaded"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Asset not found") })
	public Response uploadArtifact(@PathParam("assetType") final String assetType, @PathParam("uuid") final String uuid, @ApiParam(value = "json describe the artifact", required = true) String data) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat = null;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		String requestURI = request.getRequestURI();
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		if (componentType == null) {
			log.debug("uploadArtifact: assetType parameter {} is not valid", assetType);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);

		if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
			log.debug("uploadArtifact: Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditExternalUploadArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
			log.debug("uploadArtifact: Missing USER_ID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.COMPONENT_MISSING_CONTACT);
			getComponentsUtils().auditExternalUploadArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty()) {
			try {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.uploadArtifactToComponentByUUID(data, request, componentType, uuid, additionalParams);
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to upload artifact");
					responseFormat = uploadArtifactEither.right().value();
					getComponentsUtils().auditExternalUploadArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5ByString((String) representation));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					getComponentsUtils().auditExternalUploadArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
				}
			} catch (Exception e) {
				final String message = "failed to upload artifact to a resource or service";
				BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
				log.debug(message, e);
				responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
			}
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * Uploads an artifact to resource instance
	 * 
	 * @param assetType
	 * @param uuid
	 * @param resourceInstanceName
	 * @return
	 */
	@POST
	@Path("/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "uploads an artifact to a resource instance", httpMethod = "POST", notes = "uploads an artifact to a resource instance", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact uploaded"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Asset not found") })
	public Response uploadArtifactToInstance(@PathParam("assetType") final String assetType, @PathParam("uuid") final String uuid, @PathParam("resourceInstanceName") final String resourceInstanceName,
			@ApiParam(value = "json describe the artifact", required = true) String data) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat = null;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		String requestURI = request.getRequestURI();
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		if (componentType == null) {
			log.debug("uploadArtifact: assetType parameter {} is not valid", assetType);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);

		if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
			log.debug("uploadArtifact: Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditExternalUploadArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
			log.debug("uploadArtifact: Missing USER_ID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.COMPONENT_MISSING_CONTACT);
			getComponentsUtils().auditExternalUploadArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty()) {
			try {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.uploadArtifactToRiByUUID(data, request, componentType, uuid, resourceInstanceName, additionalParams);
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to upload artifact");
					responseFormat = uploadArtifactEither.right().value();
					getComponentsUtils().auditExternalUploadArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5ByString((String) representation));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					getComponentsUtils().auditExternalUploadArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
				}
			} catch (Exception e) {
				final String message = "failed to upload artifact to a resource instance";
				BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
				log.debug(message, e);
				responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
			}
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * updates an artifact on a resource or service
	 * 
	 * @param assetType
	 * @param uuid
	 * @param artifactUUID
	 * @return
	 */
	@POST
	@Path("/{assetType}/{uuid}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "updates an artifact on a resource or service", httpMethod = "POST", notes = "uploads of artifact to a resource or service", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact Updated"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Asset not found") })
	public Response updateArtifact(@PathParam("assetType") final String assetType, @PathParam("uuid") final String uuid, @PathParam("artifactUUID") final String artifactUUID,
			@ApiParam(value = "json describe the artifact", required = true) String data) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat = null;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		String requestURI = request.getRequestURI();
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		if (componentType == null) {
			log.debug("updateArtifact: assetType parameter {} is not valid", assetType);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);

		// Mandatory
		if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
			log.debug("updateArtifact: Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditExternalUpdateArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
			log.debug("updateArtifact: Missing USER_ID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.COMPONENT_MISSING_CONTACT);
			getComponentsUtils().auditExternalUpdateArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty()) {
			try {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.updateArtifactOnComponentByUUID(data, request, componentType, uuid, artifactUUID, additionalParams);
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to update artifact");
					responseFormat = uploadArtifactEither.right().value();
					getComponentsUtils().auditExternalUpdateArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5ByString((String) representation));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					getComponentsUtils().auditExternalUpdateArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
				}
			} catch (Exception e) {
				final String message = "failed to update artifact on a resource or service";
				BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
				log.debug(message, e);
				responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
			}
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * updates an artifact on a resource instance
	 * 
	 * @param assetType
	 * @param uuid
	 * @param resourceInstanceName
	 * @param artifactUUID
	 * @return
	 */
	@POST
	@Path("/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "updates an artifact on a resource instance", httpMethod = "POST", notes = "uploads of artifact to a resource or service", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact Updated"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Asset not found") })
	public Response updateArtifactOnResourceInstance(@PathParam("assetType") final String assetType, @PathParam("uuid") final String uuid, @PathParam("resourceInstanceName") final String resourceInstanceName,
			@PathParam("artifactUUID") final String artifactUUID, @ApiParam(value = "json describe the artifact", required = true) String data) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat = null;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		String requestURI = request.getRequestURI();
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		if (componentType == null) {
			log.debug("updateArtifactOnResourceInstance: assetType parameter {} is not valid", assetType);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);

		if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
			log.debug("updateArtifactOnResourceInstance: Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditExternalUpdateArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
			log.debug("updateArtifactOnResourceInstance: Missing USER_ID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.COMPONENT_MISSING_CONTACT);
			getComponentsUtils().auditExternalUpdateArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty()) {
			try {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.updateArtifactOnRiByUUID(data, request, componentType, uuid, resourceInstanceName, artifactUUID, additionalParams);
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to update artifact");
					responseFormat = uploadArtifactEither.right().value();
					getComponentsUtils().auditExternalUpdateArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5ByString((String) representation));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					getComponentsUtils().auditExternalUpdateArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
				}
			} catch (Exception e) {
				final String message = "failed to update artifact on resource instance";
				BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
				log.debug(message, e);
				responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
			}
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * deletes an artifact of a resource or service
	 * 
	 * @param assetType
	 * @param uuid
	 * @param artifactUUID
	 * @return
	 */
	@DELETE
	@Path("/{assetType}/{uuid}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "deletes an artifact of a resource or service", httpMethod = "DELETE", notes = "deletes an artifact of a resource or service", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact Deleted"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Asset not found") })
	public Response deleteArtifact(@PathParam("assetType") final String assetType, @PathParam("uuid") final String uuid, @PathParam("artifactUUID") final String artifactUUID) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);

		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		if (componentType == null) {
			log.debug("deleteArtifact: assetType parameter {} is not valid", assetType);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);

		if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
			log.debug("deleteArtifact: Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditExternalDeleteArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
			log.debug("deleteArtifact: Missing USER_ID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.COMPONENT_MISSING_CONTACT);
			getComponentsUtils().auditExternalDeleteArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty()) {
			try {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.deleteArtifactOnComponentByUUID(request, componentType, uuid, artifactUUID, additionalParams);
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to delete artifact");
					responseFormat = uploadArtifactEither.right().value();
					getComponentsUtils().auditExternalDeleteArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5ByString((String) representation));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					getComponentsUtils().auditExternalDeleteArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
				}
			} catch (Exception e) {
				final String message = "failed to delete an artifact of a resource or service";
				BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
				log.debug(message, e);
				responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
			}
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * deletes an artifact of a resource instance
	 * 
	 * @param assetType
	 * @param uuid
	 * @param resourceInstanceName
	 * @return
	 */
	@DELETE
	@Path("{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "deletes an artifact of a resource insatnce", httpMethod = "DELETE", notes = "deletes an artifact of a resource insatnce", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact Deleted"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Asset not found") })
	public Response deleteArtifactOnResourceInstance(@PathParam("assetType") final String assetType, @PathParam("uuid") final String uuid, @PathParam("resourceInstanceName") final String resourceInstanceName,
			@PathParam("artifactUUID") final String artifactUUID) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		String userId = request.getHeader(Constants.USER_ID_HEADER);
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);

		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		if (componentType == null) {
			log.debug("deleteArtifactOnResourceInsatnce: assetType parameter {} is not valid", assetType);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParams = new EnumMap<>(AuditingFieldsKeysEnum.class);

		if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
			log.debug("deleteArtifactOnResourceInsatnce: Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditExternalDeleteArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty() && (userId == null || userId.isEmpty())) {
			log.debug("deleteArtifactOnResourceInsatnce: Missing USER_ID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.COMPONENT_MISSING_CONTACT);
			getComponentsUtils().auditExternalDeleteArtifact(responseFormat, componentType.getValue(), request, additionalParams);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty()) {
			try {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<ArtifactDefinition, ResponseFormat> uploadArtifactEither = artifactsLogic.deleteArtifactOnRiByUUID(request, componentType, uuid, resourceInstanceName, artifactUUID, additionalParams);
				if (uploadArtifactEither.isRight()) {
					log.debug("failed to delete artifact");
					responseFormat = uploadArtifactEither.right().value();
					getComponentsUtils().auditExternalDeleteArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					Object representation = RepresentationUtils.toRepresentation(uploadArtifactEither.left().value());
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5ByString((String) representation));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					getComponentsUtils().auditExternalDeleteArtifact(responseFormat, componentType.getValue(), request, additionalParams);
					responseWrapper.setInnerElement(buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), representation, headers));
				}
			} catch (Exception e) {
				final String message = "failed to delete an artifact of a resource instance";
				BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
				log.debug(message, e);
				responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
			}
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * downloads an artifact of a component (either a service or a resource) by artifactUUID
	 * 
	 * @param assetType
	 * @param uuid
	 * @param artifactUUID
	 * @return
	 */
	@GET
	@Path("/{assetType}/{uuid}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Download component artifact", httpMethod = "GET", notes = "Returns downloaded artifact", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact downloaded"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Artifact not found") })
	public Response downloadComponentArtifact(
			@ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("assetType") final String assetType,
			@PathParam("uuid") final String uuid, @PathParam("artifactUUID") final String artifactUUID) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		if (componentType == null) {
			log.debug("downloadComponentArtifact: assetType parameter {} is not valid", assetType);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<>(AuditingFieldsKeysEnum.class);

		if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
			log.debug("downloadComponentArtifact: Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditExternalDownloadArtifact(responseFormat, componentType.getValue(), request, additionalParam);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty()) {
			try {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<byte[], ResponseFormat> downloadComponentArtifactEither = artifactsLogic.downloadComponentArtifactByUUIDs(componentType, uuid, artifactUUID, additionalParam);
				if (downloadComponentArtifactEither.isRight()) {
					responseFormat = downloadComponentArtifactEither.right().value();
					getComponentsUtils().auditExternalDownloadArtifact(responseFormat, componentType.getValue(), request, additionalParam);
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					byte[] value = downloadComponentArtifactEither.left().value();
					InputStream is = new ByteArrayInputStream(value);
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5ByByteArray(value));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					getComponentsUtils().auditExternalDownloadArtifact(responseFormat, componentType.getValue(), request, additionalParam);
					responseWrapper.setInnerElement(buildOkResponse(responseFormat, is, headers));
				}
			} catch (Exception e) {
				final String message = "failed to download an artifact of a resource or service";
				BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
				log.debug(message, e);
				responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
			}
		}
		return responseWrapper.getInnerElement();
	}

	/**
	 * downloads an artifact of a resource instance of a component (either a service or a resource) by artifactUUID
	 * 
	 * @param assetType
	 * @param uuid
	 * @param resourceInstanceName
	 * @param artifactUUID
	 * @return
	 */
	@GET
	@Path("/{assetType}/{uuid}/resourceInstances/{resourceInstanceName}/artifacts/{artifactUUID}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Download resource instance artifact", httpMethod = "GET", notes = "Returns downloaded artifact", response = Response.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Artifact downloaded"), @ApiResponse(code = 401, message = "Authorization required"), @ApiResponse(code = 403, message = "Restricted operation"),
			@ApiResponse(code = 404, message = "Artifact not found") })
	public Response downloadResourceInstanceArtifact(
			@ApiParam(value = "valid values: resources / services", allowableValues = ComponentTypeEnum.RESOURCE_PARAM_NAME + "," + ComponentTypeEnum.SERVICE_PARAM_NAME) @PathParam("assetType") final String assetType,
			@PathParam("uuid") final String uuid, @PathParam("resourceInstanceName") final String resourceInstanceName, @PathParam("artifactUUID") final String artifactUUID) {

		Wrapper<Response> responseWrapper = new Wrapper<>();
		ResponseFormat responseFormat;
		String instanceIdHeader = request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER);
		String requestURI = request.getRequestURI();
		String url = request.getMethod() + " " + requestURI;
		log.debug("Start handle request of {}", url);
		ComponentTypeEnum componentType = ComponentTypeEnum.findByParamName(assetType);
		if (componentType == null) {
			log.debug("downloadResourceInstanceArtifact: assetType parameter {} is not valid", assetType);
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.INVALID_CONTENT);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		EnumMap<AuditingFieldsKeysEnum, Object> additionalParam = new EnumMap<>(AuditingFieldsKeysEnum.class);

		if (responseWrapper.isEmpty() && (instanceIdHeader == null || instanceIdHeader.isEmpty())) {
			log.debug("downloadResourceInstanceArtifact: Missing X-ECOMP-InstanceID header");
			responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID);
			getComponentsUtils().auditExternalDownloadArtifact(responseFormat, componentType.getValue(), request, additionalParam);
			responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
		}
		if (responseWrapper.isEmpty()) {
			try {
				ServletContext context = request.getSession().getServletContext();
				ArtifactsBusinessLogic artifactsLogic = getArtifactBL(context);
				Either<byte[], ResponseFormat> downloadResourceArtifactEither = artifactsLogic.downloadResourceInstanceArtifactByUUIDs(componentType, uuid, resourceInstanceName, artifactUUID, additionalParam);
				if (downloadResourceArtifactEither.isRight()) {
					responseFormat = downloadResourceArtifactEither.right().value();
					getComponentsUtils().auditExternalDownloadArtifact(responseFormat, componentType.getValue(), request, additionalParam);
					responseWrapper.setInnerElement(buildErrorResponse(responseFormat));
				} else {
					byte[] value = downloadResourceArtifactEither.left().value();
					InputStream is = new ByteArrayInputStream(value);
					Map<String, String> headers = new HashMap<>();
					headers.put(Constants.MD5_HEADER, GeneralUtility.calculateMD5ByByteArray(value));
					responseFormat = getComponentsUtils().getResponseFormat(ActionStatus.OK);
					getComponentsUtils().auditExternalDownloadArtifact(responseFormat, componentType.getValue(), request, additionalParam);
					responseWrapper.setInnerElement(buildOkResponse(responseFormat, is, headers));
				}
			} catch (Exception e) {
				final String message = "failed to download an artifact of a resource instance";
				BeEcompErrorManager.getInstance().logBeRestApiGeneralError(message);
				log.debug(message, e);
				responseWrapper.setInnerElement(buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR)));
			}
		}
		return responseWrapper.getInnerElement();
	}
}
