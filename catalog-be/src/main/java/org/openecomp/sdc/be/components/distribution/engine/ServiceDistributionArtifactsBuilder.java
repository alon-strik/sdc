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

package org.openecomp.sdc.be.components.distribution.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.IArtifactOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.model.operations.impl.ServiceOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("serviceDistributionArtifactsBuilder")
public class ServiceDistributionArtifactsBuilder {

	private int defaultArtifactInstallTimeout = 60;

	private static Logger logger = LoggerFactory.getLogger(ServiceDistributionArtifactsBuilder.class.getName());

	final static String BASE_ARTIFACT_URL = "/asdc/v1/catalog/services/%s/%s/";
	final static String RESOURCE_ARTIFACT_URL = BASE_ARTIFACT_URL + "resources/%s/%s/artifacts/%s";
	final static String SERVICE_ARTIFACT_URL = BASE_ARTIFACT_URL + "artifacts/%s";

	final static String RESOURCE_INSTANCE_ARTIFACT_URL = BASE_ARTIFACT_URL + "resourceInstances/%s/artifacts/%s";

	@javax.annotation.Resource
	ServiceOperation serviceOperation;

	@javax.annotation.Resource
	ResourceOperation resourceOperation;

	@javax.annotation.Resource
	InterfaceLifecycleOperation interfaceLifecycleOperation;

	@javax.annotation.Resource
	IArtifactOperation artifactOperation;

	/*
	 * @javax.annotation.Resource private InformationDeployedArtifactsBusinessLogic informationDeployedArtifactsBusinessLogic;
	 */

	@PostConstruct
	private void init() {
		defaultArtifactInstallTimeout = ConfigurationManager.getConfigurationManager().getConfiguration().getDefaultHeatArtifactTimeoutMinutes();
	}

	public ServiceOperation getServiceOperation() {
		return serviceOperation;
	}

	public void setServiceOperation(ServiceOperation serviceOperation) {
		this.serviceOperation = serviceOperation;
	}

	public ResourceOperation getResourceOperation() {
		return resourceOperation;
	}

	public void setResourceOperation(ResourceOperation resourceOperation) {
		this.resourceOperation = resourceOperation;
	}

	public InterfaceLifecycleOperation getInterfaceLifecycleOperation() {
		return interfaceLifecycleOperation;
	}

	public void setInterfaceLifecycleOperation(InterfaceLifecycleOperation interfaceLifecycleOperation) {
		this.interfaceLifecycleOperation = interfaceLifecycleOperation;
	}

	public INotificationData buildResourceInstanceForDistribution(Service service, String distributionId) {
		INotificationData notificationData = new NotificationDataImpl();

		notificationData.setResources(convertRIToJsonContanier(service));
		notificationData.setServiceName(service.getName());
		notificationData.setServiceVersion(service.getVersion());
		notificationData.setDistributionID(distributionId);
		notificationData.setServiceUUID(service.getUUID());
		notificationData.setServiceDescription(service.getDescription());
		notificationData.setServiceInvariantUUID(service.getInvariantUUID());

		logger.debug("Before returning notification data object {}", notificationData);

		return notificationData;

	}

	public INotificationData buildServiceForDistribution(INotificationData notificationData, Service service) {

		notificationData.setServiceArtifacts(convertServiceArtifactsToArtifactInfo(service));

		logger.debug("Before returning notification data object {}", notificationData);

		return notificationData;

	}

	private List<ArtifactInfoImpl> convertServiceArtifactsToArtifactInfo(Service service) {

		Map<String, ArtifactDefinition> serviceArtifactsMap = service.getDeploymentArtifacts();
		List<ArtifactDefinition> ret = new ArrayList<ArtifactDefinition>();

		for (ArtifactDefinition artifactDef : serviceArtifactsMap.values()) {
			if (artifactDef.checkEsIdExist()) {
				ret.add(artifactDef);
			}
		}
		List<ArtifactInfoImpl> artifacts = ArtifactInfoImpl.convertServiceArtifactToArtifactInfoImpl(service, ret);
		return artifacts;
	}

	private List<JsonContainerResourceInstance> convertRIToJsonContanier(Service service) {
		List<JsonContainerResourceInstance> ret = new ArrayList<JsonContainerResourceInstance>();
		if (service.getComponentInstances() != null) {
			for (ComponentInstance resourceInstance : service.getComponentInstances()) {
				String resourceType = "VF";
				List<ArtifactDefinition> artifactsDefList = getArtifactsWithPayload(resourceInstance);
				List<ArtifactInfoImpl> artifacts = ArtifactInfoImpl.convertToArtifactInfoImpl(service, resourceInstance, artifactsDefList);
				Either<String, StorageOperationStatus> responseResult = resourceOperation.getInvariantUUID(NodeTypeEnum.Resource, resourceInstance.getComponentUid(), false);
				String resourceInvariantUUID = null;
				if (responseResult.isRight()) {
					logger.debug("Resource {} Invariant UUID retrieving failed", resourceInstance.getComponentUid());
				} else {
					resourceInvariantUUID = responseResult.left().value();
				}
				JsonContainerResourceInstance jsonContainer = new JsonContainerResourceInstance(resourceInstance, resourceInvariantUUID, resourceType, artifacts);
				ret.add(jsonContainer);
			}

		}
		return ret;
	}

	private List<ArtifactDefinition> getArtifactsWithPayload(ComponentInstance resourceInstance) {
		List<ArtifactDefinition> ret = new ArrayList<ArtifactDefinition>();

		// List<ArtifactDefinition> informationDeployedArtifacts =
		// informationDeployedArtifactsBusinessLogic.getInformationalDeployedArtifactsForResourceInstance(resourceInstance);
		List<ArtifactDefinition> deployableArtifacts = new ArrayList<ArtifactDefinition>();
		// deployableArtifacts.addAll(informationDeployedArtifacts);
		if (resourceInstance.getDeploymentArtifacts() != null) {
			deployableArtifacts.addAll(resourceInstance.getDeploymentArtifacts().values());
		}

		for (ArtifactDefinition artifactDef : deployableArtifacts) {
			if (artifactDef.checkEsIdExist()) {
				ret.add(artifactDef);
			}
		}

		return ret;
	}

	/**
	 * build the url for resource intance artifact
	 * 
	 * @param service
	 * @param resourceData
	 * @param artifactName
	 * @return
	 */
	public static String buildResourceInstanceArtifactUrl(Service service, ComponentInstance resourceInstance, String artifactName) {

		String url = String.format(RESOURCE_INSTANCE_ARTIFACT_URL, service.getSystemName(), service.getVersion(), resourceInstance.getNormalizedName(), artifactName);

		logger.debug("After building artifact url {}", url);

		return url;
	}

	/**
	 * build the url for resource intance artifact
	 * 
	 * @param service
	 * @param resourceData
	 * @param artifactName
	 * @return
	 */
	public static String buildServiceArtifactUrl(Service service, String artifactName) {

		String url = String.format(SERVICE_ARTIFACT_URL, service.getSystemName(), service.getVersion(), artifactName);

		logger.debug("After building artifact url {}", url);

		return url;

	}

	/**
	 * Retrieve all deployment artifacts of all resources under a given service
	 * 
	 * @param resourceArtifactsResult
	 * @param service
	 * @param deConfiguration
	 * @return
	 */
	public Either<Boolean, StorageOperationStatus> isServiceContainsDeploymentArtifacts(Service service) {

		Either<Boolean, StorageOperationStatus> result = Either.left(false);
		Map<String, ArtifactDefinition> serviseArtifactsMap = service.getDeploymentArtifacts();
		if (serviseArtifactsMap != null && !serviseArtifactsMap.isEmpty()) {
			result = Either.left(true);
			return result;
		}

		List<ComponentInstance> resourceInstances = service.getComponentInstances();

		if (resourceInstances != null) {
			for (ComponentInstance resourceInstance : resourceInstances) {

				Map<String, ArtifactDefinition> deploymentArtifactsMapper = resourceInstance.getDeploymentArtifacts();
				// List<ArtifactDefinition> informationDeployedArtifacts =
				// informationDeployedArtifactsBusinessLogic.getInformationalDeployedArtifactsForResourceInstance(resourceInstance);

				boolean isDeployableArtifactFound = isContainsPayload(deploymentArtifactsMapper.values());// ||
																											// isContainsPayload(informationDeployedArtifacts);
				if (isDeployableArtifactFound) {
					result = Either.left(true);
					break;
				}

			}

		}

		return result;
	}

	private boolean isContainsPayload(Collection<ArtifactDefinition> collection) {
		boolean payLoadFound = collection != null && collection.stream().anyMatch(p -> p.checkEsIdExist());
		return payLoadFound;
	}

}
