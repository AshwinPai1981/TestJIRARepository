package com.kingfisher.wmb.ci.test.xml.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

public class XmlCompare
{
	private static Logger log;
	
	public static Logger getLog() {
		return log;
	}

	public static void setLog(Logger log) {
		XmlCompare.log = log;
	}

	public static DetailedDiff compareXml(String controlMessage, String testMessage, List<String> excludedList)throws IOException, SAXException
	{
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
		XMLUnit.setIgnoreWhitespace(true);
		try
		{
			Diff xmlDiff = XMLUnit.compareXML(controlMessage, testMessage);
			//if (excludedList!=null && excludedList.size()>0)
			//{
				xmlDiff.overrideDifferenceListener(new XmlCompareDiffListener(excludedList,log));
			//}
			//xmlDiff.overrideElementQualifier(new MultiLevelElementNameAndTextQualifier(4,true));
			XmlCompareDetailedDiff detailedDiff = new XmlCompareDetailedDiff(xmlDiff);
			return detailedDiff;
		}
		catch(IOException ioEx)
		{
			throw ioEx;
		}
		catch(SAXException saxEx)
		{
			throw saxEx;
		}
	}
	
	public static void main(String[] args) throws IOException, SAXException
	{
		String receivedMessage ="<Employee><date>2015-06-01</date><name><lastname>pai</lastname><firstname>Ashwin</firstname></name><id>123</id></Employee>";
		String mockRequestMessage ="<Employee><name><firstname>Ashwin1</firstname><lastname>pai</lastname></name><id>123</id><date>2016-06-01</date></Employee>";
		List<String> excludedList = new ArrayList<String>();
		excludedList.add("date");
		//excludedList.add("firstname");
		DetailedDiff detailedDiff = XmlCompare.compareXml(mockRequestMessage, receivedMessage, excludedList);
		System.out.println(detailedDiff.getAllDifferences());
		
	}
}
