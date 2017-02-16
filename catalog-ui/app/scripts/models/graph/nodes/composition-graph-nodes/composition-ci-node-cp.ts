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

module Sdc.Models.Graph {

    export class CompositionCiNodeCp extends CompositionCiNodeBase {

        constructor(instance:Models.ComponentsInstances.ComponentInstance,
                    imageCreator: Utils.ImageCreatorService) {
            super(instance, imageCreator);
            this.initCp();
        }

        private initCp():void {
            let sdcConfig = Services.AngularJSBridge.getAngularConfig();
            this.img = sdcConfig.imagesPath + Utils.Constants.ImagesUrl.RESOURCE_ICONS + this.componentInstance.icon + '.png';
            this.type = "basic-small-node";
            //if the cp from type cpEndPointInstances create with another template
            if(sdcConfig.cpEndPointInstances.indexOf(this.componentInstance.icon) > -1){
                this.classes = 'cp-end-point-node';
            }else {
                this.classes = 'cp-node';
            }
            if(!this.certified) {
                this.classes = this.classes + ' not-certified';
            }

        }
    }
}
