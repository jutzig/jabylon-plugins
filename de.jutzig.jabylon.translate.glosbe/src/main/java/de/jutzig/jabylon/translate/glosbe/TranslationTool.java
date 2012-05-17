/**
 * 
 */
package de.jutzig.jabylon.translate.glosbe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.URI;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import de.jutzig.jabylon.properties.PropertyFileDescriptor;
import de.jutzig.jabylon.properties.Review;
import de.jutzig.jabylon.ui.container.PropertyPairContainer.PropertyPairItem;
import de.jutzig.jabylon.ui.styles.JabylonStyle;
import de.jutzig.jabylon.ui.tools.PropertyEditorTool;
import de.jutzig.jabylon.ui.tools.SuggestionAcceptor;

/**
 * @author Johannes Utzig (jutzig.dev@googlemail.com)
 *
 */
public class TranslationTool implements PropertyEditorTool, TextChangeListener {

	
	private String source, destination;
	private BeanItemContainer<TranslationResult> container;
	private static final Pattern PATTERN = Pattern.compile(".*?\"phrase\":\\{\"text\":\"(.+?)\"");
	
	/**
	 * 
	 */
	public TranslationTool() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see de.jutzig.jabylon.ui.tools.PropertyEditorTool#init(de.jutzig.jabylon.properties.PropertyFileDescriptor, de.jutzig.jabylon.properties.PropertyFileDescriptor)
	 */
	@Override
	public void init(PropertyFileDescriptor template, PropertyFileDescriptor translation) {
		source = template.getVariant()==null ? "eng" : template.getVariant().getISO3Language();
		destination = translation.getVariant()==null ? "eng" : translation.getVariant().getISO3Language();

	}
	
	/* (non-Javadoc)
	 * @see de.jutzig.jabylon.ui.tools.PropertyEditorTool#createComponent()
	 */
	@Override
	public Component createComponent() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		TextField field = new TextField();
		field.setWidth(100,TextField.UNITS_PERCENTAGE);
		field.setInputPrompt("translate");
		field.addListener(this);
		field.setImmediate(true);
		layout.addComponent(field);
		Table table = new Table();
		layout.addComponent(table);
		table.addStyleName(JabylonStyle.TABLE_STRIPED.getCSSName());
		table.setSizeFull();
		container = new BeanItemContainer<TranslationResult>(TranslationResult.class);
//		container.addContainerProperty("source", String.class, "");
//		container.addContainerProperty("target", String.class, "");
		table.setContainerDataSource(container);
		table.setVisibleColumns(new String[]{"source","target"});
		return layout;
	}

	@Override
	public void selectionChanged(PropertyPairItem currentSelection, Collection<Review> reviews, SuggestionAcceptor acceptor) {
//		String value = currentSelection.getSourceProperty().getValue();
//		String[] words = value.split("\\s");
//		container.removeAllItems();
//		List<TranslationResult> results = new ArrayList<TranslationResult>();
//		for (String string : words) {
//			URI uri = buildURI(string);
//			String result = evaluate(uri);
//			if(result==null)
//				continue;
//			Matcher matcher = PATTERN.matcher(result);
//			if(matcher.find())
//			{
//				TranslationResult tuple = new TranslationResult(string, matcher.group(1));
//				results.add(tuple);
//			}
//		}
//		container.addAll(results);
////		uri.appendQuery(query)
		
	}

	private String evaluate(URI uri) {
		InputStream stream = null;
		try {
			URL url = new URL(uri.toString());
			stream = url.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line = reader.readLine();
			return line;
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(stream!=null)
				try {
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return null;
	}

	private URI buildURI(String phrase) {
		URI uri = URI.createURI("http://glosbe.com/gapi/translate");
		phrase = URI.encodeQuery(phrase, false);
		String query = "from={0}&dest={1}&format=json&phrase={2}";
		query = MessageFormat.format(query, source,destination,phrase);
		
		return uri.appendQuery(query);
	}

	public static class TranslationResult
	{
		String source;
		String target;
		
		
		
		public TranslationResult(String source, String target) {
			super();
			this.source = source;
			this.target = target;
		}
		
		public String getSource() {
			return source;
		}
		
		public String getTarget() {
			return target;
		}
	}

	@Override
	public void textChange(TextChangeEvent event) {
		URI uri = buildURI(event.getText());
		container.removeAllItems();
		String result = evaluate(uri);
		Matcher matcher = PATTERN.matcher(result);
		if(matcher.find())
		{
			TranslationResult tuple = new TranslationResult(event.getText(), matcher.group(1));
			container.addItem(tuple);
		}

		
	}
}

