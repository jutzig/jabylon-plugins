package org.jabylon.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.URI;
import org.jabylon.properties.PropertiesFactory;
import org.jabylon.properties.Property;
import org.jabylon.properties.PropertyFile;
import org.jabylon.properties.types.PropertyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AndroidConverter implements PropertyConverter{

	private static final String STRING = "string";
	
	private static final String STRING_ARRAY = "string-array";

	private static final String PLURALS = "plurals";

	private static final String ITEM = "item";

	private static final String ROOT_NODE = "resources";

	private static final Logger LOG = LoggerFactory.getLogger(AndroidConverter.class);
	
	private URI uri;
	
	public AndroidConverter(URI resourceLocation) {
		this.uri = resourceLocation;
	}
	
	@Override
	public PropertyFile load(InputStream in, String encoding) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document result = builder.parse(in);
			PropertyFile file = PropertiesFactory.eINSTANCE.createPropertyFile();
			Node resources = result.getDocumentElement();
			System.out.println(resources);
			if(!ROOT_NODE.equals(resources.getNodeName())) {
				LOG.error("XML does not start with "+ROOT_NODE+" but "+resources.getLocalName()+". Location: "+uri);
				return file;
			}
			NodeList nodes = resources.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				loadNode(node,file);
			}
			return file;
			
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			in.close();
		}
		return null;
	}

	private void loadNode(Node node, PropertyFile file) {
		if(node.getNodeType()==Node.TEXT_NODE)
			return;
		Property property = PropertiesFactory.eINSTANCE.createProperty();
		String name = node.getNodeName();
		if(name.equals(STRING)){
			loadString(node, property);
		} else if(name.equals(PLURALS)){
			loadPlurals(node, property);
		} else if(name.equals(STRING_ARRAY)){
			loadArray(node, property);
		}
		else {
			//TODO: log
			return;
		}
		file.getProperties().add(property);
	}

	private void loadString(Node node, Property property) {
		Node name = node.getAttributes().getNamedItem("name");
		String key = name.getNodeValue();
		property.setKey(key);
		property.setValue(decode(node.getTextContent()));
	}

	private void loadPlurals(Node node, Property property) {
		// TODO Auto-generated method stub
		
	}

	private void loadArray(Node node, Property property) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * decodes a string according to 
	 * http://developer.android.com/guide/topics/resources/string-resource.html#FormattingAndStyling
	 * @param textContent
	 * @return the decoded string
	 */
	private String decode(String textContent) {
		if(textContent==null)
			return null;
		if(textContent.startsWith("\"") && textContent.endsWith("\""))
			return textContent.substring(1, textContent.length()-1);
		return textContent.replace("\\'", "'");
	}
	
	@Override
	public int write(OutputStream out, PropertyFile file, String encoding) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}



}
