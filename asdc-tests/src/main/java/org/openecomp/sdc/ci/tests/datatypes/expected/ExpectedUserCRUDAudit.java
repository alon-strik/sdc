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

package org.openecomp.sdc.ci.tests.datatypes.expected;

public class ExpectedUserCRUDAudit {
	String action;
	String modifier;
	String status;
	String desc;
	String userBefore;
	String userAfter;

	public ExpectedUserCRUDAudit(String action, String modifier, String status, String desc, String userBefore,
			String userAfter) {
		super();
		this.action = action;
		this.modifier = modifier;
		this.status = status;
		this.desc = desc;
		this.userBefore = userBefore;
		this.userAfter = userAfter;
	}

	public ExpectedUserCRUDAudit() {
		action = null;
		modifier = null;
		userBefore = null;
		userAfter = null;
		status = null;
		desc = null;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getUserBefore() {
		return userBefore;
	}

	public void setUserBefore(String userBefore) {
		this.userBefore = userBefore;
	}

	public String getUserAfter() {
		return userAfter;
	}

	public void setUserAfter(String userAfter) {
		this.userAfter = userAfter;
	}
}
