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
/// <reference path="../../../../references"/>
module Sdc.ViewModels {
    'use strict';

    export interface IActivityLogViewModelScope extends IWorkspaceViewModelScope {
        activityDateArray: Array<any>; //this is in order to sort the dates
        activityLog: Array<Models.Activity>;
        preVersion:string;

        tableHeadersList: Array<any>;
        reverse: boolean;
        sortBy:string;
        searchBind:string;

        getActivityLog(uniqueId:string):void;
        onVersionChanged(version:any) : void;
        parseAction(action:string):string;
        sort(sortBy:string): void;
    }

    export class ActivityLogViewModel {

        static '$inject' = [
            '$scope',
            '$state',
            'Sdc.Services.ActivityLogService'
        ];

        constructor(private $scope:IActivityLogViewModelScope,
                    private $state:ng.ui.IStateService,
                    private activityLogService:Services.ActivityLogService
            ) {

            this.initScope();
            this.$scope.setValidState(true);
            this.initSortedTableScope();
            this.$scope.updateSelectedMenuItem();

            // Set default sorting
            this.$scope.sortBy = 'logDate';
        }

        private initScope():void {

            this.$scope.preVersion = this.$scope.component.version;

            this.$scope.onVersionChanged = (version:any):void => {
                if (version.versionNumber != this.$scope.component.version) {
                    this.$scope.isLoading = true;
                    this.$scope.getActivityLog(version.versionId);
                }
            };

            this.$scope.getActivityLog = (uniqueId:any):void => {

                let onError = (response) => {
                    this.$scope.isLoading = false;
                    console.info('onFaild', response);

                };

                let onSuccess = (response:Array<Models.Activity>) => {
                    this.$scope.activityLog = _.sortBy(response, function(o) { return o.TIMESTAMP; }); //response; //
                    this.$scope.isLoading = false;
                };

                this.$scope.isLoading = true;
                if (this.$scope.component.isResource()) {
                    this.activityLogService.getActivityLogService('resources', uniqueId).then(onSuccess, onError);
                }
                if (this.$scope.component.isService()) {
                    this.activityLogService.getActivityLogService('services', uniqueId).then(onSuccess, onError);
                }

            };

            if (!this.$scope.activityLog || this.$scope.preVersion != this.$scope.component.version) {
                this.$scope.getActivityLog(this.$scope.component.uniqueId);
            }

            this.$scope.parseAction = (action:string) => {
                return action ? action.split(/(?=[A-Z])/).join(' ') : '';
            };

        }

        private initSortedTableScope = ():void => {
            this.$scope.tableHeadersList = [
                {title: 'Date', property: 'logDate'},
                {title: 'Action', property: 'logAction'},
                {title: 'Comment', property: 'logComment'},
                {title: 'Username', property: 'logUsername'},
                {title: 'Status', property: 'logStatus'}
            ];

            this.$scope.sort = (sortBy:string):void => {
                this.$scope.reverse = (this.$scope.sortBy === sortBy) ? !this.$scope.reverse : false;
                this.$scope.sortBy = sortBy;
            };
        };

    }
}
