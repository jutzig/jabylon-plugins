/**
 *
 */
package org.jabylon.team.cvs.config;

import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.eclipse.emf.common.util.URI;
import org.jabylon.team.cvs.impl.CVSConstants;
import org.osgi.service.prefs.Preferences;

import org.jabylon.properties.Project;
import org.jabylon.properties.PropertiesPackage;
import org.jabylon.rest.ui.model.EObjectPropertyModel;
import org.jabylon.rest.ui.model.PreferencesPropertyModel;

/**
 * @author Johannes Utzig (jutzig.dev@googlemail.com)
 *
 */
public class CVSConfigPanel extends GenericPanel<Project> {

	private static final long serialVersionUID = 1L;

	public CVSConfigPanel(String id, IModel<Project> model, Preferences config) {
		super(id, model);
		EObjectPropertyModel<URI, Project> repositoryURI = new EObjectPropertyModel<URI, Project>(model, PropertiesPackage.Literals.PROJECT__REPOSITORY_URI);
		TextField<URI> uriField = new TextField<URI>("inputURI", repositoryURI);
		uriField.setType(URI.class);
		uriField.setConvertEmptyInputStringToNull(true);
		add(uriField);

		PreferencesPropertyModel moduleModel = new PreferencesPropertyModel(config, CVSConstants.KEY_MODULE, "");
		add(new TextField<String>("inputModule",moduleModel));

		PreferencesPropertyModel usernameModel = new PreferencesPropertyModel(config, CVSConstants.KEY_USERNAME, "");
		add(new TextField<String>("inputUsername",usernameModel));
		PreferencesPropertyModel passwordModel = new PreferencesPropertyModel(config, CVSConstants.KEY_PASSWORD, "");
		PasswordTextField passwordTextField = new PasswordTextField("inputPassword",passwordModel);
		passwordTextField.setResetPassword(false);
		passwordTextField.setRequired(false);
		add(passwordTextField);

	}

}
