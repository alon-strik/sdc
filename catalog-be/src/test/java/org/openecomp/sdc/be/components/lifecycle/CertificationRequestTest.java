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

package org.openecomp.sdc.be.components.lifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.components.impl.ServiceBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.CertificationRequestTransition;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.ToscaRepresentation;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public class CertificationRequestTest extends LifecycleTestBase {

	private ComponentsUtils componentsUtils = new ComponentsUtils();

	protected ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder = Mockito.mock(ServiceDistributionArtifactsBuilder.class);
	protected ServiceBusinessLogic serviceBusinessLogic = Mockito.mock(ServiceBusinessLogic.class);
	protected ResourceOperation resourceOperation = Mockito.mock(ResourceOperation.class);
	protected CapabilityOperation capabilityOperation = Mockito.mock(CapabilityOperation.class);
	protected ToscaExportHandler toscaExportUtils = Mockito.mock(ToscaExportHandler.class);
	@InjectMocks
	private CertificationRequestTransition rfcObj = new CertificationRequestTransition(componentsUtils, lcOperation, serviceDistributionArtifactsBuilder, serviceBusinessLogic, capabilityOperation, toscaExportUtils);

	protected ToscaRepresentation toscaRepresentation = Mockito.mock(ToscaRepresentation.class);

	@Before
	public void setup() {
		Mockito.reset(resourceOperation);
		MockitoAnnotations.initMocks(this);
		super.setup();

		// checkout transition object
		rfcObj.setLifeCycleOperation(lcOperation);
		// checkoutObj.setAuditingManager(iAuditingManager);
		rfcObj.setConfigurationManager(configurationManager);
		componentsUtils.Init();

		Either<ToscaRepresentation, ToscaError> either = Either.left(toscaRepresentation);
		when(toscaExportUtils.exportComponent(Mockito.anyObject())).thenReturn(either);

	}

	@Test
	public void testCheckoutStateValidation() {
		Either<? extends Component, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject(false);

		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		Either<User, ResponseFormat> ownerResponse = rfcObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		// changeStateResult = rfcObj.changeStateOperation(resource, user,
		// owner, false);
		changeStateResult = rfcObj.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
		assertEquals(changeStateResult.isLeft(), true);

		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		// changeStateResult = rfcObj.changeStateOperation(resource, user,
		// owner, false);
		changeStateResult = rfcObj.changeState(ComponentTypeEnum.RESOURCE, resource, serviceBusinessLogic, user, owner, false, false);
		assertEquals(changeStateResult.isLeft(), true);
	}

	@Test
	public void testAlreadyRfc() {
		Either<Resource, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject(false);

		resource.setLifecycleState(LifecycleStateEnum.READY_FOR_CERTIFICATION);
		Either<User, ResponseFormat> ownerResponse = rfcObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		Either<Boolean, ResponseFormat> validateBeforeTransition = rfcObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.READY_FOR_CERTIFICATION);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());
		assertResponse(changeStateResult, ActionStatus.COMPONENT_SENT_FOR_CERTIFICATION, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

	}

	@Test
	public void testCertificationInProgress() {
		Either<Resource, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject(false);

		resource.setLifecycleState(LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		Either<User, ResponseFormat> ownerResponse = rfcObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		Either<Boolean, ResponseFormat> validateBeforeTransition = rfcObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFICATION_IN_PROGRESS);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());
		assertResponse(changeStateResult, ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

	}

	@Test
	public void testAlreadyCertified() {
		Either<Resource, ResponseFormat> changeStateResult;
		Resource resource = createResourceObject(false);

		resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		Either<User, ResponseFormat> ownerResponse = rfcObj.getComponentOwner(resource, ComponentTypeEnum.RESOURCE);
		assertTrue(ownerResponse.isLeft());
		User owner = ownerResponse.left().value();
		Either<Boolean, ResponseFormat> validateBeforeTransition = rfcObj.validateBeforeTransition(resource, ComponentTypeEnum.RESOURCE, user, owner, LifecycleStateEnum.CERTIFIED);
		assertEquals(validateBeforeTransition.isRight(), true);
		changeStateResult = Either.right(validateBeforeTransition.right().value());
		assertResponse(changeStateResult, ActionStatus.COMPONENT_ALREADY_CERTIFIED, resource.getName(), ComponentTypeEnum.RESOURCE.name().toLowerCase(), user.getFirstName(), user.getLastName(), user.getUserId());

	}

	@Test
	public void testValidateAllResourceInstanceCertified_SuccessWithoutRI() {
		Resource resource = new Resource();
		Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = rfcObj.validateAllResourceInstanceCertified(resource);
		assertTrue(validateAllResourceInstanceCertified.isLeft());
	}

	@Test
	public void testValidateAllResourceInstanceCertified_SuccessWithCertifiedResources() {
		Resource resource = new Resource();
		List<ComponentInstance> riList = new ArrayList<ComponentInstance>();
		ComponentInstance ri = new ComponentInstance();
		ri.setComponentVersion("2.0");
		riList.add(ri);
		resource.setComponentInstances(riList);

		Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = rfcObj.validateAllResourceInstanceCertified(resource);
		assertTrue(validateAllResourceInstanceCertified.isLeft());
	}

	@Test
	public void testValidateAllResourceInstanceCertified_FailWithUnCertifiedResourcesMinorVersion() {
		Resource resource = createVFWithRI("0.3");

		simulateCertifiedVersionExistForRI();

		Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = rfcObj.validateAllResourceInstanceCertified(resource);

		assertTrue(validateAllResourceInstanceCertified.isRight());
		ResponseFormat responseFormat = validateAllResourceInstanceCertified.right().value();
		assertTrue(responseFormat.getStatus() == HttpStatus.SC_FORBIDDEN);
		assertTrue(responseFormat.getMessageId().equals("SVC4559"));

	}

	@Test
	public void testValidateAllResourceInstanceCertified_FailWithUnCertifiedResourcesMajorVersion() {
		Resource resource = createVFWithRI("1.3");

		simulateCertifiedVersionExistForRI();

		Either<Boolean, ResponseFormat> validateAllResourceInstanceCertified = rfcObj.validateAllResourceInstanceCertified(resource);

		assertTrue(validateAllResourceInstanceCertified.isRight());
		ResponseFormat responseFormat = validateAllResourceInstanceCertified.right().value();
		assertTrue(responseFormat.getStatus() == HttpStatus.SC_FORBIDDEN);
		assertTrue(responseFormat.getMessageId().equals("SVC4559"));

	}

	private void simulateCertifiedVersionExistForRI() {
		Resource dummyResource = new Resource();
		Either<List<Resource>, StorageOperationStatus> result = Either.left(new ArrayList<Resource>() {
			{
				add(dummyResource);
			}
		});
		Mockito.when(resourceOperation.getResource(Mockito.anyString())).thenReturn(Either.left(dummyResource));
		Mockito.when(resourceOperation.findLastCertifiedResourceByUUID(Mockito.any(Resource.class))).thenReturn(result);
	}

	private Resource createVFWithRI(String riVersion) {
		Resource resource = new Resource();
		List<ComponentInstance> riList = new ArrayList<ComponentInstance>();
		ComponentInstance ri = new ComponentInstance();

		ri.setComponentVersion(riVersion);
		ri.setComponentUid("someUniqueId");
		riList.add(ri);
		resource.setComponentInstances(riList);
		return resource;
	}

}
