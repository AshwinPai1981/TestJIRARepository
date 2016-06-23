package com.kingfisher.wmb.ci.test.assertion;

import java.io.IOException;

import net.sf.json.JSON;

import org.apache.log4j.Logger;

import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.support.JsonUtil;
import com.kingfisher.wmb.ci.test.json.util.JsonCompare;

public class GenericScriptAssertionHelper {

	public static boolean assertResponseJson(Logger log, TestCaseRunContext context, MessageExchange messageExchange, String expectedJsonResponse) throws IOException
	{
		JSON actualJSONObj = new JsonUtil().parseTrimmedText(messageExchange.getResponseContent());
		
		JsonCompare.setLog(log);
		return JsonCompare.compareJson(expectedJsonResponse,actualJSONObj.toString());
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
