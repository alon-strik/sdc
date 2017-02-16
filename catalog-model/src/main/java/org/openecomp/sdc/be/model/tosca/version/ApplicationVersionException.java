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

package org.openecomp.sdc.be.model.tosca.version;

import org.openecomp.sdc.be.model.tosca.constraints.exception.TechnicalException;

public class ApplicationVersionException extends TechnicalException {

	private static final long serialVersionUID = -5192834855057177252L;

	public ApplicationVersionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApplicationVersionException(String message) {
		super(message);
	}
}
