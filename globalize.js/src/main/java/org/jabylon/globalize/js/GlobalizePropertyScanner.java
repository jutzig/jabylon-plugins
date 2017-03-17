/**
 * 
 */
package org.jabylon.globalize.js;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.emf.common.util.URI;
import org.jabylon.properties.ScanConfiguration;
import org.jabylon.properties.types.PropertyConverter;
import org.jabylon.properties.types.PropertyScanner;
import org.jabylon.properties.types.impl.AbstractPropertyScanner;

@Component(enabled=true,immediate=true)
@Service
public class GlobalizePropertyScanner extends AbstractPropertyScanner {

	@Property(name=PropertyScanner.TYPE, value="Globalize.js")
	public  static final String TYPE = "Globalize.js";	
	
	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#findTemplate(java.io.File, org.jabylon.properties.ScanConfiguration)
	 */
	@Override
	public File findTemplate(File propertyFile, ScanConfiguration config) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#findTranslations(java.io.File, org.jabylon.properties.ScanConfiguration)
	 */
	@Override
	public Map<Locale, File> findTranslations(File template, ScanConfiguration config) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#computeTranslationPath(java.io.File, java.util.Locale, java.util.Locale)
	 */
	@Override
	public File computeTranslationPath(File template, Locale templateLocale, Locale translationLocale) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#getLocale(java.io.File)
	 */
	@Override
	public Locale getLocale(File propertyFile) {
		// TODO Auto-generated method stub
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
		return new GlobalizePropertyConverter(resource);
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#getDefaultIncludes()
	 */
	@Override
	public String[] getDefaultIncludes() {
		return new String[]{"**/*.json"};
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#getDefaultExcludes()
	 */
	@Override
	public String[] getDefaultExcludes() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#getEncoding()
	 */
	@Override
	public String getEncoding() {
		// TODO Auto-generated method stub
		return "UTF-8";
	}

}
