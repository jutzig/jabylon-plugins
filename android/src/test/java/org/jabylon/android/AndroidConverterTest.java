package org.jabylon.android;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.emf.common.util.URI;
import org.jabylon.properties.PropertiesFactory;
import org.jabylon.properties.Property;
import org.jabylon.properties.PropertyFile;
import org.junit.Before;
import org.junit.Test;

public class AndroidConverterTest {

	private AndroidConverter fixture;
	
	@Before
	public void setup() {
		fixture = new AndroidConverter(URI.createFileURI("src/test/resources/android.xml"),false);
		
	}
	
	@Test
	public void testLoad() throws IOException {
		PropertyFile result = fixture.load(loadXML(new File("src/test/resources/android.xml")),"UTF-8");
		assertEquals(4, result.getProperties().size());
		assertEquals(" license header ", result.getLicenseHeader());
	}
	
	@Test
	public void testSimpleString() throws IOException {
		PropertyFile result = fixture.load(loadXML(new File("src/test/resources/android.xml")),"UTF-8");
		Property property = result.getProperty("hello");
		assertEquals("hello", property.getKey());
		assertEquals("Hello!", property.getValue());
	}
	
	@Test
	public void testSimpleStringWithComment() throws IOException {
		PropertyFile result = fixture.load(loadXML(new File("src/test/resources/android.xml")),"UTF-8");
		Property property = result.getProperty("test");
		assertEquals("test", property.getKey());
		assertEquals("test", property.getValue());
		assertEquals(" testcomment ", property.getComment());
	}
	
	@Test
	public void testStringArray() throws IOException {
		PropertyFile result = fixture.load(loadXML(new File("src/test/resources/android.xml")),"UTF-8");
		Property property = result.getProperty("planets_array");
		assertEquals("planets_array", property.getKey());
		assertEquals("Mercury\nVenus\nEarth\nMars", property.getValue());
	}
	
	@Test
	public void testPlurals() throws IOException {
		PropertyFile result = fixture.load(loadXML(new File("src/test/resources/android.xml")),"UTF-8");
		Property property = result.getProperty("numberOfSongsAvailable");
		assertEquals("numberOfSongsAvailable", property.getKey());
		assertEquals("one) One song found.\nother) %d songs found.", property.getValue());
	}
	
	@Test
	public void testWriteSimpleString() throws IOException {
		PropertyFile file = PropertiesFactory.eINSTANCE.createPropertyFile();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Property property = PropertiesFactory.eINSTANCE.createProperty();
		file.getProperties().add(property);
		property.setKey("key");
		property.setValue("value");
		property.setComment("comment");
		fixture.write(out, file, "UTF-8");
		assertEquals("<?xml version='1.0' encoding='UTF-8' standalone='no'?><resources><!--comment--><string name='key'>value</string></resources>".replace("'", "\""), new String(out.toByteArray(),"UTF-8"));
	}
	
	@Test
	public void testWriteStringArray() throws IOException {
		PropertyFile file = PropertiesFactory.eINSTANCE.createPropertyFile();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Property property = PropertiesFactory.eINSTANCE.createProperty();
		file.getProperties().add(property);
		property.setKey("key");
		property.setValue("value\nvalue2");
		property.setComment("@string-array");
		fixture.write(out, file, "UTF-8");
		assertEquals("<?xml version='1.0' encoding='UTF-8' standalone='no'?><resources><!--@string-array--><string-array name='key'><item>value</item><item>value2</item></string-array></resources>".replace("'", "\""), new String(out.toByteArray(),"UTF-8"));
	}
	
	@Test
	public void testWritePlurals() throws IOException {
		PropertyFile file = PropertiesFactory.eINSTANCE.createPropertyFile();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Property property = PropertiesFactory.eINSTANCE.createProperty();
		file.getProperties().add(property);
		property.setKey("key");
		property.setValue("one) value\nmany) value2");
		property.setComment("@plurals");
		fixture.write(out, file, "UTF-8");
		assertEquals("<?xml version='1.0' encoding='UTF-8' standalone='no'?><resources><!--@plurals--><plurals name='key'><item quantity='one'>value</item><item quantity='many'>value2</item></plurals></resources>".replace("'", "\""), new String(out.toByteArray(),"UTF-8"));
	}	
	
	@Test
	public void testWrite() throws IOException {
		PropertyFile file = PropertiesFactory.eINSTANCE.createPropertyFile();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Property property = PropertiesFactory.eINSTANCE.createProperty();
		file.getProperties().add(property);
		file.setLicenseHeader("license header");
		property.setKey("key");
		property.setValue("value");
		assertEquals(1, fixture.write(out, file, "UTF-8"));
		assertEquals("<?xml version='1.0' encoding='UTF-8' standalone='no'?><!--license header--><resources><string name='key'>value</string></resources>".replace("'", "\""), new String(out.toByteArray(),"UTF-8"));
	}
		

	private InputStream loadXML(File file) throws FileNotFoundException {
		return new FileInputStream(file);
		
	}

}
