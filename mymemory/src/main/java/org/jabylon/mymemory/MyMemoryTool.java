package org.jabylon.mymemory;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jabylon.rest.ui.model.PropertyPair;
import org.jabylon.rest.ui.tools.PropertyEditorTool;

/**
 * @author Johannes Utzig (jutzig.dev@googlemail.com)
 *
 */
@Component
@Service(value=PropertyEditorTool.class)
public class MyMemoryTool implements PropertyEditorTool {

    private static final long serialVersionUID = 1L;
    
    static final int PRECEDENCE = 95;

    /* (non-Javadoc)
     * @see org.jabylon.rest.ui.wicket.PanelFactory#createPanel(org.apache.wicket.request.mapper.parameter.PageParameters, org.apache.wicket.model.IModel, java.lang.String)
     */
    @Override
    public Panel createPanel(PageParameters params, IModel<PropertyPair> input, String id) {
        return new MyMemoryToolPanel(id, input);
    }

    @Override
    public String getName() {
        return "%mymemory.name";
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE;
    }

}
