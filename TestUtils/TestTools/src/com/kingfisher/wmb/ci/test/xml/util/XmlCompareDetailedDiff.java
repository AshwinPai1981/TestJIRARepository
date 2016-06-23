package com.kingfisher.wmb.ci.test.xml.util;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;

public class XmlCompareDetailedDiff extends DetailedDiff 
{
	public XmlCompareDetailedDiff(Diff diff)
	{
		super(diff);
	}
}
