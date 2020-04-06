package org.jabylon.csharp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ContentHandler.ByteOrderMark;
import org.jabylon.properties.PropertiesFactory;
import org.jabylon.properties.Property;
import org.jabylon.properties.PropertyFile;
import org.jabylon.properties.types.PropertyConverter;
import org.jabylon.properties.types.impl.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CSharpConverter implements PropertyConverter{

	private static final String NAME_ATTRIBUTE = "name";
	private static final String XML_SPACE = "xml:space";
	private static final String DATA = "data";
	private static final String COMMENT = "comment";
	private static final String PRESERVE = "preserve";
	private static final String VALUE = "value";
	private static final String TYPE = "type";
	private static final String SYSTEM_STRING = "System.String";
	private static final String INVARIANT = "@Invariant";			// denotes non translatable content
	private static final String ROOT_NODE = "root";
	private static final String EMPTY_RES_FILE = readFileAsString("EmptyResFile");

	private static final Logger LOG = LoggerFactory.getLogger(CSharpConverter.class);

	/**
	 * allows to disable pretty print for unit tests
	 */
	private boolean prettyPrint = true;

	private String comment;

	public CSharpConverter(URI resourceLocation, boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}
	
	
	public static String readFileAsString(String key) {
	    String data = "";
	    
	    try {
	    	
	    	Properties prop = new Properties();
	    	ClassLoader loader = Thread.currentThread().getContextClassLoader();
	    	
	    	//String path = "/org/jabylon/csharp/const.properties";
	    	String path = "org/jabylon/csharp/const.properties";
	    	
	    	InputStream stream = loader.getResourceAsStream(path);
	    	if(stream == null)
	    		return data;
	    	prop.load(stream);
	      data = prop.getProperty(key);
	      //LOG.debug("header loaded: " + data);
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    return data;
	}


	@Override
	public PropertyFile load(InputStream in, String encoding) throws IOException {
        if(!in.markSupported())
            in = new BufferedInputStream(in);
        ByteOrderMark bom = PropertiesHelper.checkForBom(in);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document result = builder.parse(in);
			PropertyFile file = PropertiesFactory.eINSTANCE.createPropertyFile();

			Node resources = result.getDocumentElement();

			LOG.debug("resources.getTextContent(): " + resources.getTextContent());

			NodeList nodes = resources.getChildNodes();

			int nodesLength = nodes.getLength();
			LOG.debug("nodesLength: " + nodesLength);

			for (int i = nodesLength-1; i >= 0; i--) {
				Node node = nodes.item(i);
				LOG.debug("child node_" + i + " Name: " + node.getNodeName() + " Value: " + node.getNodeValue() + " Content:" + node.getTextContent() + " Type: " + node.getNodeType());

				if (loadNode(node,file)) {
					// 5/14/2019: Now we try plan B and replace the values in the xml file therefore there is no removal anymore of the Jabylon treated nodes
					//resources.removeChild(node);
				}
			}

			// Build a new Document includes the nodes getting treated by Jabylon
			DocumentBuilderFactory docBuilderFact = DocumentBuilderFactory.newInstance();
			docBuilderFact.setNamespaceAware(true);
			DocumentBuilder docBuilder = docBuilderFact.newDocumentBuilder();
			Document newDoc = docBuilder.newDocument();
			Node importedNode = newDoc.importNode(resources, true);
			newDoc.appendChild(importedNode);

			StringWriter sw = new StringWriter();
			String resFileAsString = "";

			Node firstNode = newDoc.getChildNodes().item(0);

		    try {
		      Transformer t = TransformerFactory.newInstance().newTransformer();
		      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		      t.setOutputProperty(OutputKeys.INDENT, "yes");
		      t.transform(new DOMSource(firstNode), new StreamResult(sw));
		      resFileAsString = sw.toString();
		      LOG.debug("resFileAsString: " + resFileAsString);
		    } catch (TransformerException te) {
		      LOG.error("TransformerException: ", te);
		    }
			file.setLicenseHeader(resFileAsString);		// we take the license header to store everything from our .resx

			return file;

		} catch (SAXException e) {
			throw new IOException(e);
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} finally {
			in.close();
		}
	}


	/**
	 *
	 * @param node
	 * @param file
	 * @return true:  node is provided for translation
	 *         false: node is not provided for translation
	 */
	private boolean loadNode(Node node, PropertyFile file) {
		String name = node.getNodeName();
		LOG.debug("Name: " + name);

		if(!name.equals(DATA)){
			LOG.debug("NodeName not appropriate");
			return false;
		}

		NamedNodeMap namedNodeMap = node.getAttributes();
		if (null == namedNodeMap) {
			LOG.error("namedNodeMap is null");
			return false;
		}

		Node namedNode = namedNodeMap.getNamedItem(NAME_ATTRIBUTE);
		if (null == namedNode) {
			LOG.error("NAME_ATTRIBUTE not found");
			return false;
		}

		for (int j=0; j<namedNodeMap.getLength(); j++) {
			Node attribute = namedNodeMap.item(j);
			String value = attribute.getNodeValue();
			LOG.debug("attribute_" + j + " Name: " + attribute.getNodeName() + " Value: " + value + " Content:" + attribute.getTextContent() + " Type: " + attribute.getNodeType());
			if (value.startsWith(">>")) {
				LOG.debug("node value not appropriate");
				return false;
			}
			if (attribute.getNodeName().equals(TYPE)) {
				if (!attribute.getNodeValue().equals(SYSTEM_STRING)) {
					LOG.debug("type attribute found which is not appropriate");
					return false;
				}
			}
		}

		// search for comment
		NodeList childNodes = node.getChildNodes();

		int childNodesLength = childNodes.getLength();
		LOG.debug("nodesLength: " + childNodesLength);

		for (int k = childNodesLength-1; k >= 0; k--) {
			Node childNode = childNodes.item(k);
			String nodeName = childNode.getNodeName();
			String nodeContent = childNode.getTextContent();
			LOG.debug("childNode_" + k + " Name: " + nodeName + " Value: " + childNode.getNodeValue() + " Content: " + nodeContent + " Type: " + childNode.getNodeType());
			if (null != nodeName) {
				if (nodeName.equals(COMMENT)) {
					LOG.debug("comment found");
					if (nodeContent.equals(INVARIANT)) {		// identifies an entry that is not provided for translation
						LOG.debug("comment indicates that this entry is not provided for translation, nodeContent: " + nodeContent);
						return false;
					}
					else {
						comment = nodeContent;
					}
				}
			}
		}

		String textContentName = namedNode.getTextContent();
		LOG.debug("textContentName: " + textContentName);

		Property property = PropertiesFactory.eINSTANCE.createProperty();
		String key = namedNode.getNodeValue();
		property.setKey(key);

		LOG.debug("provided for translation ");

		Node valueNode = getValueNode(node);
		if (null != valueNode) {
			String textcontent = valueNode.getTextContent(); 
			LOG.debug("textcontent: " + textcontent);
			property.setValue(textcontent);
		}

		if (null != comment) {
			LOG.debug("comment was set: " + comment);
			property.setComment(comment);
		}
		comment = null;
		LOG.debug("property to be added");
		file.getProperties().add(property);
		return true;
	}


	private Node getValueNode(Node node) {
		Node childNode = null;
		
		if (null != node) {
			childNode = getChildNode(node, VALUE);
		}
		
		return childNode;
	}


	private Node getCommentNode(Node node) {
		Node childNode = getChildNode(node, COMMENT);
		return childNode;
	}


	private Node getChildNode(Node node, String desiredNodeName) {
		LOG.debug("desiredNodeName: " + desiredNodeName);
		NodeList childNodes = node.getChildNodes();
		if (null != childNodes) {
			int numberOfChildren = childNodes.getLength();
			LOG.debug("number of child nodes: " + numberOfChildren);

			for (int i=0; i<numberOfChildren; i++) {
				Node childNode = childNodes.item(i);
				String nodeName = childNode.getNodeName();
				LOG.debug("node name: " + nodeName);
				if (0 == nodeName.compareTo(desiredNodeName)) {
					return childNode;
				}
			}
		}
		return null;
	}


	@Override
	public int write(OutputStream out, PropertyFile file, String encoding) throws IOException {
		try {
			int counter = 0;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();


			String licenseHeader = file.getLicenseHeader();
			
			if(isFilled(licenseHeader)) {
				LOG.debug("licenseHeader was filled: " + licenseHeader);
			}
			else {
				licenseHeader = EMPTY_RES_FILE;												// Obviously no translation for this .resx file existing
				LOG.debug("licenseHeader was not filled: " + licenseHeader);
			}
			
            try {
            	document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(licenseHeader)));
            	LOG.debug("document was built");
            } catch (Exception e) {
            	LOG.error("Exception when rewriting the non translatable part of the .resx file", e);
            }


			EList<Property> properties = file.getProperties();
			LOG.debug("count properties: " + properties.size());
			
			XPath xPath = XPathFactory.newInstance().newXPath();

			for (Property property : properties) {
				LOG.debug("C#:write10, property.getKey: " + property.getKey() + " property.getValue: " + property.getValue());

				try {
					Node nodeWithValue = getNodeWithValue(document, property.getKey(), xPath);

					if (null == nodeWithValue) {
						LOG.debug("node not found");
						Node rootNode = (Node)xPath.evaluate("/root", document, XPathConstants.NODE);
						
						if (null == rootNode) {
							LOG.debug("root node created");
							rootNode = document.createElement(ROOT_NODE);
							document.appendChild(rootNode);
							LOG.debug("root node appended");
						}

						Element newElem = document.createElement(DATA);
						newElem.setAttribute(NAME_ATTRIBUTE, property.getKey());
						newElem.setAttribute(XML_SPACE, PRESERVE);
						
						Element value = document.createElement(VALUE);
						value.setNodeValue(property.getValue());
						newElem.appendChild(value);
						
						LOG.debug("node with new translation created");
							
						rootNode.appendChild(newElem);
						nodeWithValue = getNodeWithValue(document, property.getKey(), xPath);
					}

					if (null != nodeWithValue) {
						LOG.debug("nodeWithValue.getNodeName: " + nodeWithValue.getNodeName() + " nodeWithValue.getTextContent: " + nodeWithValue.getTextContent() + " nodeWithValue.getNodeValue: " + nodeWithValue.getNodeValue()  + " nodeWithValue.getNodeType: " + nodeWithValue.getNodeType());
					}

					String newVal = property.getValue();
					// remove the carriage return:
					newVal = newVal.replaceAll("\\r", "");
					
					Node valueNode = getValueNode(nodeWithValue);
					if (null != valueNode) {
						LOG.debug("valueNode.getNodeName: " + valueNode.getNodeName() + " valueNode.getTextContent: " + valueNode.getTextContent() + " valueNode.getNodeValue: " + valueNode.getNodeValue()  + " valueNode.getNodeType: " + valueNode.getNodeType());
						valueNode.setTextContent(newVal);
					}
					else {
						Element newElem = document.createElement(VALUE);
						LOG.debug("value node inserted: " + newVal);
				        newElem.setTextContent(newVal);
					}

					String newComment = property.getComment();
					if (null != newComment) {
						// remove the carriage return:
						newComment = newComment.replaceAll("\\r", "");
						LOG.debug("newComment: " + newComment);
						Node commentNode = getCommentNode(nodeWithValue);
						if (null != commentNode) {
							LOG.debug("comment replaced: " + newComment);
							commentNode.setTextContent(newComment);
						}
						else {
							Node newNode = nodeWithValue.appendChild(document.createElement(COMMENT));
							LOG.debug("comment inserted");
					        newNode.setTextContent(newComment);
						}
					}
				} catch (Exception e) {
					LOG.error("exception when writing: ", e);
				}
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			if(prettyPrint) {
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			}
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(out);

			transformer.transform(source, result);
			return counter;
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (TransformerConfigurationException e) {
			throw new IOException(e);
		} catch (TransformerException e) {
			throw new IOException(e);
		} finally{
			out.close();
		}
	}


	private Node getNodeWithValue(Document document, String key, XPath xPath) {
		String searchStr = "//data[@name=\"" + key + "\"]";
		LOG.debug("searchstr: " + searchStr);
			Node nodeWithValue =null;
			try {
				nodeWithValue = (Node)xPath.evaluate(searchStr, document, XPathConstants.NODE);
			} catch (XPathExpressionException e) {
				LOG.debug("failed: ", e);
			}
			return nodeWithValue;
	}


	private boolean isFilled(String s){
		return s!=null && !s.isEmpty();
	}
}