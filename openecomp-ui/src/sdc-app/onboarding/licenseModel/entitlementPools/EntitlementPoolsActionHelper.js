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
import {actionTypes as entitlementPoolsActionTypes } from './EntitlementPoolsConstants.js';
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';

function baseUrl(licenseModelId) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-license-models/${licenseModelId}/entitlement-pools`;
}

function fetchEntitlementPoolsList(licenseModelId, version) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(licenseModelId)}${versionQuery}`);
}

function postEntitlementPool(licenseModelId, entitlementPool) {
	return RestAPIUtil.create(baseUrl(licenseModelId), {
		name: entitlementPool.name,
		description: entitlementPool.description,
		thresholdValue: entitlementPool.thresholdValue,
		thresholdUnits: entitlementPool.thresholdUnits,
		entitlementMetric: entitlementPool.entitlementMetric,
		increments: entitlementPool.increments,
		aggregationFunction: entitlementPool.aggregationFunction,
		operationalScope: entitlementPool.operationalScope,
		time: entitlementPool.time,
		manufacturerReferenceNumber: entitlementPool.manufacturerReferenceNumber
	});
}


function putEntitlementPool(licenseModelId, previousEntitlementPool, entitlementPool) {
	return RestAPIUtil.save(`${baseUrl(licenseModelId)}/${entitlementPool.id}`, {
		name: entitlementPool.name,
		description: entitlementPool.description,
		thresholdValue: entitlementPool.thresholdValue,
		thresholdUnits: entitlementPool.thresholdUnits,
		entitlementMetric: entitlementPool.entitlementMetric,
		increments: entitlementPool.increments,
		aggregationFunction: entitlementPool.aggregationFunction,
		operationalScope: entitlementPool.operationalScope,
		time: entitlementPool.time,
		manufacturerReferenceNumber: entitlementPool.manufacturerReferenceNumber
	});
}

function deleteEntitlementPool(licenseModelId, entitlementPoolId) {
	return RestAPIUtil.destroy(`${baseUrl(licenseModelId)}/${entitlementPoolId}`);
}


export default {
	fetchEntitlementPoolsList(dispatch, {licenseModelId, version}) {
		return fetchEntitlementPoolsList(licenseModelId, version).then(response => dispatch({
			type: entitlementPoolsActionTypes.ENTITLEMENT_POOLS_LIST_LOADED,
			response
		}));
	},

	openEntitlementPoolsEditor(dispatch, {entitlementPool} = {}) {
		dispatch({
			type: entitlementPoolsActionTypes.entitlementPoolsEditor.OPEN,
			entitlementPool
		});
	},

	deleteEntitlementPool(dispatch, {licenseModelId, entitlementPoolId}) {
		return deleteEntitlementPool(licenseModelId, entitlementPoolId).then(() => {
			dispatch({
				type: entitlementPoolsActionTypes.DELETE_ENTITLEMENT_POOL,
				entitlementPoolId
			});
		});
	},

	entitlementPoolsEditorDataChanged(dispatch, {deltaData}) {
		dispatch({
			type: entitlementPoolsActionTypes.entitlementPoolsEditor.DATA_CHANGED,
			deltaData
		});
	},

	closeEntitlementPoolsEditor(dispatch) {
		dispatch({
			type: entitlementPoolsActionTypes.entitlementPoolsEditor.CLOSE
		});
	},

	saveEntitlementPool(dispatch, {licenseModelId, previousEntitlementPool, entitlementPool}) {
		if (previousEntitlementPool) {
			return putEntitlementPool(licenseModelId, previousEntitlementPool, entitlementPool).then(() => {
				dispatch({
					type: entitlementPoolsActionTypes.EDIT_ENTITLEMENT_POOL,
					entitlementPool
				});
			});
		}
		else {
			return postEntitlementPool(licenseModelId, entitlementPool).then(response => {
				dispatch({
					type: entitlementPoolsActionTypes.ADD_ENTITLEMENT_POOL,
					entitlementPool: {
						...entitlementPool,
						id: response.value
					}
				});
			});
		}
	},

	hideDeleteConfirm(dispatch) {
		dispatch({
			type: entitlementPoolsActionTypes.ENTITLEMENT_POOLS_DELETE_CONFIRM,
			entitlementPoolToDelete: false
		});
	},
	openDeleteEntitlementPoolConfirm(dispatch, {entitlementPool}) {
		dispatch({
			type: entitlementPoolsActionTypes.ENTITLEMENT_POOLS_DELETE_CONFIRM,
			entitlementPoolToDelete: entitlementPool
		});
	},

	switchVersion(dispatch, {licenseModelId, version}) {
		LicenseModelActionHelper.fetchLicenseModelById(dispatch, {licenseModelId, version}).then(() => {
			this.fetchEntitlementPoolsList(dispatch, {licenseModelId, version});
		});
	}
};
