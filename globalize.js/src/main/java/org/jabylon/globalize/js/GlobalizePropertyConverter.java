package org.jabylon.globalize.js;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.jabylon.properties.PropertiesFactory;
import org.jabylon.properties.Property;
import org.jabylon.properties.PropertyFile;
import org.jabylon.properties.types.PropertyConverter;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class GlobalizePropertyConverter implements PropertyConverter {

	private URI resource;

	public GlobalizePropertyConverter(URI resource) {
		this.resource = resource;
	}

	@Override
	public PropertyFile load(InputStream in, String encoding) throws IOException {
		JsonValue json = Json.parse(new InputStreamReader(in, encoding));
		JsonObject root = json.asObject();
		List<String> names = root.names();
		PropertyFile propertyFile = PropertiesFactory.eINSTANCE.createPropertyFile();
		for (String locale : names) {
			loadLanguage(root.get(locale),propertyFile, "");
		}
		return propertyFile;
	}

	private void loadLanguage(JsonValue node, PropertyFile propertyFile, String prefix) {
		JsonObject language = node.asObject();
		List<String> keys = language.names();
		for (String key : keys) {
			String fullKey = prefix+key;
			JsonValue jsonKey = language.get(key);
			if(jsonKey.isString()) {
				Property property = PropertiesFactory.eINSTANCE.createProperty();
				property.setKey(fullKey);
				property.setValue(jsonKey.asString());
				propertyFile.getProperties().add(property);
			}
			else if(jsonKey.isObject()) {
				loadLanguage(jsonKey.asObject(), propertyFile, fullKey+"/");
			}
		}
		
	}

	@Override
	public int write(OutputStream out, PropertyFile file, String encoding) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
