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

package org.openecomp.sdc.be.servlets;

import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.openecomp.sdc.be.components.impl.GroupBusinessLogic;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.info.GroupDefinitionInfo;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcabi.aspects.Loggable;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import fj.data.Either;

/**
 * Root resource (exposed at "/" path)
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/v1/catalog")
@Api(value = "Resource Group Servlet", description = "Resource Group Servlet")
@Singleton
public class GroupServlet extends AbstractValidationsServlet {

	private static Logger log = LoggerFactory.getLogger(GroupServlet.class.getName());

	private Gson gson = new Gson();

	@GET
	@Path("/{containerComponentType}/{componentId}/groups/{groupId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get group artifacts ", httpMethod = "GET", notes = "Returns artifacts metadata according to groupId", response = Resource.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "group found"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 404, message = "Group not found") })
	public Response getGroupArtifactById(@PathParam("containerComponentType") final String containerComponentType, @PathParam("componentId") final String componentId, @PathParam("groupId") final String groupId,
			@Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {
		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("(get) Start handle request of {}", url);
		Response response = null;

		try {

			GroupBusinessLogic businessLogic = this.getGroupBL(context);
			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			Either<GroupDefinitionInfo, ResponseFormat> actionResponse = businessLogic.getGroupWithArtifactsById(componentTypeEnum, componentId, groupId, userId, false);

			if (actionResponse.isRight()) {
				log.debug("failed to get all non abstract {}", containerComponentType);
				return buildErrorResponse(actionResponse.right().value());
			}

			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), actionResponse.left().value());

		} catch (Throwable e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "getGroupArtifactById");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("getGroupArtifactById");
			log.debug("getGroupArtifactById unexpected exception", e);
			return buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
		}

	}

	@PUT
	@Path("/{containerComponentType}/{componentId}/groups/{groupUniqueId}/metadata")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update Group Metadata", httpMethod = "PUT", notes = "Returns updated group definition", response = GroupDefinition.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Group Updated"), @ApiResponse(code = 403, message = "Restricted operation"), @ApiResponse(code = 400, message = "Invalid content / Missing content") })
	public Response updateGroupMetadata(@PathParam("containerComponentType") final String containerComponentType, @PathParam("componentId") final String componentId, @PathParam("groupUniqueId") final String groupUniqueId,
			@ApiParam(value = "Service object to be Updated", required = true) String data, @Context final HttpServletRequest request, @HeaderParam(value = Constants.USER_ID_HEADER) String userId) {

		ServletContext context = request.getSession().getServletContext();
		String url = request.getMethod() + " " + request.getRequestURI();
		log.debug("Start handle request of {}", url);

		User user = new User();
		user.setUserId(userId);
		log.debug("modifier id is {}", userId);

		Response response = null;

		try {
			GroupBusinessLogic businessLogic = getGroupBL(context);

			Either<GroupDefinition, ResponseFormat> convertResponse = parseToGroup(data);
			if (convertResponse.isRight()) {
				log.debug("failed to parse group");
				response = buildErrorResponse(convertResponse.right().value());
				return response;
			}
			GroupDefinition updatedGroup = convertResponse.left().value();

			// Update GroupDefinition
			ComponentTypeEnum componentTypeEnum = ComponentTypeEnum.findByParamName(containerComponentType);
			Either<GroupDefinition, ResponseFormat> actionResponse = businessLogic.updateGroupMetadata(componentId, user, groupUniqueId, componentTypeEnum, updatedGroup, true);

			if (actionResponse.isRight()) {
				log.debug("failed to update GroupDefinition");
				response = buildErrorResponse(actionResponse.right().value());
				return response;
			}

			GroupDefinition group = actionResponse.left().value();
			Object result = RepresentationUtils.toRepresentation(group);
			return buildOkResponse(getComponentsUtils().getResponseFormat(ActionStatus.OK), result);

		} catch (Exception e) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeRestApiGeneralError, "Update Group Metadata");
			BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Update Group Metadata");
			log.debug("update group metadata failed with exception", e);
			response = buildErrorResponse(getComponentsUtils().getResponseFormat(ActionStatus.GENERAL_ERROR));
			return response;

		}
	}

	/**
	 * Convert data to GroupDefinition object
	 * 
	 * @param groupJson
	 * @return
	 */
	public Either<GroupDefinition, ResponseFormat> parseToGroup(String groupJson) {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			GroupDefinition groupDefinition = gson.fromJson(groupJson, GroupDefinition.class);
			return Either.left(groupDefinition);
		} catch (Exception e) {
			log.debug("Failed to parse json (group data) to GroupDefinition object");
			ResponseFormat responseFormat = componentsUtils.getResponseFormat(ActionStatus.INVALID_CONTENT);
			return Either.right(responseFormat);
		}
	}

}
