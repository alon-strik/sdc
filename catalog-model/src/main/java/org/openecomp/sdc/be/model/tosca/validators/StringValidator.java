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

import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.Configuration.ToscaValidatorsConfig;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringValidator implements PropertyTypeValidator {

	public static final int DEFAULT_STRING_MAXIMUM_LENGTH = 100;

	public static int STRING_MAXIMUM_LENGTH = DEFAULT_STRING_MAXIMUM_LENGTH;

	private static Logger log = LoggerFactory.getLogger(StringValidator.class.getName());

	private static StringValidator stringValidator = new StringValidator();

	public static StringValidator getInstance() {
		return stringValidator;
	}

	private StringValidator() {
		if (ConfigurationManager.getConfigurationManager() != null) {
			ToscaValidatorsConfig toscaValidators = ConfigurationManager.getConfigurationManager().getConfiguration()
					.getToscaValidators();
			log.debug("toscaValidators={}", toscaValidators);
			if (toscaValidators != null) {
				Integer stringMaxLength = toscaValidators.getStringMaxLength();
				if (stringMaxLength != null) {
					STRING_MAXIMUM_LENGTH = stringMaxLength;
				}
			}
		}
	}

	@Override
	public boolean isValid(String value, String innerType, Map<String, DataTypeDefinition> allDataTypes) {

		if (value == null || true == value.isEmpty()) {
			return true;
		}

		if (value.length() > STRING_MAXIMUM_LENGTH) {
			log.debug("parameter String length {} is higher the configured({})", value.length(), STRING_MAXIMUM_LENGTH);
			return false;
		}
		String coverted = ValidationUtils.removeNoneUtf8Chars(value);
		boolean isValid = ValidationUtils.validateIsAscii(coverted);

		if (false == isValid) {
			log.debug("parameter String value {} is not ascii string.", (value != null ? value.substring(0, Math.min(value.length(), 20)) : null));
		}

		return isValid;
	}

	@Override
	public boolean isValid(String value, String innerType) {
		return isValid(value, innerType, null);
	}
}
