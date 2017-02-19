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

package org.openecomp.sdc.be.model.tosca.validators;

import java.util.Map;

import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.common.util.ValidationUtils;

public class HeatCommaDelimitedListValidator implements PropertyTypeValidator {

	private static HeatCommaDelimitedListValidator stringValidator = new HeatCommaDelimitedListValidator();

	public static HeatCommaDelimitedListValidator getInstance() {
		return stringValidator;
	}

	private HeatCommaDelimitedListValidator() {

	}

	@Override
	public boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {

		if (value == null || true == value.isEmpty()) {
			return true;
		}

		String coverted = ValidationUtils.removeNoneUtf8Chars(value);
		return ValidationUtils.validateIsEnglish(coverted);
	}

	@Override
	public boolean isValid(String value, String innerType) {
		return isValid(value, innerType, null);
	}
}
