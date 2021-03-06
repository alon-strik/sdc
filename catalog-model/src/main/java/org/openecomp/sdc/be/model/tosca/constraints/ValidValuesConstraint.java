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
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintValueDoNotMatchPropertyTypeException;
import org.openecomp.sdc.be.model.tosca.constraints.exception.ConstraintViolationException;

//import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;

public class ValidValuesConstraint extends AbstractPropertyConstraint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5906087180079892853L;

	@NotNull
	private List<String> validValues;
	// @JsonIgnore
	private Set<Object> validValuesTyped;

	public ValidValuesConstraint(List<String> validValues) {
		super();
		this.validValues = validValues;
	}

	public ValidValuesConstraint() {
		super();
	}

	@Override
	public void initialize(ToscaType propertyType) throws ConstraintValueDoNotMatchPropertyTypeException {
		validValuesTyped = Sets.newHashSet();
		if (validValues == null) {
			throw new ConstraintValueDoNotMatchPropertyTypeException(
					"validValues constraint has invalid value <> property type is <" + propertyType.toString() + ">");
		}
		for (String value : validValues) {
			if (!propertyType.isValidValue(value)) {
				throw new ConstraintValueDoNotMatchPropertyTypeException("validValues constraint has invalid value <"
						+ value + "> property type is <" + propertyType.toString() + ">");
			} else {
				validValuesTyped.add(propertyType.convert(value));
			}
		}
	}

	@Override
	public void validate(Object propertyValue) throws ConstraintViolationException {
		if (propertyValue == null) {
			throw new ConstraintViolationException("Value to validate is null");
		}
		if (!validValuesTyped.contains(propertyValue)) {
			throw new ConstraintViolationException("The value is not in the list of valid values");
		}
	}

	public List<String> getValidValues() {
		return validValues;
	}

	public void setValidValues(List<String> validValues) {
		this.validValues = validValues;
	}

}
