package org.jabylon.csharp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.jabylon.csharp.CSharpScanner;
import org.jabylon.properties.Property;
import org.jabylon.properties.PropertyFile;
import org.jabylon.properties.ScanConfiguration;;
import org.junit.Before;
import org.junit.Test;


public class CSharpScannerTest {

	private CSharpScanner fixture;
	
	private static final String RESX_FILE = "src/test/resources/TestRibbon.resx";
	private static final String RESX_FILE_NL = "src/test/resources/TestRibbon.nl.resx";
	private static final String RESX_FILE_DE = "src/test/resources/TestRibbon.de.resx";
	
	@Before
	public void setup() {
		fixture = new CSharpScanner();
	}
	
	@Test
	public void testIsTemplate() throws IOException {
		File resourceFile_nl = getFileObject(RESX_FILE_NL);
		if (true == fixture.isTemplate(resourceFile_nl, "")) {
			fail(RESX_FILE_NL + " is not a template file! ");
		}
		
		File resourceFile = getFileObject(RESX_FILE);
		if (false == fixture.isTemplate(resourceFile, "")) {
			fail(RESX_FILE + " is a template file!");
		}
	}
	
	@Test
	public void testIsTranslation() throws IOException {
		File resourceFile_de = getFileObject(RESX_FILE_DE);
		if (false == fixture.isTranslation(resourceFile_de, null)) {
			fail(RESX_FILE_DE + " is a translation file! ");
		}
		
		File resourceFile = getFileObject(RESX_FILE);
		if (true == fixture.isTranslation(resourceFile, null)) {
			fail(RESX_FILE + " is not a translation file!");
		}
	}
	
	private File getFileObject(String path) {
		File file = new File(path);
		if (null == file) {
			fail(" File object could not be built from path " + path);
		}
		return file;
	}

	
	@Test
	public void testFindTranslations() throws IOException {
		File template = getFileObject(RESX_FILE);
		Map<Locale, File> translationFiles = fixture.findTranslations(template, null);
		assertEquals(2, translationFiles.size());
		
		Locale de = new Locale("de");
		if (false == translationFiles.containsKey(de))
			fail("No translation file found that was suitable for German local");
		Locale nl = new Locale("nl");
		if (false == translationFiles.containsKey(nl))
			fail("No translation file found that was suitable for Dutch local");
		
		File trans_NL = getFileObject(RESX_FILE_NL);
		if (false == translationFiles.containsValue(trans_NL))
			fail(" Dutch translation file not found! ");
		
		File trans_DE = getFileObject(RESX_FILE_DE);
		if (false == translationFiles.containsValue(trans_DE))
			fail(" German translation file not found! ");
	}
}
