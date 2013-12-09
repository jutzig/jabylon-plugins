package org.jabylon.team.cvs.impl.config;



/**
 * @author Johannes Utzig (jutzig.dev@googlemail.com)
 *
 */
public class CVSConfigSection //extends AbstractConfigSection<Project> implements Adapter{
{
	
//	private Form form;
//	
//	@Override
//	public Component createContents() {
//		form = new Form();
//		form.setImmediate(true);
//		form.setCaption("CVS Settings");
//		form.setWriteThrough(true);
//		form.setFormFieldFactory(new DefaultFieldFactory() {
//			@Override
//			public Field createField(Item item, Object propertyId, Component uiContext) {
//				if(propertyId.equals(CVSConstants.KEY_PASSWORD))
//				{
//					PasswordField field = new PasswordField();
//					field.setCaption("Password");
//					field.setNullRepresentation("");
//					return field;
//				}
//				
//				Field field = super.createField(item, propertyId, uiContext);
//				if(propertyId.equals(CVSConstants.KEY_USERNAME))
//				{
//					field.setCaption("Username");
//					if (field instanceof TextField) {
//						TextField text = (TextField) field;
//						text.setNullRepresentation("");
//						
//					}
//				}
//				if(propertyId.equals(CVSConstants.KEY_MODULE))
//				{
//					field.setCaption("CVS Module");
//					if (field instanceof TextField) {
//						TextField text = (TextField) field;
//						text.setNullRepresentation("");
//						
//					}
//					field.setRequired(true);
//				}
//				return field; 
//			}
//		});
//		return form;
//	}
//
//	@Override
//	public void commit(Preferences config) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	protected void init(Preferences config) {
//		getDomainObject().eAdapters().add(new WeakReferenceAdapter(this));
//		form.setVisible(cvsSelected());
//		PreferencesItem item = new PreferencesItem(config);
//		item.addProperty(CVSConstants.KEY_USERNAME, String.class, null);
//		item.addProperty(CVSConstants.KEY_PASSWORD, String.class, null);
//		item.addProperty(CVSConstants.KEY_MODULE, String.class, null);
//		form.setItemDataSource(item);
//		
//		
//	}
//
//	@Override
//	public void notifyChanged(Notification notification) {
//		form.setVisible(cvsSelected());
//		
//	}
//
//	private boolean cvsSelected() {
//		return "CVS".equals(getDomainObject().getTeamProvider());
//	}
//
//	@Override
//	public Notifier getTarget() {
//		return getDomainObject();
//	}
//
//	@Override
//	public void setTarget(Notifier newTarget) {
//		
//	}
//
//	@Override
//	public boolean isAdapterForType(Object type) {
//		return false;
//	}
//

}
