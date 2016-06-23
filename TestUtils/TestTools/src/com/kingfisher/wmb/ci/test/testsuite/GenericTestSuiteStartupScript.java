package com.kingfisher.wmb.ci.test.testsuite;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;

public class GenericTestSuiteStartupScript {


	public static void setupTestSuite(Logger log, TestSuiteRunner runner, TestSuiteRunContext context, TestSuite testSuite ) throws Exception
	{
		log.info("Setup of Test Suite ("+testSuite.getName()+") Started");

		List<WsdlMockService> wsdlMockServiceList = testSuite.getProject().getMockServiceList();
		List<RestMockService> restMockServiceList = testSuite.getProject().getRestMockServiceList();

		List<MockRunner> mockRunnerList = new ArrayList<MockRunner>();
		String mockPort = testSuite.getProject().getPropertyValue("MOCK_SAP_PORT");
		if(mockPort == null || mockPort.equals(""))
		{
			String globalMockPort = com.eviware.soapui.SoapUI.getGlobalProperties().getPropertyValue( "GLOBAL_MOCK_SAP_PORT" );
			mockPort = globalMockPort;
		}

		for(int i=0;i<wsdlMockServiceList.size();i++)
		{
			MockService mockService = wsdlMockServiceList.get(i);
			mockService.setPort(Integer.parseInt(mockPort));
			log.info("Starting SOAP Mock Service -> "+mockService.getName()+" on port: "+mockService.getPort());
			mockRunnerList.add(mockService.start());
		}

		for(int i=0;i<restMockServiceList.size();i++)
		{
			MockService mockService = restMockServiceList.get(i);
			mockService.setPort(Integer.parseInt(mockPort));
			log.info("Starting REST Mock Service -> "+mockService.getName()+" on port: "+mockService.getPort());
			mockRunnerList.add(mockService.start());
		}		
		context.setProperty("MockRunnerList",mockRunnerList);
		log.info("Setup of Test Suite ("+testSuite.getName()+") Complete");		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
