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
/**
 * Created by obarda on 2/11/2016.
 */
/// <reference path="../references"/>
module Sdc {
    let moduleName:string = 'Sdc.Utils';
    let serviceModule:ng.IModule = angular.module(moduleName, []);



    //Utils
    serviceModule.service('ComponentFactory', Sdc.Utils.ComponentFactory);
    serviceModule.service('ComponentInstanceFactory', Sdc.Utils.ComponentInstanceFactory);
    serviceModule.service('ChangeLifecycleStateHandler', Sdc.Utils.ChangeLifecycleStateHandler);
    serviceModule.service('ModalsHandler', Sdc.Utils.ModalsHandler);
    serviceModule.service('MenuHandler', Sdc.Utils.MenuHandler);

 

}
