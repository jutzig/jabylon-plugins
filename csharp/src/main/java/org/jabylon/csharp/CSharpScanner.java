/**
 *
 */
package org.jabylon.csharp;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.emf.common.util.URI;
import org.jabylon.properties.PropertiesFactory;
import org.jabylon.properties.PropertiesPackage;
import org.jabylon.properties.ScanConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.jabylon.properties.types.PropertyConverter;
import org.jabylon.properties.types.PropertyScanner;
import org.jabylon.properties.types.impl.AbstractPropertyScanner;



/**
 * adds support for the C# xml format.
 *
 * @author c.gromer@seeburger.de
 *
 */
@Component(enabled=true,immediate=true)
@Service
public class CSharpScanner extends AbstractPropertyScanner {

	@Property(name=PropertyScanner.TYPE, value="CSHARP")
	public  static final String TYPE = "CSHARP";

    private static final String[] DEFAULT_EXCLUDES = {};
    private static final String[] DEFAULT_INCLUDES = {"**/*.resx"};
    
    private static final Logger LOG = LoggerFactory.getLogger(CSharpScanner.class);

    /**
     * group1: basename
     * group2: locale (optional)
     * group3: extension
     *
     */
    static final Pattern LOCALE_PATTERN = Pattern.compile("(.+?)((?:\\.\\p{Alpha}+)(?:-\\p{Alpha}+)?)?(\\.resx)");

	public CSharpScanner() {
		LOG.info("C#:CSharpScanner1");
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
    public File findTemplate(File propertyFile, ScanConfiguration config) {
		LOG.debug("C#:findTemplate1");
		LOG.debug("C#:findTemplate2, propertyFile.getName(): " + propertyFile.getName());
        Matcher matcher = LOCALE_PATTERN.matcher(propertyFile.getName());
        if(!matcher.matches())
            return null;
        String prefix = matcher.group(1);
        String suffix = matcher.group(3);
        StringBuilder filename = new StringBuilder(prefix);
        if(config.getMasterLocale()!=null)
        {
            filename.append(".");
            filename.append(config.getMasterLocale().replace('_', '-'));
        }
        filename.append(suffix);
        return new File(propertyFile.getParentFile(),filename.toString());
    }


	@Override
	public Map<Locale, File> findTranslations(File template, ScanConfiguration config) {
		LOG.debug("C#:findTranslations1");
		LOG.debug("C#:findTranslations2, template.getName(): " + template.getName());
        Map<Locale, File> results = new HashMap<Locale, File>();
        Matcher matcher = LOCALE_PATTERN.matcher(template.getName());
        if(matcher.matches())
        {
        	String baseName = matcher.group(1);
        	LOG.debug("C#:findTranslations3, baseName: " + baseName);
        	File folder = template.getParentFile();
        	File[] files = folder.listFiles();
        	if(files==null)
        		return results;
        	for (File file : files) {
        		LOG.debug("C#:findTranslations4, file.getName(): " + file.getName());
        		if(file.equals(template))
        			continue;
        		Matcher fileMatcher = LOCALE_PATTERN.matcher(file.getName());
        		if(fileMatcher.matches())
        		{
        			if(fileMatcher.group(2)!=null && baseName.equals(fileMatcher.group(1)))
        			{
        				String localeString = fileMatcher.group(2).substring(1).replace('-', '_');
        				LOG.debug("C#:findTranslations5, localeString: " + localeString);
        				Locale locale = (Locale) PropertiesFactory.eINSTANCE.createFromString(PropertiesPackage.Literals.LOCALE, localeString);
        				results.put(locale, file);
        			}
        		}
        	}
        }
        return results;
	}

	@Override
	public File computeTranslationPath(File template, Locale templateLocale, Locale translationLocale) {
		LOG.debug("C#:computeTranslationPath1");
		LOG.debug("C#:computeTranslationPath2 + template.getName(): " + template.getName());
        Matcher matcher = LOCALE_PATTERN.matcher(template.getName());
        if (!matcher.matches())
            return null;
        String newName = matcher.group(1) + "." + translationLocale.toString().replace('_', '-') + matcher.group(3);
        
        LOG.debug("C#:computeTranslationPath3 + newName: " + newName);
        
        return new File(template.getParentFile(),newName);
	}

	@Override
	public Locale getLocale(File propertyFile) {
		LOG.debug("C#:getLocale1");
		LOG.debug("C#:getLocale2, propertyFile.getName(): " + propertyFile.getName());
        Matcher matcher = LOCALE_PATTERN.matcher(propertyFile.getName());
        if (matcher.matches()) {
            String actualLocale = matcher.group(2);
            LOG.debug("C#:getLocale3, actualLocale: " + actualLocale);
            if(actualLocale==null || actualLocale.isEmpty())
                return null;
            actualLocale = actualLocale.substring(1).replace('-', '_');
            return (Locale) PropertiesFactory.eINSTANCE.createFromString(PropertiesPackage.Literals.LOCALE, actualLocale);
        }
        return null;

	}

	@Override
	public boolean isBilingual() {
		LOG.debug("C#:isBilingual1");
		return false;
	}

	@Override
	public PropertyConverter createConverter(URI resource) {
		LOG.debug("C#:createConverter1");
		return new org.jabylon.csharp.CSharpConverter(resource, true);
	}

	@Override
	public String getEncoding() {
		LOG.debug("C#:getEncoding1");
		return "UTF-8";
	}
}
