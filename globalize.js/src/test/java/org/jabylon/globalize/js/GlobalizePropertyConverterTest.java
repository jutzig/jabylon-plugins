package org.jabylon.globalize.js;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.jabylon.properties.PropertyFile;
import org.junit.Before;
import org.junit.Test;

public class GlobalizePropertyConverterTest {

	private GlobalizePropertyConverter fixture;

	@Before
	public void setUp() throws Exception {
		fixture = new GlobalizePropertyConverter(URI.createFileURI("unittest"));
	}

	@Test
	public void testLoad() throws FileNotFoundException, IOException {
		PropertyFile propertyFile = fixture.load(new FileInputStream("src/test/resources/globalize/en.json"), "UTF-8");
		assertEquals("Hello",propertyFile.getProperty("hello").getValue());
		assertEquals("value",propertyFile.getProperty("world/prop").getValue());
		assertEquals("value",propertyFile.getProperty("test").getValue());
		assertEquals(3, propertyFile.getProperties().size());
	}

}
