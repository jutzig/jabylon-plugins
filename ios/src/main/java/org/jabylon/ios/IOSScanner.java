/**
 * 
 */
package org.jabylon.ios;

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
 * adds support for the iOS properties format. See
 * https://developer.apple.com/library/mac/documentation/cocoa/conceptual/loadingresources/Strings/Strings.html
 * <p>
 * iOS seems a bit limited regarding locale support, this implementation is therefore not strict enough for iOS
 * http://www.ibabbleon.com/iOS-Language-Codes-ISO-639.html
 * 
 * @author jutzig.dev@googlemail.com
 *
 */
@Component(enabled=true,immediate=true)
@Service
public class IOSScanner extends AbstractPropertyScanner implements PropertyScanner {

	
	//for iOS see https://developer.apple.com/library/mac/documentation/cocoa/conceptual/loadingresources/Strings/Strings.html
	
	@Property(name=PropertyScanner.TYPE, value="iOS")
	public  static final String TYPE = "iOS";
	/** example de.lproj */
    private static final Pattern LOCALE_PATTERN = Pattern.compile("(?:(\\w\\w)\\.)(.+?)");
    private static final String[] DEFAULT_EXCLUDES = {};
    private static final String[] DEFAULT_INCLUDES = {"**/*.strings"};
			

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#findTemplate(java.io.File, org.jabylon.properties.ScanConfiguration)
	 */
	@Override
	public File findTemplate(File propertyFile, ScanConfiguration config) {
		File folder = propertyFile.getParentFile();
		File container = folder.getParentFile();
		Matcher matcher = LOCALE_PATTERN.matcher(folder.getName());
		if(matcher.matches()){
			String baseName = matcher.group(2);
			String masterLocale = config.getMasterLocale();
			String folderName = baseName; 
			if(masterLocale!=null && !masterLocale.isEmpty())
				folderName = toIOSPrefix((Locale) PropertiesFactory.eINSTANCE.createFromString(PropertiesPackage.Literals.LOCALE, masterLocale)) + folderName;
			return new File(new File(container,folderName),propertyFile.getName());			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#findTranslations(java.io.File, org.jabylon.properties.ScanConfiguration)
	 */
	@Override
	public Map<Locale, File> findTranslations(File template, ScanConfiguration config) {
		Map<Locale, File> results = new HashMap<Locale, File>();
		File templateFolder = template.getParentFile();
		File container = templateFolder.getParentFile();
		File[] files = container.listFiles();
		for (File folder : files) {
			if(folder.getName().equals(templateFolder.getName()))
				continue;
			Locale locale = getLocaleFromFolder(folder);
			if(locale==null)
				continue;
			results.put(locale, new File(folder,template.getName()));
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#computeTranslationPath(java.io.File, java.util.Locale, java.util.Locale)
	 */
	@Override
	public File computeTranslationPath(File template, Locale templateLocale, Locale translationLocale) {
		File folder = template.getParentFile();
		File container = folder.getParentFile();
		Matcher matcher = LOCALE_PATTERN.matcher(folder.getName());
		if (!matcher.matches())
			return null;
		String baseName = matcher.group(2);
		baseName = toIOSPrefix(translationLocale) + baseName;
		return new File(new File(container,baseName),template.getName());
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#getLocale(java.io.File)
	 */
	@Override
	public Locale getLocale(File propertyFile) {
		File folder = propertyFile.getParentFile();
		return getLocaleFromFolder(folder);
	}
	
	public Locale getLocaleFromFolder(File propertyFolder) {
		Matcher matcher = LOCALE_PATTERN.matcher(propertyFolder.getName());
		if(matcher.matches()) {
			String language = null;
			int count = matcher.groupCount();
			if(count>1)
				language = matcher.group(1);
			if(language!=null)
				return new Locale(language);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#isBilingual()
	 */
	@Override
	public boolean isBilingual() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#createConverter(org.eclipse.emf.common.util.URI)
	 */
	@Override
	public PropertyConverter createConverter(URI resource) {
		return new IOSConverter(true,resource);
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#getDefaultIncludes()
	 */
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

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#getEncoding()
	 */
	@Override
	public String getEncoding() {
		return "UTF-16";
	}
	
	private String toIOSPrefix(Locale locale) {
		if(locale==null)
			return "";
		StringBuilder builder = new StringBuilder();
		builder.append(locale.getLanguage());
		builder.append(".");
		return builder.toString();
	}

}
