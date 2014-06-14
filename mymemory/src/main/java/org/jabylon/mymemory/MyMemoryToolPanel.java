/**
 * (C) Copyright 2013 Jabylon (http://www.jabylon.org) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jabylon.mymemory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.common.util.URI;
import org.jabylon.properties.PropertyFileDescriptor;
import org.jabylon.rest.ui.Activator;
import org.jabylon.rest.ui.model.PropertyPair;
import org.jabylon.rest.ui.wicket.BasicPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Johannes Utzig (jutzig.dev@googlemail.com)
 */
public class MyMemoryToolPanel extends BasicPanel<PropertyPair> {

	private static final long serialVersionUID = 1L;
	private static Logger LOG = LoggerFactory.getLogger(MyMemoryToolPanel.class);

	/**
	 * copies similar strings to translation area
	 */
	private static final String JS = "$(\"#similarity-table i.icon-share\").click(function () { " + "var translation = $(this).prev(\"span\");"
			+ "var widget = $(\"#translation\");" + "if(widget.attr(\"readonly\")!=='readonly') {" + "widget.val(translation.text());" + "markDirty()};"
			+ "});";

	public MyMemoryToolPanel(String id, IModel<PropertyPair> model) {
		super(id, model);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript(JS));
	}
	
	@Override
	protected void construct() {
		super.construct();
        List<Suggestion> result = createSuggestions(sendRequest(getModelObject(),getTemplateLocale(getModelObject())));
        ListView<Suggestion> list = new ListView<Suggestion>("children", result)
        {

            private static final long serialVersionUID = 1L;


            @Override
            protected void populateItem(ListItem<Suggestion> item)
            {
            	Suggestion similarity = item.getModelObject();
                item.add(new Label("template", similarity.getOriginal()));
                item.add(new Label("translation", similarity.getTranslation()));
                WebMarkupContainer progress = new WebMarkupContainer("similarity");
                item.add(progress);
                progress.add(new AttributeModifier("style", "width: " + similarity.getQuality() + "%"));

            }
        };
        add(list);
	}

    private Locale getTemplateLocale(PropertyPair pair)
    {
        try
        {
            CDOObject object = Activator.getDefault().getRepositoryLookup().resolve(pair.getDescriptorID());
            if (object instanceof PropertyFileDescriptor)
            {
                PropertyFileDescriptor descriptor = (PropertyFileDescriptor)object;
                return descriptor.getProjectLocale().getParent().getTemplate().getLocale();
            }
        }
        catch (Exception e)
        {
            LOG.error("Failed to lookup project for "+pair);
        }
        return Locale.ENGLISH;
    }
	
	protected List<Suggestion> createSuggestions(JSONObject o){
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		if(o==null)
			return suggestions;
		try {
			JSONArray matches = o.getJSONArray("matches");
			for(int i=0;i<matches.length();i++) {
				try {
					JSONObject json = matches.getJSONObject(i);
					Suggestion s = new Suggestion(json.getString("segment"), json.getString("translation"), json.getInt("quality"),i);
					suggestions.add(s);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return suggestions;
	}
	
	protected JSONObject sendRequest(PropertyPair pair, Locale masterLocale) {
		URI uri = URI.createURI("http://api.mymemory.translated.net/get");
		String query = "q={0}&langpair={1}|{2}";
		String template = masterLocale != null ? masterLocale.getLanguage() : "en";
		String translationLanguage = pair.getLanguage() != null ? pair.getLanguage().getLanguage() : "en";
		String text = URI.encodeQuery(pair.getOriginal(), false);
		query = MessageFormat.format(query, text, template, translationLanguage);

		uri = uri.appendQuery(query);
		try {
			URL url = new URL(uri.toString());
			URLConnection connection = url.openConnection();
			if (connection instanceof HttpURLConnection) {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				if (httpConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
					String charset = httpConnection.getContentEncoding() == null ? "UTF-8" : httpConnection.getContentEncoding();
					BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName(charset)));
					StringBuilder builder = new StringBuilder(connection.getContentLength());
					char[] buffer = new char[1024];
					int read = 0;
					while (read >= 0) {
						read = reader.read(buffer);
						if(read>0)
								builder.append(buffer, 0, read);
					}
					System.out.println(builder.toString());
					return new JSONObject(builder.toString());
				} else
					LOG.error("Request returned " + httpConnection.getResponseCode() + " : " + httpConnection.getResponseMessage());

			}

		} catch (MalformedURLException e) {
			LOG.error("invalid request", e);
		} catch (IOException e) {
			LOG.error("Failed to read response", e);
		} catch (JSONException e) {
			LOG.error("Failed to parse json response", e);
		}
		return null;
	}

	public static class Suggestion implements Serializable, Comparable<Suggestion> {

		private static final long serialVersionUID = 1L;
		private String original;
		private String translation;
		private int quality;
		private String key;
		/** the order number is to make sure compareTo never returns 0 */
		private int orderNumber;

		public Suggestion(String original, String translation, int quality, int orderNumber) {
			super();
			this.original = original;
			this.translation = translation;
			this.quality = quality;
			this.orderNumber = orderNumber;
		}

		public String getKey() {
			return key;
		}

		public String getOriginal() {
			return original;
		}

		public String getTranslation() {
			return translation;
		}

		public int getQuality() {
			return quality;
		}

		@Override
		public int compareTo(Suggestion o) {
			int result = o.getQuality() - getQuality();
			if (result == 0) {
				if (equals(o))
					return 0;
				return orderNumber - o.orderNumber;
			}
			return result;
		}

	}
}
