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

    export interface ITypeMapScope extends ng.IScope {
        parentFormObj:ng.IFormController;
        schemaProperty:Models.SchemaProperty;
        isSchemaTypeDataType:boolean;
        valueObjRef:any;
        mapKeys:Array<string>;//array of map keys
        propertyNameValidationPattern: RegExp;
        fieldsPrefixName:string;
        readOnly:boolean;
        mapDefaultValue:any;
        types:Models.DataTypesMap;
        maxLength:number;

        getValidationPattern(type:string):RegExp;
        validateIntRange(value:string):boolean;
        changeKeyOfMap(newKey:string, index:number, fieldName:string):void;
        deleteMapItem(index:number):void;
        addMapItemFields():void;
        parseToCorrectType(objectOfValues:any, locationInObj:string, type:string):void;
        getNumber(num:number):Array<any>;
    }


    export class TypeMapDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService,
                    private DataTypesService:Sdc.Services.DataTypesService,
                    private PropertyNameValidationPattern: RegExp,
                    private ValidationUtils:Sdc.Utils.ValidationUtils,
                    private $timeout: ng.ITimeoutService) {
        }

        scope = {
            valueObjRef: '=',//ref to map object in the parent value object
            schemaProperty: '=',//get the schema.property object
            parentFormObj: '=',//ref to parent form (get angular form object)
            fieldsPrefixName: '=',//prefix for form fields names
            readOnly: '=',//is form read only
            defaultValue: '@',//this map default value
            types: '=',//data types list
            maxLength: '='
        };

        restrict = 'E';
        replace = true;
        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/property-types/type-map/type-map-directive.html');
        };

        link = (scope:ITypeMapScope, element:any, $attr:any) => {
            scope.propertyNameValidationPattern = this.PropertyNameValidationPattern;

            //reset valueObjRef and mapKeys when schema type is changed
            scope.$watchCollection('schemaProperty.type', (newData:any):void => {
                scope.isSchemaTypeDataType = this.DataTypesService.isDataTypeForSchemaType(scope.schemaProperty,scope.types);
                if(scope.valueObjRef){
                    scope.mapKeys = Object.keys(scope.valueObjRef);
                }
            });

            //when user brows between properties in "edit property form"
            scope.$watchCollection('fieldsPrefixName', (newData:any):void => {
                if(!scope.valueObjRef) {
                    scope.valueObjRef={};
                }
                scope.mapKeys = Object.keys(scope.valueObjRef);

                if($attr.defaultValue){
                    scope.mapDefaultValue = JSON.parse($attr.defaultValue);
                }
            });

            //return dummy array in order to prevent rendering map-keys ng-repeat again when a map key is changed
            scope.getNumber = (num:number):Array<any> => {
                return new Array(num);
            };

            scope.getValidationPattern = (type:string):RegExp => {
                return this.ValidationUtils.getValidationPattern(type);
            };

            scope.validateIntRange = (value:string):boolean => {
                return !value || this.ValidationUtils.validateIntRange(value);
            };

            scope.changeKeyOfMap = (newKey:string, index:number, fieldName:string) : void => {
                let oldKey = Object.keys(scope.valueObjRef)[index];
                if(Object.keys(scope.valueObjRef).indexOf(newKey)>-1){
                    scope.parentFormObj[fieldName].$setValidity('keyExist', false);
                }else{
                    scope.parentFormObj[fieldName].$setValidity('keyExist', true);
                    if(!scope.parentFormObj[fieldName].$invalid){
                        angular.copy(JSON.parse(JSON.stringify(scope.valueObjRef).replace('"'+oldKey+'":', '"'+newKey+'":')),scope.valueObjRef);//update key
                    }
                }
            };

            scope.deleteMapItem=(index:number):void=>{
                delete scope.valueObjRef[scope.mapKeys[index]];
                scope.mapKeys.splice(index,1);
                if (!scope.mapKeys.length) {//only when user removes all pairs of key-value fields - put the default
                    if ( scope.mapDefaultValue ) {
                        angular.copy(scope.mapDefaultValue, scope.valueObjRef);
                        scope.mapKeys = Object.keys(scope.valueObjRef);
                    }
                }
            };

            scope.addMapItemFields = ():void => {
                scope.valueObjRef['']= null;
                scope.mapKeys = Object.keys(scope.valueObjRef);
            };

            scope.parseToCorrectType = (objectOfValues:any, locationInObj:string, type:string):void => {
                if(objectOfValues[locationInObj] && type != Utils.Constants.PROPERTY_TYPES.STRING){
                    objectOfValues[locationInObj] = JSON.parse(objectOfValues[locationInObj]);
                }
            }
        };

        public static factory = ($templateCache:ng.ITemplateCacheService,
                                 DataTypesService:Sdc.Services.DataTypesService,
                                 PropertyNameValidationPattern:RegExp,
                                 ValidationUtils:Sdc.Utils.ValidationUtils,
                                 $timeout: ng.ITimeoutService)=> {
            return new TypeMapDirective($templateCache,DataTypesService,PropertyNameValidationPattern,ValidationUtils,$timeout);
        };
    }

    TypeMapDirective.factory.$inject = ['$templateCache','Sdc.Services.DataTypesService','PropertyNameValidationPattern','ValidationUtils','$timeout'];
}
