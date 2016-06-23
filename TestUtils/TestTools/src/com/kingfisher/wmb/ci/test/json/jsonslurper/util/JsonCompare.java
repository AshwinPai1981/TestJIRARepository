package com.kingfisher.wmb.ci.test.json.jsonslurper.util;

import groovy.json.JsonSlurper;

import java.io.IOException;

import org.apache.log4j.Logger;

public class JsonCompare 
{
	static Logger log = null;
	
	public static Logger getLog() {
		return log;
	}

	public static void setLog(Logger log) {
		JsonCompare.log = log;
	}
	
	public static boolean compareJson(String controlMessage, String testMessage)throws IOException
	{
		System.out.println("Expected:"+controlMessage);
		System.out.println("Actual: "+testMessage);
		
		Object controlMessageObj = new JsonSlurper().parseText(controlMessage);
		Object testMessageObj = new JsonSlurper().parseText(testMessage);

/*		HashMap<Object, Object> controlMessageObjClone = (HashMap<Object, Object>)controlMessageObj.clone();
		HashMap<Object, Object> testMessageObjClone = (HashMap<Object, Object>)testMessageObj.clone();
		
		Set<Object> removedKeys = controlMessageObjClone.keySet();
		//removedKeys.addAll(testMessageObjClone.entrySet());
		removedKeys.removeAll(testMessageObjClone.keySet());

		Set<Object> addedKeys = testMessageObjClone.keySet();
		//removedKeys.addAll(testMessageObjClone.entrySet());
		removedKeys.removeAll(controlMessageObjClone.keySet());
		
		Set<Entry<Object, Object>> changedEntries = controlMessageObjClone.entrySet();
		changedEntries.removeAll(testMessageObjClone.entrySet());*/
		
		
		return controlMessageObj.equals(testMessageObj);
		
		//System.out.println("added " + addedKeys);
		//System.out.println("removed " + removedKeys);
		//System.out.println("changed " + changedEntries);
		
		//return 	expectedJsonObj==actualJSONObj;		
	}
	
	public static void main(String args[]) throws IOException
	{
		String controlMessage="{    \"Class\": [\"Aisle\"],    \"properties\":    {       \"storageLocation\": \"115\",       \"aisleNumber\": \"A709\",       \"storageType\": \"IM\",       \"description\": \"Selling Space Aisle\"    },    \"entities\": [   {       \"Class\": [\"FulfilmentSite\"],       \"rel\": [\"urn:x-kingfisher:name:parentStore\"],       \"properties\": {\"siteNumber\": \"1115\"},       \"links\": [      {          \"rel\": [\"self\"],          \"href\": \"http://unxs0384.ghanp.kfplc.com:8070/FulfilmentSite/1115\"       }]    }] }";
		String testMessage="{    \"Class\": [\"Aisle\"],    \"properties\":    {       \"storageLocation\": \"115\",       \"aisleNumber\": \"A708\",       \"storageType\": \"IM\",       \"description\": \"Selling Space Aisle\"    },    \"entities\": [   {       \"Class\": [\"FulfilmentSite\"],       \"rel\": [\"urn:x-kingfisher:name:parentStore\"],       \"properties\": {\"siteNumber\": \"1115\"},       \"links\": [      {          \"rel\": [\"self\"],          \"href\": \"http://unxs0384.ghanp.kfplc.com:8070/FulfilmentSite/1115\"       }]    }] }";
		System.out.println(JsonCompare.compareJson(controlMessage, testMessage));
	}
}
