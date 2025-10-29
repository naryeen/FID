package org.openforis.collect.persistence.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.manager.UserPersistenceException;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.User;
import org.openforis.collect.model.UserRole;
import org.openforis.collect.utils.Dates;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class DataHandler extends DefaultHandler {
	
	private static final String NEW_USER_PASSWORD = "password";
	private static final String ATTRIBUTE_VERSION = "version";
	private static final String ATTRIBUTE_MODIFIED_BY = "modified_by";
	private static final String ATTRIBUTE_CREATED_BY = "created_by";
	private static final String ATTRIBUTE_DATE_MODIFIED = "modified";
	private static final String ATTRIBUTE_DATE_CREATED = "created";
	private static final String ATTRIBUTE_STATE = "state";
	private static final String ATTRIBUTE_SYMBOL = "symbol";
	private static final String ATTRIBUTE_REMARKS = "remarks";
	
	private UserManager userManager;

	private CollectRecord record;
	protected Node<?> node;
	protected String field;
	private boolean failed;
	private List<NodeUnmarshallingError> failures;
	private List<NodeUnmarshallingError> warnings;
	private StringBuilder content;
	protected Map<String, String> attributes;
	private CollectSurvey recordSurvey;
	private CollectSurvey currentSurvey;
	private int ignoreLevels;
	private boolean validationEnabled;
	
	private Map<String, User> usersByName;
	
	public DataHandler(CollectSurvey survey) {
		this(null, survey);
	}
	
	public DataHandler(UserManager userManager, CollectSurvey survey) {
		this(userManager, survey, survey);
	}

	public DataHandler(UserManager userManager, CollectSurvey currentSurvey, CollectSurvey recordSurvey) {
		this(userManager, currentSurvey, recordSurvey, true);
	}
	
	public DataHandler(UserManager userManager, CollectSurvey currentSurvey, CollectSurvey recordSurvey, boolean validationEnabled) {
		super();
		this.userManager = userManager;
		this.currentSurvey = currentSurvey;
		this.recordSurvey = recordSurvey;
		this.usersByName = new HashMap<String, User>();
		this.validationEnabled = validationEnabled;
	}

	@Override
	public void startDocument() throws SAXException {
		this.record = null;
		this.node = null;
		this.failed = false;
		this.field = null;
		this.failures = new ArrayList<DataHandler.NodeUnmarshallingError>();
		this.warnings = new ArrayList<DataHandler.NodeUnmarshallingError>();
		this.attributes = null;
		this.ignoreLevels = 0;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		String name = localName.isEmpty() ? qName : localName;
		try {
			if ( failed ) {
				return; 
			} else if ( ignoreLevels > 0 ) {
				pushIgnore();
				return;
			} else if ( node == null ) {
				// if root element, read audit data, version, and 
				startRecord(name, attributes);
			} else {
				this.content = new StringBuilder();
				this.attributes = createAttributesMap(attributes);
				if ( node instanceof Entity ) {
					startChildNode(name, attributes);
				} else if ( node instanceof Attribute ) {
					startAttributeField(name, attributes);
				}
			}
		} catch ( NullPointerException e ) {
			throw e;
		} catch ( RuntimeException e ) {
			if ( node == null ) {
				fail(e+" at root");
			} else { 
				fail(e+" at "+getPath());
			}
		}
	}

	protected String getPath() {
		if ( node == null ) {
			return "root element";
		} else if ( field == null ){
			return node.getPath();
		} else {
			return node.getPath() + "/" + field;			
		}
	}

	public void startRecord(String localName, Attributes attributes) {
		String versionName = extractVersionName(attributes);
		record = new CollectRecord(currentSurvey, versionName, localName, validationEnabled);
		String stateAttr = attributes.getValue(ATTRIBUTE_STATE);
		State state = State.fromCode(stateAttr);
		record.setState(state);

		Date created = Dates.parseDateTime(attributes.getValue(ATTRIBUTE_DATE_CREATED));
		Date modified =  Dates.parseDateTime(attributes.getValue(ATTRIBUTE_DATE_MODIFIED));
		record.setCreationDate(created);
		record.setModifiedDate(modified);

		String createdByUserName = attributes.getValue(ATTRIBUTE_CREATED_BY);
		User createdBy = fetchUser(createdByUserName);
		record.setCreatedBy(createdBy);
		String modifiedByUserName = attributes.getValue(ATTRIBUTE_MODIFIED_BY);
		User modifiedBy = fetchUser(modifiedByUserName);
		record.setModifiedBy(modifiedBy);
		
		node = record.getRootEntity();
	}
	
	private User fetchUser(String name) {
		User user;
		if ( StringUtils.isBlank(name) || userManager == null ) {
			return null;
		} else if ( usersByName.containsKey(name) ) {
			return usersByName.get(name);
		} else {
			user = userManager.loadByUserName(name);
			if ( user == null ) {
				//create a user with data entry role and password equal to the user name
				try {
					user = userManager.insertUser(name, NEW_USER_PASSWORD, UserRole.ENTRY);
				} catch (UserPersistenceException e) {
					throw new RuntimeException("Error creating new user with username '" + name + "'", e);
				}
			}
			usersByName.put(name, user);
			return user;
		}
	}

	protected String extractVersionName(Attributes attributes) {
		String versionName = null;
		String recordVersionName = attributes.getValue(ATTRIBUTE_VERSION);
		if ( StringUtils.isNotBlank(recordVersionName) ) {
			ModelVersion recordVersion = recordSurvey.getVersion(recordVersionName);
			if ( recordVersion == null ) {
				throw new IllegalArgumentException(String.format("Record version with name %s not found in the survey", recordVersionName));
			}
			int versionId = recordVersion.getId();
			ModelVersion version = currentSurvey.getVersionById(versionId);
			if ( version == null ) {
				throw new IllegalArgumentException(String.format("Record version with id %d not found in the current survey", versionId));
			}
			versionName = version.getName();
		}
		return versionName;
	}

	public void startChildNode(String localName, Attributes attributes) {
		Entity entity = (Entity) node;
		NodeDefinition childDefn = getNodeDefinition(entity, localName, attributes);
		if ( childDefn == null ) {
			warn(localName, "Undefined node");
			pushIgnore();
		} else {
			ModelVersion version = record.getVersion();
			if ( version == null || version.isApplicable(childDefn)) {
				Node<?> newNode = childDefn.createNode();
				entity.add(newNode);
				Integer stateValue = getNodeState();
				if ( stateValue != null ) {
					entity.setChildState(localName, stateValue);
				}
				this.node = newNode;
			} else {
				warn(localName, "Node definition is not applicable to the record version");
				pushIgnore();
			}
		}
	}

	private NodeDefinition getNodeDefinition(Entity parentEntity, String localName, Attributes attributes) {
		NodeDefinition newDefn = null;
		EntityDefinition parentEntityDefn = parentEntity.getDefinition();
		Schema originalSchema = recordSurvey.getSchema();
		EntityDefinition originlParentEntityDefn = (EntityDefinition) originalSchema.getDefinitionById(parentEntityDefn.getId());
		NodeDefinition originalDefn = originlParentEntityDefn.getChildDefinition(localName);
		if ( originalDefn != null ) {
			Schema newSchema = currentSurvey.getSchema();
			newDefn = newSchema.getDefinitionById(originalDefn.getId());
		}
		return newDefn;
	}
	
	protected void startAttributeField(String localName, Attributes attributes) {
		this.field = localName;
	}

	protected void pushIgnore() {
		ignoreLevels++;
	}

	protected void warn(String localName, String msg) {
		String path = getPath() + "/" + localName;
		NodeUnmarshallingError nodeErrorItem = new NodeUnmarshallingError(record.getStep(), path, msg);
		warnings.add(nodeErrorItem);
	}

	protected void fail(String msg) {
		String path = getPath();
		Step step = record == null ? null : record.getStep();
		NodeUnmarshallingError nodeErrorItem = new NodeUnmarshallingError(step, path, msg);
		failures.add(nodeErrorItem);
		failed = true;
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ( failed ) {
			return;
		} else if ( ignoreLevels > 0 ) {
			popIgnore();
			return;
		} else if ( node == null ) {
			fail("Reached root node before end of document");
		} else {
			try {
				if ( node instanceof Attribute ) {
					endAttributeElement();
				} else {
					endEntityElement();
				}
				this.content = null;
				if ( node == null ) {
					endRecordElement();
				}
			} catch (NullPointerException e) {
				throw e;
			} catch (RuntimeException e) {
				fail(e+" at "+getPath());
			}
		}
	}

	protected void popIgnore() {
		ignoreLevels--;
	}

	protected void setNode(Node<?> node) {
		this.node = node;
	}
	
	protected void endRecordElement() {
		this.record.updateSummaryFields();
	}
	
	protected void endEntityElement() {
		Node<?> parent = node.getParent();
		removeIfEmpty(node);
		this.node = parent;
	}

	@SuppressWarnings({ "rawtypes" })
	protected void endAttributeElement() {
		Attribute attr = (Attribute) node;
		try {
			if (field != null) {
				Field<?> fld = getField();
				if ( fld != null ) {
					setField(fld);
				} else {
					warn(field, "Can't parse field with type "+attr.getClass().getSimpleName());
					//this.node = node.getParent();
				}
			}
		} catch (NumberFormatException e) {
			warn(field, e.toString());
		}
		if ( field == null ) {
			Node<?> oldNode = node;
			this.node = node.getParent();
			removeIfEmpty(oldNode);
		} else {
			this.field = null;
		}
	}

	private Field<?> getField() {
		Attribute<?, ?> attr = (Attribute<?, ?>) node;
		Field<?> fld;
		if ( attr instanceof FileAttribute ) {
			//backwards compatibility
			if ( field.equals("fileName") ) {
				fld = ((FileAttribute) attr).getFilenameField();
			} else if ( field.equals("fileSize") ) {
				fld = ((FileAttribute) attr).getSizeField();
			} else {
				fld = attr.getField(field);
			}
		} else {
			fld = attr.getField(field);
		}
		return fld;
	}

	protected void removeIfEmpty(Node<?> node) {
		if ( node != null && ! node.hasData() && node.getParent() != null ) {
			//if node is empty, remove it
			node.getParent().remove(node.getDefinition(), node.getIndex());
		}
	}
	
	protected String getXmlValue() {
		return content == null ? null : content.toString().trim();
	}
	
	protected void setXmlValue(String content) {
		this.content = new StringBuilder(content);
	}
	
	protected Node<?> getNode() {
		return node;
	}
	
	protected void setField(Field<?> fld) {
		String value = getXmlValue();
		fld.setValueFromString(value);
		String remarks = attributes.get(ATTRIBUTE_REMARKS);
		fld.setRemarks(remarks);
		String s = attributes.get(ATTRIBUTE_SYMBOL);
		if ( StringUtils.isNotBlank(s) ) {
			char c = s.charAt(0);
			FieldSymbol fs = FieldSymbol.valueOf(c);
			if ( fs != null ) {
				fld.setSymbol(fs.getCode());
			}
		}
		Integer stateValue = getNodeState();
		if ( stateValue != null && stateValue > 0 ) {
			fld.getState().set(stateValue);
		}
		fld.getAttribute().updateSummaryInfo();
	}

	private Integer getNodeState() {
		String state = attributes.get(ATTRIBUTE_STATE);
		int stateInt = 0;
		if ( state != null) {
			stateInt = Integer.parseInt(state);
			return stateInt;
		} else {
			return null;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if ( content != null && node instanceof Attribute ) {
			content.append(ch, start, length);
		}
	}
	
	protected Map<String, String> createAttributesMap(Attributes attributes) {
		HashMap<String, String> result = new HashMap<String, String>();
		int length = attributes.getLength();
		for ( int i = 0; i < length; i++) {
			String qName = attributes.getQName(i);
			String value = attributes.getValue(i);
			result.put(qName, value);
		}
		return result;
	}

	public CollectRecord getRecord() {
		return record;
	}

	public CollectSurvey getRecordSurvey() {
		return recordSurvey;
	}
	
	public List<NodeUnmarshallingError> getFailures() {
		return CollectionUtils.unmodifiableList(failures);
	}
	
	public List<NodeUnmarshallingError> getWarnings() {
		return CollectionUtils.unmodifiableList(warnings);
	}
	
	public static class NodeUnmarshallingError {
		
		private Step step;
		private String path;
		private String message;
		
		public NodeUnmarshallingError(String message) {
			this.message = message;
		}
		
		public NodeUnmarshallingError(Step step, String path, String message) {
			this(message);
			this.step = step;
			this.path = path;
		}
		
		public Step getStep() {
			return step;
		}
		
		public void setStep(Step step) {
			this.step = step;
		}
		
		public String getPath() {
			return path;
		}
		
		public void setPath(String path) {
			this.path = path;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		
		@Override
		public String toString() {
			return "Node: " + path + " message: " + message;
		}

	}
}
