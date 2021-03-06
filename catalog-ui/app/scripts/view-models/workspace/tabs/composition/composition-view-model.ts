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

    export interface ICompositionViewModelScope extends IWorkspaceViewModelScope {

        currentComponent: Models.Components.Component;
        selectedComponent: Models.Components.Component;
        isLoading: boolean;
        graphApi:any;
        sharingService:Sdc.Services.SharingService;
        sdcMenu:Models.IAppMenu;
        version:string;
        isViewOnly:boolean;
        isLoadingRightPanel:boolean;
        setComponent(component: Models.Components.Component);
        isComponentInstanceSelected():boolean;
        updateSelectedComponent(): void
        openUpdateModal();
        deleteSelectedComponentInstance():void;
        onBackgroundClick():void;
        setSelectedInstance(componentInstance: Models.ComponentsInstances.ComponentInstance): void;
        printScreen():void;

        cacheComponentsInstancesFullData: Models.Components.Component;
    }

    export class CompositionViewModel {

        static '$inject' = [
            '$scope',
            '$log',
            'sdcMenu',
            'MenuHandler',
            '$modal',
            '$templateCache',
            '$state',
            'Sdc.Services.SharingService',
            '$filter',
            'Sdc.Services.CacheService',
            'ComponentFactory',
            'ChangeLifecycleStateHandler',
            'LeftPaletteLoaderService',
            'ModalsHandler',
            'EventListenerService'
        ];

        constructor(private $scope:ICompositionViewModelScope,
                    private $log: ng.ILogService,
                    private sdcMenu:Models.IAppMenu,
                    private MenuHandler: Utils.MenuHandler,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $templateCache:ng.ITemplateCacheService,
                    private $state:ng.ui.IStateService,
                    private sharingService:Services.SharingService,
                    private $filter:ng.IFilterService,
                    private cacheService:Services.CacheService,
                    private ComponentFactory: Utils.ComponentFactory,
                    private ChangeLifecycleStateHandler: Sdc.Utils.ChangeLifecycleStateHandler,
                    private LeftPaletteLoaderService: Services.Components.LeftPaletteLoaderService,
                    private ModalsHandler: Sdc.Utils.ModalsHandler,
                    private eventListenerService:Services.EventListenerService) {

            this.$scope.setValidState(true);
            this.initScope();
            this.$scope.updateSelectedMenuItem();
            this.registerGraphEvents(this.$scope);
        }
        private cacheComponentsInstancesFullData: Array<Models.Components.Component>;

        private initComponent = ():void => {

            this.$scope.currentComponent = this.$scope.component;
            this.$scope.selectedComponent = this.$scope.currentComponent;
            this.updateUuidMap();
            this.$scope.isViewOnly = this.$scope.isViewMode();
        };
        private registerGraphEvents = (scope:ICompositionViewModelScope):void => {

            this.eventListenerService.registerObserverCallback(Utils.Constants.GRAPH_EVENTS.ON_NODE_SELECTED, scope.setSelectedInstance);
            this.eventListenerService.registerObserverCallback(Utils.Constants.GRAPH_EVENTS.ON_GRAPH_BACKGROUND_CLICKED, scope.onBackgroundClick);

        }
        private openUpdateComponentInstanceNameModal = ():void => {

            let viewModelsHtmlBasePath:string = '/app/scripts/view-models/';
            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get(viewModelsHtmlBasePath + 'forms/resource-instance-name-form/resource-instance-name-view.html'),
                controller: 'Sdc.ViewModels.ResourceInstanceNameViewModel',
                size: 'sdc-sm',
                backdrop: 'static',
                resolve: {
                    component: ():Models.Components.Component => {
                        return this.$scope.currentComponent;

                    }
                }
            };

            let modalInstance:ng.ui.bootstrap.IModalServiceInstance = this.$modal.open(modalOptions);
            modalInstance.result.then(():void => {
                this.eventListenerService.notifyObservers(Utils.Constants.GRAPH_EVENTS.ON_COMPONENT_INSTANCE_NAME_CHANGED, this.$scope.currentComponent.selectedInstance);
                //this.$scope.graphApi.updateNodeName(this.$scope.currentComponent.selectedInstance);
            });
        };

        private removeSelectedComponentInstance = ():void => {
            this.eventListenerService.notifyObservers(Utils.Constants.GRAPH_EVENTS.ON_DELETE_MULTIPLE_COMPONENTS);
        };

        private updateUuidMap = ():void => {
            /**
             * In case user press F5, the page is refreshed and this.sharingService.currentEntity will be undefined,
             * but after loadService or loadResource this.sharingService.currentEntity will be defined.
             * Need to update the uuidMap with the new resource or service.
             */
             this.sharingService.addUuidValue(this.$scope.currentComponent.uniqueId,this.$scope.currentComponent.uuid);
        };

        private initScope = ():void => {

            this.$scope.sharingService = this.sharingService;
            this.$scope.sdcMenu = this.sdcMenu;
            this.$scope.isLoading = false;
            this.$scope.isLoadingRightPanel = false;
            this.$scope.graphApi = {};
            this.$scope.version = this.cacheService.get('version');
            this.initComponent();

            this.cacheComponentsInstancesFullData = new Array<Models.Components.Component>();

            this.$scope.isComponentInstanceSelected = ():boolean => {
                return  this.$scope.currentComponent && this.$scope.currentComponent.selectedInstance  != undefined && this.$scope.currentComponent.selectedInstance != null;
            };

            this.$scope.updateSelectedComponent = (): void => {
                if(this.$scope.currentComponent.selectedInstance){

                    let componentParent = _.find(this.cacheComponentsInstancesFullData, (component) => {
                       return component.uniqueId ===  this.$scope.currentComponent.selectedInstance.componentUid;
                    });
                    if(componentParent) {
                        this.$scope.selectedComponent = componentParent;
                    }
                    else {
                        try {
                            let onSuccess = (component:Models.Components.Component) => {
                                this.$scope.isLoadingRightPanel = false;
                                this.$scope.selectedComponent = component;
                                this.cacheComponentsInstancesFullData.push(component);
                            };
                            let onError = (component:Models.Components.Component) => {
                                console.log("Error updating selected component");
                                this.$scope.isLoadingRightPanel = false;
                            };
                            this.ComponentFactory.getComponentFromServer(this.$scope.currentComponent.selectedInstance.originType, this.$scope.currentComponent.selectedInstance.componentUid).then(onSuccess, onError);
                        } catch(e){
                            console.log("Error updating selected component", e);
                            this.$scope.isLoadingRightPanel = false;
                        }
                    }
                }
                else {
                    this.$scope.selectedComponent = this.$scope.currentComponent;
                }
            };

            this.$scope.setSelectedInstance = (selectedComponent:Models.ComponentsInstances.ComponentInstance):void => {

                this.$log.debug('composition-view-model::onNodeSelected:: with id: '+ selectedComponent.uniqueId);
                this.$scope.currentComponent.setSelectedInstance(selectedComponent);
                this.$scope.updateSelectedComponent();

                if (this.$state.current.name === 'workspace.composition.api') {
                    this.$state.go('workspace.composition.details');
                }
                if (this.$state.current.name === 'workspace.composition.relations' && this.$scope.currentComponent.isProduct()) {
                    this.$state.go('workspace.composition.details');
                }
            };

            this.$scope.onBackgroundClick = ():void => {
                this.$scope.currentComponent.selectedInstance = null;
                this.$scope.selectedComponent = this.$scope.currentComponent;

                if (this.$state.current.name === 'workspace.composition.api') {
                    this.$state.go('workspace.composition.details');
                }
            };

            this.$scope.openUpdateModal = ():void => {
                this.openUpdateComponentInstanceNameModal();
            };

            this.$scope.deleteSelectedComponentInstance = ():void => {
                let state = "deleteInstance";
                let onOk = ():void => {
                    this.removeSelectedComponentInstance();
                    //this.$scope.graphApi.deleteSelectedNodes();
                };
                let title:string =  this.$scope.sdcMenu.alertMessages[state].title;
                let message:string =  this.$scope.sdcMenu.alertMessages[state].message.format([this.$scope.currentComponent.selectedInstance.name]);
                this.ModalsHandler.openAlertModal(title, message).then(onOk);
            };

            this.$scope.setComponent = (component: Models.Components.Product):void => {
                this.$scope.currentComponent = component;
            }

        }
    }
}
