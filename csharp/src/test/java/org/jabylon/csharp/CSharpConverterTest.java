package org.jabylon.csharp;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.jabylon.csharp.CSharpConverter;
import org.jabylon.properties.Property;
import org.jabylon.properties.PropertyFile;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CSharpConverterTest {

	private CSharpConverter fixture;

	private static final String RESX_FILE = "src/test/resources/TestRibbon.resx";
	
	private static final Logger LOG = LoggerFactory.getLogger(CSharpConverterTest.class);
	
	@Before
	public void setup() {
		
		URI resourceLocation = URI.createFileURI(RESX_FILE);
		
		if (null != resourceLocation) {
			fixture = new CSharpConverter(resourceLocation, false);
		}
	}

	@Test
	public void testLoad() throws IOException {
		PropertyFile result = fixture.load(loadXML(new File(RESX_FILE)), "UTF-8");
		EList<Property> properties = result.getProperties();
		int sizeOfProperties = properties.size();
		assertEquals(3, sizeOfProperties);
		int count = 0;
		Property property = properties.get(count++);
		assertEquals("SeeFxAboutButton.Label", property.getKey());
		assertEquals("About BIS FileExchange...", property.getValue());

		property = properties.get(count++);
		assertEquals("SeeFxPrefsButton.Label", property.getKey());
		assertEquals("BIS FileExchange Prefs...", property.getValue());

		property = properties.get(count++);
		assertEquals("SeeFxSyncManagerStateViewButton.Label", property.getKey());
		assertEquals("BIS FileExchange SyncManager Status Overview", property.getValue());
	}

	private InputStream loadXML(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}

}