/**
 *
 */
package org.jabylon.csharp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.wicket.util.crypt.StringUtils;
import org.eclipse.emf.common.util.URI;
import org.jabylon.properties.PropertiesFactory;
import org.jabylon.properties.PropertiesPackage;
import org.jabylon.properties.ScanConfiguration;
import org.jabylon.properties.types.PropertyConverter;
import org.jabylon.properties.types.PropertyScanner;
import org.jabylon.properties.types.impl.AbstractPropertyScanner;
//import org.jabylon.security.CommonPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * adds support for the C# xml format.
 *
 * @author c.gromer@seeburger.de
 *
 */
@Component(enabled=true,immediate=true)
@Service
public class CSharpScanner implements PropertyScanner {

	@Property(name=PropertyScanner.TYPE, value="CSHARP")
	public  static final String TYPE = "CSHARP";

    private static final String[] DEFAULT_EXCLUDES = {};
    private static final String[] DEFAULT_INCLUDES = {"**/*.resx"};

    private static final Logger LOG = LoggerFactory.getLogger(CSharpScanner.class);

	public CSharpScanner() {
		LOG.debug("C#:CSharpScanner1");
	}


	@Override
	public String[] getDefaultIncludes() {
		LOG.debug("C#:getDefaultIncludes1");
		return DEFAULT_INCLUDES;
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#getDefaultExcludes()
	 */
	@Override
	public String[] getDefaultExcludes() {
		LOG.debug("C#:getDefaultExcludes1");
		return DEFAULT_EXCLUDES;
	}


	@Override
	public boolean isTemplate(File propertyFile, String masterLocale) {
		LOG.debug("C#:isTemplate2 propertyFile: " + propertyFile.getName() + " masterLocale: " + masterLocale);

		if(isResourceFile(propertyFile) && countingPeriodsFullfillsTemplateFileCondition(propertyFile)) {
			LOG.debug("C#:isTemplate3 propertyFile is a template");
			return true;
		}
		
		LOG.debug("C#:isTemplate4 propertyFile is not a template");
		return false;
	}


	@Override
	public boolean isTranslation(File propertyFile, ScanConfiguration config) {
		String fileName = propertyFile.getName();
		LOG.debug("C#:isTranslation2 fileName: " + fileName);

		if (isResourceFile(propertyFile) && (countingPeriodsFullfillsTranslationFileCondition(propertyFile))) {
			LOG.debug("C#:isTranslation3 property file is a translation file");
			return true;
		}
		
		LOG.debug("C#:isTranslation4 property file is not a translation file");
		return false;
	}


	private boolean countingPeriodsFullfillsTranslationFileCondition(File propertyFile) {
		//E.g.: Resources.nl.resx is a translation file, Resources.resx is a template file
        LOG.debug("C#:countingPeriodsFullfillsTranslationFileCondition1: propertyFile: " + propertyFile.getName());

        if (2 == propertyFile.getName().length() - propertyFile.getName().replace(".","").length()) {
            LOG.debug("C#:countingPeriodsFullfillsTranslationFileCondition2: translation file" );
            return true;
        }
        LOG.debug("C#:countingPeriodsFullfillsTranslationFileCondition3: no translation file" );
        return false;
	}


	private boolean countingPeriodsFullfillsTemplateFileCondition(File propertyFile) {
		//E.g.: Resources.nl.resx is a translation file, Resources.resx is a template file
		boolean bReturn = false;
	    LOG.debug("C#:countingPeriodsFullfillsTemplateFileCondition1: propertyFile: " + propertyFile.getName());

        if (1 == propertyFile.getName().length() - propertyFile.getName().replace(".","").length()) {
            LOG.debug("C#:countingPeriodsFullfillsTemplateFileCondition2: template" );
            bReturn = true;
        }

        LOG.debug("C#:countingPeriodsFullfillsTemplateFileCondition3: bReturn: " + bReturn);
        return bReturn;
	}


	private boolean isResourceFile(File propertyFile) {
		LOG.debug("C#:isResourceFile2, propertyFile: " + propertyFile.getName());
		boolean isResFile = false;
		if(propertyFile.getName().endsWith(".resx")) {
			LOG.debug("C#:isResourceFile3: resource file" );
			isResFile = true;
		}
		LOG.debug("C#:isResourceFile4: isResFile: " + isResFile );
		return isResFile;
	}


	@Override
	public File findTemplate(File propertyFile, ScanConfiguration config) {
		LOG.info("C#:findTemplate1");
		if (isTranslation(propertyFile, config)) {
			String fileName = propertyFile.getName();
			LOG.debug("C#:findTemplate2: fileName: " + fileName );		// e.g.: Resources.nl.resx
			String[] strings = fileName.split(".", -1);
			if (3 == strings.length) {
				String templateFile = strings[0] + "." + strings[2];		// e.g.: Resources.resx
				LOG.debug("C#:findTemplate3: templateFile: " + templateFile);
				return new File(templateFile);
			}
		}
		LOG.debug("C#:findTemplate4");
		return null;
	}


	@Override
	public Map<Locale, File> findTranslations(File template, ScanConfiguration config) {
		LOG.debug("C#:findTranslations1, template: " + template.getName());
		if (isTemplate(template, "")) {
			// instead of using org.apache.commons:
			String templateWithoutExtension = getFirstPart(template);
			String folder = template.getParent();
			LOG.debug("C#:findTranslations2: folder: " + folder);
			File[] files = new File(folder).listFiles();
			Map<Locale, File> translations = new HashMap<Locale, File>();
			for (File file : files) {
				LOG.debug("C#:findTranslations3: file: " + file.getName());
				if (templateWithoutExtension.equals(getFirstPart(file))) {
					LOG.debug("C#:findTranslations4: first part matches");
					if (isTranslation(file, config)) {
						LOG.debug("C#:findTranslations5: is translation");
						translations.put(getLocale(file), file);
					}
				}
			}
			return translations;
		}
		return null;
	}

	
	private String getFirstPart(File file) {
		String firstPart = "";
		String fileParts[] = file.getName().split("\\.");
		
		if (0 < fileParts.length) {
			firstPart = fileParts[0];
		}
		LOG.debug("C#:getFirstPart, return: " + firstPart);
		return(firstPart);
	}


	@Override
	public File computeTranslationPath(File template, Locale templateLocale, Locale translationLocale) {
		LOG.debug("C#:computeTranslationPath1, template:" + template.getName() + " template.getPath: " + template.getPath());
		File folder = template.getParentFile();			// template and translation files reside in the same folder
		
		if (null != folder) {
			LOG.debug("C#:computeTranslationPath2, folder: " + folder.getName());
			LOG.debug("C#:computeTranslationPath2.1, folder.getPath: " + folder.getPath());
			LOG.debug("C#:computeTranslationPath2.2, folder.getParent: " + folder.getParent());
		}

		return folder;
	}


	@Override
	public Locale getLocale(File propertyFile) {
		LOG.debug("C#:getLocale1, propertyFile: " + propertyFile);
		String propFileName = propertyFile.getName();				// e.g.: dialog.resx (template file), dialog.nl.resx (translation file)
		LOG.debug("C#:getLocale2, propFileName: " + propFileName);
		
		String[] splittedPropFileName = propFileName.split("\\.");
		
		String language = "";		// "en" in "en_US"
		String culture = "";		// "US" in "en_US" but there is also "Hans" in "zh-Hans"
		boolean isTemplate = false;
		
		if (1 == splittedPropFileName.length) {
			LOG.debug("C#:getLocale3, obviously no property file: " + propFileName);
		}
		else if (1 < splittedPropFileName.length) {
			LOG.debug("C#:getLocale4, splittedPropFileName.length: " + splittedPropFileName.length);
			
			if (2 == splittedPropFileName.length) {
				//e.g.: dialog.resx (template file)
				isTemplate = true;
			}
			else if (3 == splittedPropFileName.length){
				if (2 == splittedPropFileName[splittedPropFileName.length-2].length()) {
					LOG.debug("C#:getLocale6, only language ");
					language = splittedPropFileName[splittedPropFileName.length-2];
				}
				else {
					LOG.debug("C#:getLocale7, splittedPropFileName[splittedPropFileName.length-2].substring(2,3): " + splittedPropFileName[splittedPropFileName.length-2].substring(2,3));
					if ("-".equals(splittedPropFileName[splittedPropFileName.length-2].substring(2,3))) {
						LOG.debug("C#:getLocale8, language + culture ");
						language = splittedPropFileName[splittedPropFileName.length-2].substring(0,2);		// two chars
						culture = splittedPropFileName[splittedPropFileName.length-2].substring(3);			// can be more than two chars
					}
				}
			}
		}
		Locale loc = null;

		if (false == isTemplate) {
			LOG.debug("C#:getLocale11, no Template");
			if (0 < language.length()){
				LOG.debug("C#:getLocale12, isTranslation File, language: " + language + " culture: " + culture);
				loc = new Locale(language, culture);
			}
		}
		return loc;
	}


	@Override
	public boolean isBilingual() {
		LOG.debug("C#:isBilingual1");
		return false;
	}


	@Override
	public PropertyConverter createConverter(URI resource) {
		LOG.debug("C#:createConverter1, resource: " + resource.path());
		return new CSharpConverter(resource, true);
	}


	@Override
	public String getEncoding() {
		String encoding = "UTF-8"; 
		LOG.debug("C#:getEncoding1, encoding: " + encoding);
		return encoding;
	}
}
