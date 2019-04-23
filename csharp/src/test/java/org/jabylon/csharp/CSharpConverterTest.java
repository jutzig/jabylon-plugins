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


public class CSharpConverterTest {

	private CSharpConverter fixture;
	
	private static final String RESX_FILE = "src/test/resources/TestRibbon.resx";
	
	@Before
	public void setup() {
		fixture = new CSharpConverter(URI.createFileURI(RESX_FILE), false);
	}
	
	@Test
	public void testLoad() throws IOException {
		PropertyFile result = fixture.load(loadXML(new File(RESX_FILE)), "UTF-8");
		EList<Property> properties = result.getProperties();
		int sizeOfProperties = properties.size();
		assertEquals(3, sizeOfProperties);
		for (int i=0; i<sizeOfProperties; i++) {
			Property property = properties.get(i);
			String key = property.getKey();
			String value = property.getValue();
			switch(i) {
				case 0:
					assertEquals("SeeFxAboutButton.Label", key);
					assertEquals("About BIS FileExchange...", value);
					break;
				case 1:
					assertEquals("SeeFxPrefsButton.Label", key);
					assertEquals("BIS FileExchange Prefs...", value);
					break;
				case 2:
					assertEquals("SeeFxSyncManagerStateViewButton.Label", key);
					assertEquals("BIS FileExchange SyncManager Status Overview", value);
					break;
			}
		}
	}


	private InputStream loadXML(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}

}
