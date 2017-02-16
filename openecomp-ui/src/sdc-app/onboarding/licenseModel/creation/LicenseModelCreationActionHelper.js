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

import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';
import {actionTypes} from './LicenseModelCreationConstants.js';

function baseUrl() {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-license-models/`;
}

function createLicenseModel(licenseModel) {
	return RestAPIUtil.create(baseUrl(), {
		vendorName: licenseModel.vendorName,
		description: licenseModel.description,
		iconRef: 'icon'
	});
}


export default {

	open(dispatch) {
		dispatch({
			type: actionTypes.OPEN
		});
	},

	close(dispatch){
		dispatch({
			type: actionTypes.CLOSE
		});
	},

	dataChanged(dispatch, {deltaData}){
		dispatch({
			type: actionTypes.DATA_CHANGED,
			deltaData
		});
	},

	createLicenseModel(dispatch, {licenseModel}){
		return createLicenseModel(licenseModel).then(response => {
			LicenseModelActionHelper.addLicenseModel(dispatch, {
				licenseModel: {
					...licenseModel,
					id: response.value
				}
			});
			return response.value;
		});
	}
};
