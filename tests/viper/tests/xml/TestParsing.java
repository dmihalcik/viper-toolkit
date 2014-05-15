package viper.tests.xml;

import java.io.IOException;

import javax.xml.parsers.*;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import viper.api.ViperData;

public class TestParsing {
	public static void main(String[] args) throws SAXException, IOException,
			ParserConfigurationException {
		DocumentBuilderFactory docBuilder = DocumentBuilderFactory
				.newInstance();
		docBuilder.setNamespaceAware(true);
		Element docElement = docBuilder.newDocumentBuilder().parse(
				"samples/evaluation/object-eval/all.gtf.xml")
				.getDocumentElement();
		ViperData answer = new viper.api.impl.ViperParser()
				.parseDoc(docElement);
		System.out.println(answer);
	}
}
