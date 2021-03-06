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
/// <reference path="../../references"/>
module Sdc.Models {
    'use strict';


    export class RelationMenuDirectiveObj {

        fromNode:Models.Graph.CompositionCiNodeBase;
        toNode:Models.Graph.CompositionCiNodeBase;
        //  modelLinks:Array<Models.CompositionCiLinkBase>;
        mp2mpVL:Models.Components.Component;
        p2pVL:Models.Components.Component;
        menuPosition: Cy.Position;
        rightSideLink:GraphLinkMenuSide;
        leftSideLink:GraphLinkMenuSide;
        selectionText:string;
        vlType:string;

        constructor(fromNode:Models.Graph.CompositionCiNodeBase, toNode:Models.Graph.CompositionCiNodeBase, mp2mpVL:Models.Components.Component, p2pVL:Models.Components.Component, menuPosition:Cy.Position, possibleRelations:Array<Models.MatchBase>) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            // this.modelLinks = modelLinks;
            this.mp2mpVL = mp2mpVL;
            this.p2pVL = p2pVL;
            this.menuPosition = menuPosition;
            this.leftSideLink = new GraphLinkMenuSide(this.fromNode.componentInstance);
            this.rightSideLink = new GraphLinkMenuSide(this.toNode.componentInstance);
            this.selectionText = '';
            this.vlType = null;

            possibleRelations.forEach((match:any) => {

                let reqObjKey: string = match.requirement.ownerName + match.requirement.uniqueId;
                let capObjKey: string = match.secondRequirement ? match.secondRequirement.ownerName + match.secondRequirement.uniqueId
                    : match.capability.ownerName + match.capability.uniqueId;

                if (match.fromNode === this.leftSideLink.componentInstance.uniqueId) {
                    //init the left side requirements Array
                    if (!this.leftSideLink.requirements[reqObjKey]) {
                        this.leftSideLink.requirements[reqObjKey] = [];
                    }
                    //push the match to fromNode object (from node is always the requirement)
                    this.leftSideLink.requirements[reqObjKey].push(match);

                    if (match instanceof  Models.MatchReqToReq) {
                        //init the right side requirements Array
                        if (!this.rightSideLink.requirements[capObjKey]) {
                            this.rightSideLink.requirements[capObjKey] = [];
                        }
                        this.rightSideLink.requirements[capObjKey].push(match);
                    } else {
                        //init the right side capabilities Array
                        if (!this.rightSideLink.capabilities[capObjKey]) {
                            this.rightSideLink.capabilities[capObjKey] = [];
                        }
                        //add to array
                        this.rightSideLink.capabilities[capObjKey].push(match);
                    }

                } else {
                    if (!this.rightSideLink.requirements[reqObjKey]) {
                        this.rightSideLink.requirements[reqObjKey] = [];
                    }
                    this.rightSideLink.requirements[reqObjKey].push(match);

                    if (!this.leftSideLink.capabilities[capObjKey]) {
                        this.leftSideLink.capabilities[capObjKey] = [];
                    }
                    this.leftSideLink.capabilities[capObjKey].push(match);
                }
            });

        }
    }


    export class GraphLinkMenuSide {
        public componentInstance:Models.ComponentsInstances.ComponentInstance;
        public selectedMatch:Array<any>;        //match array returned by function in utils
        public requirements:any;  //array of matches returned by function in utils
        public capabilities:any;  //array of matches returned by function in utils

        constructor(componentInstance:Models.ComponentsInstances.ComponentInstance) {
            this.componentInstance = componentInstance;
            this.capabilities = {};
            this.requirements = {};
        }

        public selectMatchArr(matchArr:Array<Models.MatchBase>):void {
            if (this.selectedMatch === matchArr) {
                this.selectedMatch = undefined;
            } else {
                this.selectedMatch = matchArr;
            }
        }


        //TODO move to match object
        public getPreviewText(showReq:boolean):string {
            if (!this.selectedMatch) {
                return '';
            }

            let match:any = this.selectedMatch[0];
            if (showReq) {
                return match.requirement.ownerName + ': ' + match.requirement.name +
                    ': [' + match.requirement.minOccurrences + ', ' + match.requirement.maxOccurrences + ']';
            } else if (match.secondRequirement) {
                return match.secondRequirement.ownerName + ': ' + match.secondRequirement.name +
                    ': [' + match.secondRequirement.minOccurrences + ', ' + match.secondRequirement.maxOccurrences + ']';
            }
            else {
                return match.capability.ownerName + ': ' + match.capability.name +
                    ': [' + match.capability.minOccurrences + ', ' + match.capability.maxOccurrences + ']';
            }
        }
    }

}
