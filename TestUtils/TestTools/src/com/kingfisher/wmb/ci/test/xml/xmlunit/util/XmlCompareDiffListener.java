package com.kingfisher.wmb.ci.test.xml.xmlunit.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

public class XmlCompareDiffListener implements DifferenceListener {

	List<String> consideredList = new ArrayList<String>();
	List<Difference> diffList = null;
	List<Difference> acceptedDiffList = null;
	
	public List<String> getConsideredList() {
		return consideredList;
	}

	public void setConsideredList(List<String> consideredList) {
		this.consideredList = consideredList;
	}

	public List<Difference> getDiffList() {
		return diffList;
	}

	public void setDiffList(List<Difference> diffList) {
		this.diffList = diffList;
	}

	public List<Difference> getAcceptedDiffList() {
		return acceptedDiffList;
	}

	public void setAcceptedDiffList(List<Difference> acceptedDiffList) {
		this.acceptedDiffList = acceptedDiffList;
	}

	public Logger getLog() {
		return log;
	}

	public void setLog(Logger log) {
		this.log = log;
	}

	Logger log = null;
	
	public XmlCompareDiffListener()
	{
		
	}
	
	public XmlCompareDiffListener(List<String> excludedList, Logger log)
	{
		consideredList.addAll(excludedList);
		System.out.println("Skipped elements ----> "+consideredList);
		this.log = log;		
	}
	
		
	@Override
	public int differenceFound(Difference diff) 
	{
		//System.out.println(diff);
		if (diff.getId() == DifferenceConstants.TEXT_VALUE_ID)
		{
			if(consideredList!=null && !consideredList.contains(diff.getControlNodeDetail().getNode().getParentNode().getNodeName()))
	 		{
				System.out.println("Difference Found -> "+diff.getControlNodeDetail().getNode().getParentNode().getNodeName()+": Expected ("+diff.getTestNodeDetail().getValue()+"), Actual ("+diff.getControlNodeDetail().getValue()+") --- REJECTED");	 			
				//diffList.add(diff);
	 			return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
	 		}
			else
			{
				System.out.println("Difference Found -> "+diff.getControlNodeDetail().getNode().getParentNode().getNodeName()+": Expected ("+diff.getTestNodeDetail().getValue()+"), Actual ("+diff.getControlNodeDetail().getValue()+") --- ACCEPTED");
				//acceptedDiffList.add(diff);
				//System.out.println(diff.getControlNodeDetail().getNode().getParentNode().getNodeName()+": Expected ("+diff.getTestNodeDetail().getValue()+"), Actual ("+diff.getControlNodeDetail().getValue()+") --- ACCEPTED");
				return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
			}
		}
		else
		if(diff.getId() == DifferenceConstants.CHILD_NODELIST_SEQUENCE_ID)
		{
			System.out.println("Seq Difference Found: ("+diff+") --- ACCEPTED");
			return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
		}
		return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
	}

	@Override
	public void skippedComparison(Node arg0, Node arg1) 
	{
	}

}
