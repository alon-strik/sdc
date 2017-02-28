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

package org.openecomp.sdc.ci.tests.datatypes.enums;

import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

public enum MandatoryResourceArtifactTypeEnum {

	TEST_SCRIPTS(null, "testscripts"), 
	FEATURES(null, "features"), 
	CAPACITY(null, "capacity"), 
	VENDOR_TEST_RESULT(null,"vendortestresult"), 
	CLOUD_QUESTIONNAIRE(null, "cloudquestionnaire");

	String artifactName;
	String logicalName;

	private MandatoryResourceArtifactTypeEnum(String artifactName, String logicalName) {
		this.artifactName = artifactName;
		this.logicalName = logicalName;
	}

	public String getArtifactName() {
		return artifactName;
	}

	public String getLogicalName() {
		return logicalName;
	}

	public ArtifactGroupTypeEnum getGroupType() {
		return ArtifactGroupTypeEnum.INFORMATIONAL;
	}

}