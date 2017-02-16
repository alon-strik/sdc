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

package org.openecomp.sdc.be.model.tosca.constraints;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;

public class GreaterThanConstraint extends AbstractComparablePropertyConstraint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 405723215512121896L;

	public GreaterThanConstraint(String greaterThan) {
		super();
		this.greaterThan = greaterThan;
	}

	@NotNull
	private String greaterThan;

	@Override
	public void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
		initialize(greaterThan, propertyType);
	}

	@Override
	protected void doValidate(Object propertyValue) throws ConstraintViolationException {
		if (getComparable().compareTo(propertyValue) >= 0) {
			throw new ConstraintViolationException(propertyValue + " < " + greaterThan);
		}
	}

	public String getGreaterThan() {
		return greaterThan;
	}

	public void setGreaterThan(String greaterThan) {
		this.greaterThan = greaterThan;
	}

}
