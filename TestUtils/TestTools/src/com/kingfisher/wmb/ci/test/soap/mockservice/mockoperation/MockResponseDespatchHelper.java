package com.kingfisher.wmb.ci.test.soap.mockservice.mockoperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.DetailedDiff;
import org.xml.sax.SAXException;

import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockRunContext;
import com.kingfisher.wmb.ci.test.xml.util.XmlCompare;

public class MockResponseDespatchHelper 
{
	public static String getMockResponse(Logger log, MockRunContext mockRunContext, MockRequest mockRequest, MockOperation mockOperation) throws IOException, SAXException
	{
		String receivedMessage = mockRequest.getRequestContent();
		Operation serviceOperation;
		
		System.out.println("["+mockOperation.getMockService().getName()+"] Received Message ---> "+receivedMessage);

		serviceOperation = mockOperation.getOperation();
		
		log.debug("Operation: "+serviceOperation);

		List<Request> expectedRequestsList = serviceOperation.getRequestList();

		for(int i=0;i<expectedRequestsList.size();i++)
		{
			Request request = expectedRequestsList.get(i);
			//def requestContent = request.getRequestContent()
			String mockRequestContent = mockRunContext.expand(request.getRequestContent());
			System.out.println(request.getName()+": "+mockRequestContent);
			
			//Build exludedList
			List<String> excludedList = new ArrayList<String>();
			String excludedListStr = mockOperation.getMockService().getPropertyValue("excludedListStr");
			System.out.println("***********excludedListStr------>"+excludedListStr);
			if(excludedListStr!= null && !excludedListStr.equals(""))
			{
				excludedList = Arrays.asList(excludedListStr.split("\\s*,\\s*"));
			}
			
			XmlCompare.setLog(log);
			DetailedDiff detailedDiff = XmlCompare.compareXml(mockRequestContent, receivedMessage, excludedList);
			System.out.println(detailedDiff.getAllDifferences());
			if(detailedDiff.identical()||detailedDiff.similar())
			{
				System.out.println("Match found ---> "+request.getName());
				return request.getName();
			}
		}
		System.out.println("No match found. Responding with default response");
		
		return "Default";
	}
}
