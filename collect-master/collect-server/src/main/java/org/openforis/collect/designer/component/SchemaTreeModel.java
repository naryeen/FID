/**
 * 
 */
package org.openforis.collect.designer.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.openforis.collect.designer.component.SchemaTreeModel.SchemaNodeData;
import org.openforis.collect.designer.metamodel.NodeType;
import org.openforis.collect.designer.viewmodel.SchemaVM;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.model.SurveySummary;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.SurveyObject;
import org.zkoss.bind.BindUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.TreeNode;

/**
 * @author S. Ricci
 *
 */
public class SchemaTreeModel extends BasicTreeModel<SchemaNodeData> {
	
	private static final long serialVersionUID = 1L;
	
	protected SurveyObjectTreeModelCreator modelCreator;
	protected EntityDefinition rootEntity;
	protected String labelLanguage;
	
	public SchemaTreeModel(SurveyObjectTreeModelCreator modelCreator, 
			SchemaTreeNode root, EntityDefinition rootEntity, String labelLanguage) {
		super(root);
		this.modelCreator = modelCreator;
		this.rootEntity = rootEntity;
		this.labelLanguage = labelLanguage;
	}
	
	@Override
	protected SchemaTreeNode createNode(SchemaNodeData data, boolean defineEmptyChildrenForLeaves) {
		return (SchemaTreeNode) modelCreator.createNode((SchemaNodeData) data, defineEmptyChildrenForLeaves);
	}
	
	@Override
	public int getChildCount(TreeNode<SchemaNodeData> parent) {
		int count = super.getChildCount(parent);
		SchemaNodeData data = parent.getData();
		if (data != null && data.getSurveyObject() == null) {
			return 1;
		} else {
			return count;
		}
	}

	@Override
	public boolean isLeaf(TreeNode<SchemaNodeData> node) {
		if (node.getData() != null && node.getData().getSurveyObject() == null) {
			return false;
		} else {
			return super.isLeaf(node);
		}
	}
	
	@Override
	public TreeNode<SchemaNodeData> getChild(TreeNode<SchemaNodeData> parent, int index) {
		SchemaNodeData data = parent.getData();
		if (data != null && data.getSurveyObject() == null) {
			if (parent.getChildCount() == 0) {
				AbstractNode<SchemaNodeData> rootEntityNode = modelCreator.createSurveyRootEntityNode(data.getSurvey().getId());
				parent.add(rootEntityNode);
			}
		}
		return super.getChild(parent, index);
	}
	
	public SchemaNodeData getNodeData(SurveyObject surveyObject) {
		int[] path = getNodePath(surveyObject);
		return getNodeData(path);
	}
	
	public EntityDefinition getNearestParentEntityDefinition(SurveyObject surveyObject) {
		TreeNode<SchemaNodeData> treeNode = getTreeNode(surveyObject);
		TreeNode<SchemaNodeData> parentNode = treeNode.getParent();
		while ( parentNode != null && parentNode.getData() != null ) {
			SchemaNodeData data = parentNode.getData();
			SurveyObject currentSurveyObject = data.getSurveyObject();
			if ( currentSurveyObject instanceof EntityDefinition ) {
				return (EntityDefinition) currentSurveyObject;
			}
			parentNode = parentNode.getParent();
		}
		//if not found, return root entity
		return rootEntity;
	}

	private SchemaNodeData getNodeData(int[] path) {
		if ( path == null ) {
			return null;
		} else {
			SchemaTreeNode node = (SchemaTreeNode) getChild(path);
			SchemaNodeData data = node.getData();
			return data;
		}
	}
	
	protected int[] getNodePath(SurveyObject surveyObject) {
		TreeNode<SchemaNodeData> treeNode = getTreeNode(surveyObject);
		if ( treeNode == null ) {
			return null;
		} else {
			int[] result = super.getPath(treeNode);
			return result;
		}
	}
	
	public SchemaTreeNode getTreeNode(SurveyObject surveyObject) {
		SchemaTreeNode root = (SchemaTreeNode) getRoot();
		Stack<TreeNode<SchemaNodeData>> stack = new Stack<TreeNode<SchemaNodeData>>();
		stack.push(root);
		while ( ! stack.isEmpty() ) {
			TreeNode<SchemaNodeData> treeNode = stack.pop();
			SchemaNodeData treeNodeData = treeNode.getData();
			if ( treeNodeData != null && treeNodeData.getSurveyObject() == surveyObject ) {
				return (SchemaTreeNode) treeNode;
			}
			List<TreeNode<SchemaNodeData>> children = treeNode.getChildren();
			if ( children != null && children.size() > 0 ) {
				for (TreeNode<SchemaNodeData> child : children) {
					stack.push(child);
				}
			}
		}
		return null;
	}

	public SchemaNodeData select(SurveyObject surveyObject) {
		SchemaNodeData data = getNodeData(surveyObject);
		super.select(data);
		return data;
	}
	
	public void showSelectedNode() {
		AbstractNode<SchemaNodeData> selectedNode = getSelectedNode();
		if ( selectedNode != null ) {
			TreeNode<SchemaNodeData> parent = selectedNode.getParent();
			while ( parent != null ) {
				addOpenObject(parent);
				parent = parent.getParent();
			}
		}
	}

	public void updateNodeLabel(SurveyObject surveyObject, String label) {
		SchemaNodeData data = getNodeData(surveyObject);
		data.setLabel(label);
		BindUtils.postNotifyChange(null, null, data, "label");
	}
	
	public void appendNodeToSelected(SurveyObject surveyObject) {
		appendNodeToSelected(surveyObject, false);
	}
	
	public void appendNodeToSelected(SurveyObject surveyObject, boolean detached) {
		AbstractNode<?> selectedNode = getSelectedNode();
		boolean root = selectedNode == null;
		SchemaNodeData data = new SchemaNodeData(surveyObject, root, detached, labelLanguage);
		super.appendNodeToSelected(data);
	}
	
	public void setSelectedNodeLabel(String label) {
		AbstractNode<SchemaNodeData> selectedNode = getSelectedNode();
		if ( selectedNode != null ) {
			SchemaNodeData data = selectedNode.getData();
			data.setLabel(label);
			BindUtils.postNotifyChange(null, null, data, "label");
		}
	}
	
	public void markSelectedNodeAsDetached() {
		AbstractNode<SchemaNodeData> selectedNode = getSelectedNode();
		SchemaNodeData data = selectedNode.getData();
		data.setDetached(true);
	}

	public List<SurveyObject> getSiblingsAndSelf(SurveyObject obj, boolean sameType) {
		List<SurveyObject> result = new ArrayList<SurveyObject>();
		TreeNode<SchemaNodeData> treeNode = getTreeNode(obj);
		TreeNode<SchemaNodeData> parent = treeNode.getParent();
		List<TreeNode<SchemaNodeData>> children = parent.getChildren();
		for (TreeNode<SchemaNodeData> child : children) {
			SurveyObject surveyObject = ((SchemaNodeData) child.getData()).getSurveyObject();
			if ( sameType && (
					(obj instanceof UITab && surveyObject instanceof UITab)
					||
					(obj instanceof NodeDefinition && surveyObject instanceof NodeDefinition)
				) ) {
				result.add(surveyObject);
			}
		}
		return result;
	}
	
	public Set<SurveyObject> getOpenSchemaNodes() {
		Set<SurveyObject> result = new HashSet<SurveyObject>();
		Set<TreeNode<SchemaNodeData>> openObjects = getOpenObjects();
		for (TreeNode<SchemaNodeData> treeNode : openObjects) {
			if (treeNode != null) {
				SchemaNodeData data = (SchemaNodeData) treeNode.getData();
				SurveyObject node = data.getSurveyObject();
				result.add(node);
			}
		}
		return result;
	}
	
	public void setOpenSchemaNodes(Collection<SurveyObject> nodes) {
		Set<TreeNode<SchemaNodeData>> opened = new HashSet<TreeNode<SchemaNodeData>>();
		for (SurveyObject node : nodes) {
			TreeNode<SchemaNodeData> treeNode = getTreeNode(node);
			if ( treeNode != null ) {
				opened.add(treeNode);
			}
		}
		setOpenObjects(opened);
	}
	
	public static class SchemaNodeData extends BasicTreeModel.SimpleNodeData {
		
		private SurveySummary survey;
		private SurveyObject surveyObject;
		
		protected SchemaNodeData(SurveyObject surveyObject, boolean root, boolean detached, String labelLanguage) {
			this(SurveySummary.createFromSurvey(surveyObject.getSurvey()), surveyObject, root, detached, labelLanguage);
		}
		
		protected SchemaNodeData(SurveySummary survey, SurveyObject surveyObject, boolean root, boolean detached, String labelLanguage) {
			this(survey, surveyObject, getLabel(surveyObject, root, detached, labelLanguage), root, detached);
		}

		protected SchemaNodeData(SurveyObject surveyObject, String label, boolean root, boolean detached) {
			this(SurveySummary.createFromSurvey(surveyObject.getSurvey()), surveyObject, label, root, detached);
		}
		
		protected SchemaNodeData(SurveySummary survey, SurveyObject surveyObject, String label, boolean root, boolean detached) {
			super(label, root, detached);
			this.survey = survey;
			this.surveyObject = surveyObject;
		}

		protected static String getLabel(SurveyObject surveyObject, boolean root, boolean detached, String labelLanguage) {
			String label;
			if ( detached ) {
				label = SchemaNodeData.getDetachedLabel(surveyObject, root);
			} else if ( surveyObject instanceof NodeDefinition ) {
				label = ((NodeDefinition) surveyObject).getName();
			} else {
				label = ((UITab) surveyObject).getLabel(labelLanguage);
			}
			return label;
		}
		
		protected static String getDetachedLabel(SurveyObject surveyObject, boolean root) {
			String result;
			if ( surveyObject instanceof NodeDefinition ) {
				String nodeTypeLabel = NodeType.getHeaderLabel((NodeDefinition) surveyObject, root, true);
				Object[] args = new String[]{nodeTypeLabel};
				result = Labels.getLabel("survey.schema.tree.new_node_label", args);
			} else {
				//TODO
				result = "NEW TAB";
			}
			return result;
		}
		
		public SurveySummary getSurvey() {
			return survey;
		}
		
		public SurveyObject getSurveyObject() {
			return surveyObject;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result
					+ ((surveyObject == null) ? 0 : surveyObject.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			SchemaNodeData other = (SchemaNodeData) obj;
			if (surveyObject == null) {
				if (other.surveyObject != null)
					return false;
			} else if (!surveyObject.equals(other.surveyObject))
				return false;
			return true;
		}
		

	}

	public static class SchemaTreeNode extends AbstractNode<SchemaNodeData> {
		
		private static final long serialVersionUID = 1L;
		
		private boolean disabled;
		
		SchemaTreeNode(SchemaNodeData data) {
			super(data);
			this.disabled = false;
		}
		
		SchemaTreeNode(SchemaNodeData data, Collection<SchemaTreeNode> children) {
			super(data, children);
			this.disabled = false;
		}
		
		public void markAsDetached() {
			SchemaNodeData data = getData();
			data.setDetached(true);
//			data.setLabel(SchemaTreeNodeData.getDetachedLabel(nodeDefinition, root));
		}
		
		@Override
		public String getIcon() {
			SchemaNodeData data = getData();
			SurveyObject surveyObject = data.getSurveyObject();
			return surveyObject == null ? null : SchemaVM.getIcon(surveyObject);
		}
		
		public String getTooltiptext() {
			SchemaNodeData data = getData();
			SurveyObject surveyObject = data.getSurveyObject();
			return surveyObject == null ? null : SchemaVM.getNodeTooltiptext(surveyObject);
		}
		
		public int getIndexInModel() {
			int result;
			SchemaNodeData data = getData();
			SurveyObject surveyObject = data.getSurveyObject();
			if ( surveyObject instanceof NodeDefinition ) {
				EntityDefinition parentEntity = ((NodeDefinition) surveyObject).getParentEntityDefinition();
				result = parentEntity.getChildDefinitionIndex((NodeDefinition) surveyObject);
			} else {
				result = ((UITab) surveyObject).getIndex();
			}
			return result;
		}

		public boolean isDisabled() {
			return disabled;
		}

		public void setDisabled(boolean disabled) {
			this.disabled = disabled;
		}

	}

}
