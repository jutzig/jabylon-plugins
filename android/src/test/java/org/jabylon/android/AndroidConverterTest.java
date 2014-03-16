package org.jabylon.android;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.emf.common.util.URI;
import org.jabylon.properties.Property;
import org.jabylon.properties.PropertyFile;
import org.junit.Before;
import org.junit.Test;

public class AndroidConverterTest {

	private AndroidConverter fixture;
	
	@Before
	public void setup() {
		fixture = new AndroidConverter(URI.createFileURI("src/test/resources/android.xml"));
	}
	
	@Test
	public void testLoad() throws IOException {
		PropertyFile result = fixture.load(loadXML(new File("src/test/resources/android.xml")),"UTF-8");
		assertEquals(4, result.getProperties().size());
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

	private InputStream loadXML(File file) throws FileNotFoundException {
		return new FileInputStream(file);
		
	}

}
