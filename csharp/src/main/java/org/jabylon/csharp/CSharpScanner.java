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

    /**
     * group1: basename
     * group2: locale (optional)
     * group3: extension
     *
     */
    static final Pattern LOCALE_PATTERN = Pattern.compile("(.+?)((?:\\.\\p{Alpha}+)(?:-\\p{Alpha}+)?)?(\\.resx)");

	public CSharpScanner() {
	}


	@Override
	public String[] getDefaultIncludes() {
		return DEFAULT_INCLUDES;
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#getDefaultExcludes()
	 */
	@Override
	public String[] getDefaultExcludes() {
		return DEFAULT_EXCLUDES;
	}

    @Override
    public File findTemplate(File propertyFile, ScanConfiguration config) {
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
        Map<Locale, File> results = new HashMap<Locale, File>();
        Matcher matcher = LOCALE_PATTERN.matcher(template.getName());
        if(matcher.matches())
        {
        	String baseName = matcher.group(1);
        	File folder = template.getParentFile();
        	File[] files = folder.listFiles();
        	if(files==null)
        		return results;
        	for (File file : files) {
        		if(file.equals(template))
        			continue;
        		Matcher fileMatcher = LOCALE_PATTERN.matcher(file.getName());
        		if(fileMatcher.matches())
        		{
        			if(fileMatcher.group(2)!=null && baseName.equals(fileMatcher.group(1)))
        			{
        				String localeString = fileMatcher.group(2).substring(1).replace('-', '_');
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
        Matcher matcher = LOCALE_PATTERN.matcher(template.getName());
        if (!matcher.matches())
            return null;
        String newName = matcher.group(1) + "." + translationLocale.toString().replace('_', '-') + matcher.group(3);
        return new File(template.getParentFile(),newName);
	}

	@Override
	public Locale getLocale(File propertyFile) {
        Matcher matcher = LOCALE_PATTERN.matcher(propertyFile.getName());
        if (matcher.matches()) {
            String actualLocale = matcher.group(2);
            if(actualLocale==null || actualLocale.isEmpty())
                return null;
            actualLocale = actualLocale.substring(1).replace('-', '_');
            return (Locale) PropertiesFactory.eINSTANCE.createFromString(PropertiesPackage.Literals.LOCALE, actualLocale);
        }
        return null;

	}

	@Override
	public boolean isBilingual() {
		return false;
	}

	@Override
	public PropertyConverter createConverter(URI resource) {
		return new CSharpConverter(resource, true);
	}

	@Override
	public String getEncoding() {
		return "UTF-8";
	}
}
