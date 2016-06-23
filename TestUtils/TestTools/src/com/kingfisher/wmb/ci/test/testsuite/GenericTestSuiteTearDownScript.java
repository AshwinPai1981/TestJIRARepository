package com.kingfisher.wmb.ci.test.testsuite;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuiteRunContext;
import com.eviware.soapui.model.testsuite.TestSuiteRunner;

public class GenericTestSuiteTearDownScript {

	public static void tearDownTestSuite(Logger log, TestSuiteRunner runner, TestSuiteRunContext context, TestSuite testSuite ) throws Exception
	{
		log.info("Tear Down of Test Suite ("+testSuite.getName()+") Started");

		List<MockRunner> mockRunnerList = (List<MockRunner>)context.getProperty("MockRunnerList");

		for(int i=0;i<mockRunnerList.size();i++)
		{
			MockService service = mockRunnerList.get(i).getMockContext().getMockService();
			log.info("Stopping Mock Service -> "+service.getName()+"("+service.getPort()+")");
			mockRunnerList.get(i).stop();
		}
		log.info("Tear Down Test Suite ("+testSuite.getName()+") Complete");
	}
	
	public static void main(String[] args) 
	{
		//TODO Auto-generated method stub

	}

}
