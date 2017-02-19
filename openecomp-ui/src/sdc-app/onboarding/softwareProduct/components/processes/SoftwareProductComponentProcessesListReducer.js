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

import {actionTypes} from './SoftwareProductComponentProcessesConstants.js';

export default (state = [], action) => {
	switch (action.type) {
		case actionTypes.FETCH_SOFTWARE_PRODUCT_COMPONENTS_PROCESSES:
			return [...action.processesList];
		case actionTypes.EDIT_SOFTWARE_PRODUCT_COMPONENTS_PROCESS:
			const indexForEdit = state.findIndex(process => process.id === action.process.id);
			return [...state.slice(0, indexForEdit), action.process, ...state.slice(indexForEdit + 1)];
		case actionTypes.ADD_SOFTWARE_PRODUCT_COMPONENTS_PROCESS:
			return [...state, action.process];
		case actionTypes.DELETE_SOFTWARE_PRODUCT_COMPONENTS_PROCESS:
			return state.filter(process => process.id !== action.processId);
		default:
			return state;
	}
};