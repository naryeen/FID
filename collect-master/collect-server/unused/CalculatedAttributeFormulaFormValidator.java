package org.openforis.collect.designer.form.validator;

import org.openforis.collect.designer.viewmodel.CalculatedAttributeFormulaVM;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 * 
 */
public class CalculatedAttributeFormulaFormValidator extends FormValidator {

	protected static final String EXPRESSION_FIELD = "expression";
	protected static final String CONDITION_FIELD = "condition";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateExpression(ctx);
		validateCondition(ctx);
	}

	private void validateExpression(ValidationContext ctx) {
		if (validateRequired(ctx, EXPRESSION_FIELD)) {
			AttributeDefinition attrDefn = getAttributeDefinition(ctx);
			EntityDefinition parentEntityDefn = getParentEntityDefinition(ctx);
			validateValueExpression(ctx, attrDefn, parentEntityDefn, EXPRESSION_FIELD);
		}
	}

	private void validateCondition(ValidationContext ctx) {
		NodeDefinition contextNode = getAttributeDefinition(ctx);
		validateBooleanExpression(ctx, contextNode, CONDITION_FIELD);
	}

	private AttributeDefinition getAttributeDefinition(ValidationContext ctx) {
		AttributeDefinition result = (AttributeDefinition) ctx.getValidatorArg(CalculatedAttributeFormulaVM.ATTRIBUTE_DEFINITION_ARG);
		return result;
	}

	private EntityDefinition getParentEntityDefinition(ValidationContext ctx) {
		EntityDefinition result = (EntityDefinition) ctx.getValidatorArg(CalculatedAttributeFormulaVM.PARENT_ENTITY_DEFINITION_ARG);
		return result;
	}

}
