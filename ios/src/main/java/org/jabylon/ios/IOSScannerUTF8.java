/**
 * 
 */
package org.jabylon.ios;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.jabylon.properties.types.PropertyScanner;

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
public class IOSScannerUTF8 extends IOSScanner implements PropertyScanner {

	
	//for iOS see https://developer.apple.com/library/mac/documentation/cocoa/conceptual/loadingresources/Strings/Strings.html
	
	@Property(name=PropertyScanner.TYPE, value="iOS (UTF-8)")
	public  static final String TYPE = "iOS (UTF-8)";

	/* (non-Javadoc)
	 * @see org.jabylon.properties.types.PropertyScanner#getEncoding()
	 */
	@Override
	public String getEncoding() {
		return "UTF-8";
	}

}
