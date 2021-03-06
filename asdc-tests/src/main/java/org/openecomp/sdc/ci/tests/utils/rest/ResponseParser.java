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

package org.openecomp.sdc.ci.tests.utils.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation.PropertyConstraintJacksonDeserialiser;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceRespJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ResponseParser {
	
	private static final String INVARIANT_UUID = "invariantUUID";
	public static final String UNIQUE_ID = "uniqueId";
	public static final String VERSION = "version";
	public static final String UUID = "uuid";
	public static final String NAME = "name";
	public static final String ORIGIN_TYPE = "originType";
	public static final String TOSCA_RESOURCE_NAME = "toscaResourceName";

	static Logger logger = Logger.getLogger(ResponseParser.class.getName());

	public static String getValueFromJsonResponse(String response, String fieldName) {
		try {
			JSONObject jsonResp = (JSONObject) JSONValue.parse(response);
			Object fieldValue = jsonResp.get(fieldName);
			return fieldValue.toString();

		} catch (Exception e) {
			return null;
		}

	}

	public static String getUniqueIdFromResponse(RestResponse response) {
		return getValueFromJsonResponse(response.getResponse(), UNIQUE_ID);
	}

	public static String getInvariantUuid(RestResponse response) {
		return getValueFromJsonResponse(response.getResponse(), INVARIANT_UUID);
	}

	public static String getUuidFromResponse(RestResponse response) {
		return getValueFromJsonResponse(response.getResponse(), UUID);
	}

	public static String getNameFromResponse(RestResponse response) {
		return getValueFromJsonResponse(response.getResponse(), NAME);
	}

	public static String getVersionFromResponse(RestResponse response) {
		return ResponseParser.getValueFromJsonResponse(response.getResponse(), VERSION);
	}

	public static String getComponentTypeFromResponse(RestResponse response) {
		return ResponseParser.getValueFromJsonResponse(response.getResponse(), ORIGIN_TYPE);
	}

	public static String getToscaResourceNameFromResponse(RestResponse response) {
		return getValueFromJsonResponse(response.getResponse(), TOSCA_RESOURCE_NAME);
	}

	@SuppressWarnings("unchecked")
	public static ResourceRespJavaObject parseJsonListReturnResourceDetailsObj(RestResponse restResponse,
			String resourceType, String searchPattern, String expectedResult) throws Exception {

		// Gson gson = new Gson;

		JsonElement jElement = new JsonParser().parse(restResponse.getResponse());
		JsonObject jObject = jElement.getAsJsonObject();
		JsonArray arrayOfObjects = (JsonArray) jObject.get(resourceType);
		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();
		ResourceRespJavaObject jsonToJavaObject = new ResourceRespJavaObject();

		for (int counter = 0; counter < arrayOfObjects.size(); counter++) {
			JsonObject jHitObject = (JsonObject) arrayOfObjects.get(counter);

			map = (Map<String, Object>) gson.fromJson(jHitObject.toString(), map.getClass());
			if (map.get(searchPattern).toString().contains(expectedResult)) {

				jsonToJavaObject = gson.fromJson(jObject, ResourceRespJavaObject.class);
				break;
			}
		}
		return jsonToJavaObject;

	}

	public static Resource convertResourceResponseToJavaObject(String response) {

		ObjectMapper mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule("customerSerializationModule",
				new Version(1, 0, 0, "static version"));
		JsonDeserializer<PropertyConstraint> desrializer = new PropertyConstraintJacksonDeserialiser();
		addDeserializer(module, PropertyConstraint.class, desrializer);

		mapper.registerModule(module);
		Resource resource = null;
		try {
			resource = mapper.readValue(response, Resource.class);
			logger.debug(resource.toString());
		} catch (IOException e) {
			try {
				List<Resource> resources = Arrays.asList(mapper.readValue(response.toString(), Resource[].class));
				resource = resources.get(0);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return resource;
	}

	public static ComponentInstanceProperty convertPropertyResponseToJavaObject(String response) {

		ObjectMapper mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule("customerSerializationModule",
				new Version(1, 0, 0, "static version"));
		JsonDeserializer<PropertyConstraint> desrializer = new PropertyConstraintJacksonDeserialiser();
		addDeserializer(module, PropertyConstraint.class, desrializer);

		mapper.registerModule(module);
		ComponentInstanceProperty propertyDefinition = null;
		try {
			propertyDefinition = mapper.readValue(response, ComponentInstanceProperty.class);
			logger.debug(propertyDefinition.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return propertyDefinition;
	}

	// public static ResourceInstanceReqDetails
	// convertResourceInstanceResponseToJavaObject(String response) {
	//
	// ObjectMapper mapper = new ObjectMapper();
	// final SimpleModule module = new
	// SimpleModule("customerSerializationModule", new Version(1, 0, 0, "static
	// version"));
	// JsonDeserializer<PropertyConstraint> desrializer = new
	// PropertyConstraintJacksonDeserialiser();
	// addDeserializer(module, PropertyConstraint.class, desrializer);
	//
	// mapper.registerModule(module);
	// ResourceInstanceReqDetails resourceInstanceReqDetails = null;
	// try {
	// resourceInstanceReqDetails = mapper.readValue(response,
	// ResourceInstanceReqDetails.class);
	// logger.debug(resourceInstanceReqDetails.toString());
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// return resourceInstanceReqDetails;
	// }
	public static String toJson(Object object) {
		Gson gson = new Gson();
		return gson.toJson(object);
	}

	public static ArtifactDefinition convertArtifactDefinitionResponseToJavaObject(String response) {
		ObjectMapper mapper = new ObjectMapper();
		ArtifactDefinition artifactDefinition = null;
		try {

			artifactDefinition = mapper.readValue(response, ArtifactDefinition.class);
			logger.debug(artifactDefinition.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return artifactDefinition;

	}

	public static ArtifactReqDetails convertArtifactReqDetailsToJavaObject(String response) {

		ArtifactReqDetails artifactReqDetails = null;
		// try {
		//
		// artifactDefinition = mapper.readValue(response,
		// ArtifactReqDetails.class);
		// logger.debug(artifactDefinition.toString());
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// return artifactDefinition;
		Gson gson = new Gson();
		artifactReqDetails = gson.fromJson(response, ArtifactReqDetails.class);
		return artifactReqDetails;
	}

	public static <T> T parseToObject(String json, Class<T> clazz) {
		Gson gson = new Gson();
		T object;
		try {
			object = gson.fromJson(json, clazz);
		} catch (Exception e) {
			object = parseToObjectUsingMapper(json, clazz);
		}
		return object;
	}

	public static <T> T parseToObjectUsingMapper(String json, Class<T> clazz) {
		// Generic convert
		ObjectMapper mapper = new ObjectMapper();
		T object = null;
		final SimpleModule module = new SimpleModule("customerSerializationModule",
				new Version(1, 0, 0, "static version"));
		JsonDeserializer<PropertyConstraint> desrializer = new PropertyConstraintJacksonDeserialiser();
		addDeserializer(module, PropertyConstraint.class, desrializer);
		mapper.registerModule(module);
		try {
			object = mapper.readValue(json, clazz);
			// System.out.println("Class: "+clazz.getSimpleName()+", json:
			// "+json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return object;
	}

	public static ArtifactReqDetails convertArtifactDefinitionToArtifactReqDetailsObject(
			ArtifactDefinition artifactDefinition) {

		ArtifactReqDetails artifactReqDetails = null;
		// try {
		//
		// artifactDefinition = mapper.readValue(response,
		// ArtifactReqDetails.class);
		// logger.debug(artifactDefinition.toString());
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// return artifactDefinition;
		Gson gson = new Gson();
		String artDef = gson.toJson(artifactDefinition);
		artifactReqDetails = gson.fromJson(artDef, ArtifactReqDetails.class);
		return artifactReqDetails;
	}

	public static <T> void addDeserializer(SimpleModule module, Class<T> clazz,
			final JsonDeserializer<T> deserializer) {
		module.addDeserializer(clazz, deserializer);
	}

	public static Service convertServiceResponseToJavaObject(String response) {

		ObjectMapper mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule("customerSerializationModule",
				new Version(1, 0, 0, "static version"));
		JsonDeserializer<PropertyConstraint> desrializer = new PropertyConstraintJacksonDeserialiser();
		addDeserializer(module, PropertyConstraint.class, desrializer);

		mapper.registerModule(module);
		Service service = null;
		try {
			service = mapper.readValue(response, Service.class);
			logger.debug(service.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return service;
	}

	public static Product convertProductResponseToJavaObject(String response) {

		ObjectMapper mapper = new ObjectMapper();
		Product product = null;
		try {
			product = mapper.readValue(response, Product.class);
			logger.debug(product.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return product;
	}

	public static ComponentInstance convertComponentInstanceResponseToJavaObject(String response) {

		ObjectMapper mapper = new ObjectMapper();
		final SimpleModule module = new SimpleModule("customerSerializationModule",
				new Version(1, 0, 0, "static version"));
		JsonDeserializer<PropertyConstraint> desrializer = new PropertyConstraintJacksonDeserialiser();
		addDeserializer(module, PropertyConstraint.class, desrializer);

		mapper.registerModule(module);
		ComponentInstance componentInstance = null;
		try {
			componentInstance = mapper.readValue(response, ComponentInstance.class);
			logger.debug(componentInstance.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return componentInstance;
	}

	public static List<String> getValuesFromJsonArray(RestResponse message) throws Exception {
		List<String> artifactTypesArrayFromApi = new ArrayList<String>();

		org.json.JSONObject responseObject = new org.json.JSONObject(message.getResponse());
		JSONArray jArr = responseObject.getJSONArray("artifactTypes");

		for (int i = 0; i < jArr.length(); i++) {
			org.json.JSONObject jObj = jArr.getJSONObject(i);
			String value = jObj.get("name").toString();

			artifactTypesArrayFromApi.add(value);
		}
		return artifactTypesArrayFromApi;
	}

	public static String calculateMD5Header(ArtifactReqDetails artifactDetails) {
		Gson gson = new Gson();
		String jsonBody = gson.toJson(artifactDetails);
		// calculate MD5 for json body
		return calculateMD5(jsonBody);

	}

	public static String calculateMD5(String data) {
		String calculatedMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(data);
		// encode base-64 result
		byte[] encodeBase64 = Base64.encodeBase64(calculatedMd5.getBytes());
		String encodeBase64Str = new String(encodeBase64);
		return encodeBase64Str;

	}

	public static List<Map<String, Object>> getAuditFromMessage(Map auditingMessage) {
		List<Map<String, Object>> auditList = new ArrayList<Map<String, Object>>();
		// JsonElement jElement = new JsonParser().parse(auditingMessage);
		// JsonObject jObject = jElement.getAsJsonObject();
		// JsonObject hitsObject = (JsonObject) jObject.get("hits");
		// JsonArray hitsArray = (JsonArray) hitsObject.get("hits");
		//
		// Iterator<JsonElement> hitsIterator = hitsArray.iterator();
		// while(hitsIterator.hasNext())
		// {
		// JsonElement nextHit = hitsIterator.next();
		// JsonObject jHitObject = nextHit.getAsJsonObject();
		// JsonObject jSourceObject = (JsonObject) jHitObject.get("_source");
		//
		// Gson gson=new Gson();
		// String auditUnparsed = jSourceObject.toString();
		//
		// Map<String,Object> map = new HashMap<String,Object>();
		// map = (Map<String,Object>) gson.fromJson(auditUnparsed,
		// map.getClass());

		auditList.add(auditingMessage);
		// }
		return auditList;
	}

	public static List<CategoryDefinition> parseCategories(RestResponse getAllCategoriesRest) {

		List<CategoryDefinition> categories = new ArrayList<>();
		try {
			JsonElement jElement = new JsonParser().parse(getAllCategoriesRest.getResponse());
			JsonArray cagegories = jElement.getAsJsonArray();
			Iterator<JsonElement> iter = cagegories.iterator();
			while (iter.hasNext()) {
				JsonElement next = iter.next();
				CategoryDefinition category = ResponseParser.parseToObject(next.toString(), CategoryDefinition.class);
				categories.add(category);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return categories;
	}

	public static JSONArray getListFromJson(RestResponse res, String field) throws JSONException {
		String valueFromJsonResponse = getValueFromJsonResponse(res.getResponse(), field);
		JSONArray jArr = new JSONArray(valueFromJsonResponse);

		return jArr;
	}

	public static List<String> getDerivedListFromJson(RestResponse res) throws JSONException {
		JSONArray listFromJson = getListFromJson(res, "derivedList");
		List<String> lst = new ArrayList<String>();
		for (int i = 0; i < listFromJson.length(); i++) {
			lst.add(listFromJson.getString(i));
		}

		return lst;
	}

	public static Map<String, Object> convertStringToMap(String obj) {
		Map<String, Object> object = (Map<String, Object>) JSONValue.parse(obj);
		return object;
	}

	public static List<Map<String, Object>> getListOfMapsFromJson(RestResponse res, String field) throws Exception {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		JSONArray listFromJson = getListFromJson(res, field);
		for (int i = 0; i < listFromJson.length(); i++) {
			Map<String, Object> convertStringToMap = convertStringToMap(listFromJson.getString(i));
			list.add(convertStringToMap);
		}
		return list;

	}

	public static Map<String, Object> getJsonValueAsMap(RestResponse response, String key) {
		String valueField = getValueFromJsonResponse(response.getResponse(), key);
		Map<String, Object> convertToMap = convertStringToMap(valueField);
		return convertToMap;
	}

	public static String getJsonObjectValueByKey(String metadata, String key) {
		JsonElement jelement = new JsonParser().parse(metadata);

		JsonObject jobject = jelement.getAsJsonObject();
		Object obj = jobject.get(key);
		if (obj == null) {
			return null;
		} else {
			return obj.toString();
		}
	}

	public static Map<String, ArrayList<Component>> convertCatalogResponseToJavaObject(String response) {

		Map<String, ArrayList<Component>> map = new HashMap<String, ArrayList<Component>>();

		JsonElement jElement = new JsonParser().parse(response);
		JsonObject jObject = jElement.getAsJsonObject();
		JsonArray jArrReousrces = jObject.getAsJsonArray("resources");
		JsonArray jArrServices = jObject.getAsJsonArray("services");
		JsonArray jArrProducts = jObject.getAsJsonArray("products");

		//////// RESOURCE/////////////////////////////
		ArrayList<Component> restResponseArray = new ArrayList<>();
		Component component = null;
		for (int i = 0; i < jArrReousrces.size(); i++) {
			String resourceString = (String) jArrReousrces.get(i).toString();
			component = ResponseParser.convertResourceResponseToJavaObject(resourceString);
			restResponseArray.add(component);
		}

		map.put("resources", restResponseArray);

		///////// SERVICE/////////////////////////////

		restResponseArray = new ArrayList<>();
		component = null;
		for (int i = 0; i < jArrServices.size(); i++) {
			String resourceString = (String) jArrServices.get(i).toString();
			component = ResponseParser.convertServiceResponseToJavaObject(resourceString);
			restResponseArray.add(component);
		}

		map.put("services", restResponseArray);

		///////// PRODUCT/////////////////////////////
		restResponseArray = new ArrayList<>();
		component = null;
		for (int i = 0; i < jArrProducts.size(); i++) {
			String resourceString = (String) jArrProducts.get(i).toString();
			component = ResponseParser.convertProductResponseToJavaObject(resourceString);
			restResponseArray.add(component);
		}

		map.put("products", restResponseArray);

		return map;

	}
}
