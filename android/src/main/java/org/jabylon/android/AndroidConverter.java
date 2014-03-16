package org.jabylon.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.jabylon.properties.PropertiesFactory;
import org.jabylon.properties.PropertiesPackage;
import org.jabylon.properties.Property;
import org.jabylon.properties.PropertyAnnotation;
import org.jabylon.properties.PropertyFile;
import org.jabylon.properties.types.PropertyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AndroidConverter implements PropertyConverter{

	private static final String NAME_ATTRIBUTE = "name";
	private static final String QUANTITY_ATTRIBUTE = "quantity";

	private static final String STRING = "string";
	
	private static final String STRING_ARRAY = "string-array";

	private static final String PLURALS = "plurals";

	private static final String ITEM = "item";

	private static final String ROOT_NODE = "resources";
	
	/**
	 * for plurals in the form
	 * <pre>
	 * one) value1
	 * two) value2
	 * </pre>
	 */
	private static final Pattern PLURAL_PATTERN = Pattern.compile("(\\w+)\\)\\s*(.*)");

	private static final Logger LOG = LoggerFactory.getLogger(AndroidConverter.class);
	
	/**
	 * allows to disable pretty print for unit tests
	 */
	private boolean prettyPrint = true;
	
	private URI uri;

	private String comment;
	
	public AndroidConverter(URI resourceLocation, boolean prettyPrint) {
		this.uri = resourceLocation;
		this.prettyPrint = prettyPrint;
	}
	
	@Override
	public PropertyFile load(InputStream in, String encoding) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document result = builder.parse(in);
			PropertyFile file = PropertiesFactory.eINSTANCE.createPropertyFile();
			Node firstNode = result.getChildNodes().item(0);
			if(firstNode.getNodeType()==Node.COMMENT_NODE)
				file.setLicenseHeader(firstNode.getNodeValue());
			Node resources = result.getDocumentElement();
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
			throw new IOException(e);
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} finally {
			in.close();
		}
	}

	private void loadNode(Node node, PropertyFile file) {
		
		if(node.getNodeType()==Node.TEXT_NODE)
			return;
		if(node.getNodeType()==Node.COMMENT_NODE) {
			comment = node.getNodeValue();
			
			return;
		}
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
			LOG.error("Invalid tag "+name+" found in "+uri+" Skipping");
			return;
		}
		property.setComment(comment);
		comment = null;
		file.getProperties().add(property);
	}

	private void loadString(Node node, Property property) {
		Node name = node.getAttributes().getNamedItem(NAME_ATTRIBUTE);
		String key = name.getNodeValue();
		property.setKey(key);
		property.setValue(decode(node.getTextContent()));
	}

	private void loadPlurals(Node node, Property property) {
		NodeList nodes = node.getChildNodes();
		Node name = node.getAttributes().getNamedItem(NAME_ATTRIBUTE);
		String key = name.getNodeValue();
		property.setKey(key);
		StringBuilder value = new StringBuilder();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node item = nodes.item(i);
			if(ITEM.equals(item.getNodeName()))
			{
				Node quantityAttr = item.getAttributes().getNamedItem(QUANTITY_ATTRIBUTE);
				String quantity = quantityAttr.getNodeValue();
				value.append(quantity);
				value.append(") ");
				value.append(item.getTextContent());
				value.append("\n");
			}
		}
		if(value.length()>0)
			//get rid of last linefeed
			value.setLength(value.length()-1);
		property.setValue(decode(value.toString()));
		if(property.findAnnotation(PLURALS)==null) {
			PropertyAnnotation annotation = PropertiesFactory.eINSTANCE.createPropertyAnnotation();
			annotation.setName(PLURALS);
			property.getAnnotations().add(annotation);			
		}
		
	}

	private void loadArray(Node node, Property property) {
		
		NodeList nodes = node.getChildNodes();
		Node name = node.getAttributes().getNamedItem(NAME_ATTRIBUTE);
		String key = name.getNodeValue();
		property.setKey(key);
		StringBuilder value = new StringBuilder();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node item = nodes.item(i);
			if(ITEM.equals(item.getNodeName()))
			{
				value.append(item.getTextContent());
				value.append("\n");
			}
		}
		if(value.length()>0)
			//get rid of last linefeed
			value.setLength(value.length()-1);
		property.setValue(decode(value.toString()));
		if(property.findAnnotation(STRING_ARRAY)==null) {
			PropertyAnnotation annotation = PropertiesFactory.eINSTANCE.createPropertyAnnotation();
			annotation.setName(STRING_ARRAY);
			property.getAnnotations().add(annotation);			
		}
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
	
	private String encode(String textContent) {
		if(textContent==null)
			return null;
		return textContent.replace("'", "\\'").replace("\"", "\\\"");
	}
	
	private boolean isFilled(String s){
		return s!=null && !s.isEmpty();
	}
	
	@Override
	public int write(OutputStream out, PropertyFile file, String encoding) throws IOException {
		try {
			int counter = 0;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document document = factory.newDocumentBuilder().newDocument();
			if(isFilled(file.getLicenseHeader()))
			{
				document.appendChild(document.createComment(file.getLicenseHeader()));
			}
			Element root = document.createElement(ROOT_NODE);
			document.appendChild(root);
			EList<Property> properties = file.getProperties();
			for (Property property : properties) {
				if(writeProperty(root, document, property))
					counter++;
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			if(prettyPrint){
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

	private boolean writeProperty(Element root, Document document, Property property) throws IOException {
		if(!isFilled(property.getValue()))
			return false;
		writeCommentAndAnnotations(root, document, property);
		if(property.findAnnotation(STRING_ARRAY)!=null)
			writeStringArray(root,document,property);
		else if(property.findAnnotation(PLURALS)!=null)
			writePlurals(root,document,property);
		else
			writeString(root,document,property);
		return true;
	}
	

	private void writeString(Element root, Document document, Property property) {
		Element string = document.createElement(STRING);
		root.appendChild(string);
		string.setAttribute(NAME_ATTRIBUTE, property.getKey());
		string.setTextContent(encode(property.getValue()));
	}

	private void writeStringArray(Element root, Document document, Property property) {
		String[] split = property.getValue().split("(\r)?\n");
		Element array = document.createElement(STRING_ARRAY);
		array.setAttribute(NAME_ATTRIBUTE, property.getKey());
		root.appendChild(array);
		for (String string : split) {
			Element item = document.createElement(ITEM);
			item.setTextContent(encode(string));
			array.appendChild(item);
		}
	}
	
	private void writePlurals(Element root, Document document, Property property) {
		String[] split = property.getValue().split("(\r)?\n");
		Element array = document.createElement(PLURALS);
		array.setAttribute(NAME_ATTRIBUTE, property.getKey());
		root.appendChild(array);
		for (String string : split) {
			Element item = document.createElement(ITEM);
			Matcher matcher = PLURAL_PATTERN.matcher(string);
			if(matcher.matches()) {
				String quantity = matcher.group(1);
				String value = matcher.group(2);
				item.setAttribute(QUANTITY_ATTRIBUTE, quantity);
				item.setTextContent(encode(value));	
			}
			else
				LOG.error("Property "+property + " contains invalid plural '"+string+"'. Location "+uri);
			array.appendChild(item);
		}
		
	}

	private void writeCommentAndAnnotations(Element root, Document document, Property property) throws IOException {
        if(property.eIsSet(PropertiesPackage.Literals.PROPERTY__COMMENT) || property.getAnnotations().size()>0)
        {
        	String comment = property.getCommentWithoutAnnotations();
        	StringBuilder builder = new StringBuilder();
        	for (PropertyAnnotation annotation : property.getAnnotations()) {
				builder.append(annotation);
			}
        	if(builder.length()>0 && comment!=null && comment.length()>0 )
        	{
        		builder.append("\n");
        	}
        	builder.append(comment);
        	Comment node = document.createComment(builder.toString());
        	root.appendChild(node);
        }
	}


}
