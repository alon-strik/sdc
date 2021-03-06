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

import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import {actionTypes} from './SoftwareProductCreationConstants.js';


function baseUrl() {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/`;
}

function createSoftwareProduct(softwareProduct) {
	return RestAPIUtil.create(baseUrl(), {
		...softwareProduct,
		icon: 'icon',
		licensingData: {}
	});
}

const SoftwareProductCreationActionHelper = {

	open(dispatch) {
		SoftwareProductActionHelper.loadSoftwareProductAssociatedData(dispatch);
		dispatch({
			type: actionTypes.OPEN
		});
	},

	resetData(dispatch) {
		dispatch({
			type: actionTypes.RESET_DATA
		});
	},

	changeData(dispatch, {deltaData}) {
		dispatch({
			type: actionTypes.DATA_CHANGED,
			deltaData
		});
	},

	createSoftwareProduct(dispatch, {softwareProduct}) {
		return createSoftwareProduct(softwareProduct).then(response => {
			SoftwareProductActionHelper.addSoftwareProduct(dispatch, {
				softwareProduct: {
					...softwareProduct,
					id: response.vspId
				}
			});
			return response.vspId;
		});
	}

};

export default SoftwareProductCreationActionHelper;
