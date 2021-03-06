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
 * Created by rcohen on 9/15/2016.
 */
/// <reference path="../../../references"/>
module Sdc.Directives {
    'use strict';
   /// import Model = go.Model;

    export interface ITypeListScope extends ng.IScope {
        parentFormObj:ng.IFormController;
        schemaProperty:Models.SchemaProperty;
        isSchemaTypeDataType:boolean;
        valueObjRef:any;
        propertyNameValidationPattern: RegExp;
        fieldsPrefixName:string;
        readOnly:boolean;
        listDefaultValue:any;
        types:Models.DataTypesMap;
        listNewItem:any;
        maxLength:number;

        getValidationPattern(type:string):RegExp;
        validateIntRange(value:string):boolean;
        addListItem():void;
        deleteListItem(listItemIndex:number):void
    }


    export class TypeListDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService,
                    private DataTypesService:Sdc.Services.DataTypesService,
                    private PropertyNameValidationPattern: RegExp,
                    private ValidationUtils:Sdc.Utils.ValidationUtils) {
        }

        scope = {
            valueObjRef: '=',//ref to list object in the parent value object
            schemaProperty: '=',//get the schema.property object
            parentFormObj: '=',//ref to parent form (get angular form object)
            fieldsPrefixName: '=',//prefix for form fields names
            readOnly: '=',//is form read only
            defaultValue: '@',//this list default value
            types: '=',//data types list
            maxLength: '='
        };

        restrict = 'E';
        replace = true;
        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/property-types/type-list/type-list-directive.html');
        };

        link = (scope:ITypeListScope, element:any, $attr:any) => {
            scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;

            //reset valueObjRef when schema type is changed
            scope.$watchCollection('schemaProperty.type', (newData:any):void => {
                scope.isSchemaTypeDataType = this.DataTypesService.isDataTypeForSchemaType(scope.schemaProperty,scope.types);
                //insert 1 empty item dt by default
                if(scope.isSchemaTypeDataType && (!scope.valueObjRef||!scope.valueObjRef.length)){
                    scope.valueObjRef = scope.valueObjRef ||[];
                    scope.valueObjRef.push({});
                }
            });

            //when user brows between properties in "edit property form"
            scope.$watchCollection('fieldsPrefixName', (newData:any):void => {
                scope.listNewItem={value:''};

                if($attr.defaultValue){
                    scope.listDefaultValue = JSON.parse($attr.defaultValue);
                }
            });

            scope.getValidationPattern = (type:string):RegExp => {
                return this.ValidationUtils.getValidationPattern(type);
            };

            scope.validateIntRange = (value:string):boolean => {
                return !value || this.ValidationUtils.validateIntRange(value);
            };

            scope.addListItem = ():void => {
                scope.valueObjRef = scope.valueObjRef ||[];
                let newVal = ((scope.schemaProperty.simpleType||scope.schemaProperty.type)==Utils.Constants.PROPERTY_TYPES.STRING?scope.listNewItem.value:JSON.parse(scope.listNewItem.value));
                scope.valueObjRef.push(newVal);
                scope.listNewItem.value = "";
            };

            scope.deleteListItem = (listItemIndex:number):void => {
                scope.valueObjRef.splice(listItemIndex,1);
                if (!scope.valueObjRef.length) {
                    if (scope.listDefaultValue ) {
                        angular.copy(scope.listDefaultValue, scope.valueObjRef);
                    }
                }
            };
        };

        public static factory = ($templateCache:ng.ITemplateCacheService,
                                 DataTypesService:Sdc.Services.DataTypesService,
                                 PropertyNameValidationPattern:RegExp,
                                 ValidationUtils:Sdc.Utils.ValidationUtils)=> {
            return new TypeListDirective($templateCache,DataTypesService,PropertyNameValidationPattern,ValidationUtils);
        };
    }

    TypeListDirective.factory.$inject = ['$templateCache','Sdc.Services.DataTypesService','PropertyNameValidationPattern','ValidationUtils'];
}

