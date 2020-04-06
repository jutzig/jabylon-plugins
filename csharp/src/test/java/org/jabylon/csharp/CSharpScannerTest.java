package org.jabylon.csharp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.jabylon.properties.PropertiesFactory;
import org.jabylon.properties.ScanConfiguration;
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
		File resourceFile_nl = new File(RESX_FILE_NL);
		assertFalse(RESX_FILE_NL + " is not a template file! ",fixture.isTemplate(resourceFile_nl, ""));

		File resourceFile = new File(RESX_FILE);
		assertTrue(RESX_FILE + " is a template file!",fixture.isTemplate(resourceFile, ""));


		assertFalse(fixture.isTemplate(new File("TestRibbon.resx"), "en_US"));
		assertFalse(fixture.isTemplate(new File("TestRibbon.de-DE.resx"), "en_US"));
		assertTrue(fixture.isTemplate(new File("TestRibbon.en-us.resx"), "en_US"));

	}

	@Test
	public void testIsTranslation() throws IOException {
		File resourceFile_de = new File(RESX_FILE_DE);
		ScanConfiguration config = PropertiesFactory.eINSTANCE.createScanConfiguration();

		assertTrue(RESX_FILE_DE + " is a translation file! ",fixture.isTranslation(resourceFile_de, config));
		File resourceFile = new File(RESX_FILE);
		assertFalse(RESX_FILE + " is not a translation file! ",fixture.isTranslation(resourceFile, config));

		config.setMasterLocale("en_US");
		assertFalse(fixture.isTranslation(new File("TestRibbon.en-us.resx"), config));
		assertFalse(fixture.isTranslation(new File("TestRibbon.resx"), config));
		assertTrue(fixture.isTranslation(new File("TestRibbon.de-DE.resx"), config));
	}

	@Test
	public void testFindTranslations() throws IOException {
		File template = new File(RESX_FILE);
		Map<Locale, File> translationFiles = fixture.findTranslations(template, null);
		assertEquals(2, translationFiles.size());

		Locale de = new Locale("de");
		assertTrue("No translation file found that was suitable for German local",translationFiles.containsKey(de));
		Locale nl = new Locale("nl");
		assertTrue("No translation file found that was suitable for Dutch local",translationFiles.containsKey(nl));

		File trans_NL = new File(RESX_FILE_NL);
		assertTrue(" Dutch translation file not found! ",translationFiles.containsValue(trans_NL));

		File trans_DE = new File(RESX_FILE_DE);
		assertTrue(" German translation file not found! ",translationFiles.containsValue(trans_DE));

	}

	@Test
	public void testGetLocale() throws Exception {
		assertEquals(new Locale("de","AT"),fixture.getLocale(new File("Test.de-AT.resx")));
		assertEquals(new Locale("en"),fixture.getLocale(new File("Test.en.resx")));
		assertEquals(new Locale("zh","Hans"),fixture.getLocale(new File("Test.zh-Hans.resx")));
		assertEquals(new Locale("en"),fixture.getLocale(new File("Test.de-DE.en.resx")));

	}

	@Test
	public void testComputeTranslationPath() throws Exception {
		assertEquals("test.de-DE.resx", fixture.computeTranslationPath(new File("test.resx"), null, new Locale("de","DE")).getName());
		assertEquals("test.de-DE.resx", fixture.computeTranslationPath(new File("test.en.resx"), new Locale("en"), new Locale("de","DE")).getName());
		assertEquals("test.de-AT.resx", fixture.computeTranslationPath(new File("test.en-US.resx"), new Locale("en","US"), new Locale("de","AT")).getName());
		assertEquals("test.de-AT.resx", fixture.computeTranslationPath(new File("test.zh-Hans.resx"), new Locale("zh","Hans"), new Locale("de","AT")).getName());

	}

	@Test
	public void testFindTemplate() {
		ScanConfiguration configuration = PropertiesFactory.eINSTANCE.createScanConfiguration();
		assertEquals("test.resx", fixture.findTemplate(new File("test.de-DE.resx"), configuration).getName());
		assertEquals("test.resx", fixture.findTemplate(new File("test.de.resx"), configuration).getName());
	}

	@Test
	public void testFindTemplateWithMasterLocale() {
		ScanConfiguration configuration = PropertiesFactory.eINSTANCE.createScanConfiguration();
		configuration.setMasterLocale("en_US");
		assertEquals("test.en-US.resx", fixture.findTemplate(new File("test.de-DE.resx"), configuration).getName());
		assertEquals("test.en-US.resx", fixture.findTemplate(new File("test.de-US.resx"), configuration).getName());
	}

}