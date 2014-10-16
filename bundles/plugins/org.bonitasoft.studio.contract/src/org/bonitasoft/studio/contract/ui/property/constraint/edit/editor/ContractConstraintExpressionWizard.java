/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.contract.ui.property.constraint.edit.editor;

import org.bonitasoft.studio.common.emf.tools.ModelHelper;
import org.bonitasoft.studio.contract.i18n.Messages;
import org.bonitasoft.studio.model.process.Contract;
import org.bonitasoft.studio.model.process.ContractConstraint;
import org.bonitasoft.studio.model.process.ContractInput;
import org.bonitasoft.studio.model.process.ProcessPackage;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;


/**
 * @author Romain Bioteau
 *
 */
public class ContractConstraintExpressionWizard extends Wizard {

    private ContractConstraintExpressionWizardPage editor;
    private final ContractConstraint constraint;
    private final ContractConstraint constraintWorkingCopy;
    private final EList<ContractInput> inputs;
    private final IPropertySourceProvider propertySourceProvider;

    public ContractConstraintExpressionWizard(final ContractConstraint constraint, final IPropertySourceProvider propertySourceProvider) {
        this.constraint = constraint;
        constraintWorkingCopy = EcoreUtil.copy(constraint);
        inputs = ModelHelper.getFirstContainerOfType(constraint, Contract.class).getInputs();
        this.propertySourceProvider = propertySourceProvider;
        setDefaultPageImageDescriptor(Pics.getWizban());
    }

    @Override
    public void addPages() {
        editor = new ContractConstraintExpressionWizardPage(constraintWorkingCopy, inputs);
        if (constraintWorkingCopy.getExpression() == null || constraintWorkingCopy.getExpression().isEmpty()) {
            editor.setTitle(Messages.bind(Messages.addContentToConstraint, constraintWorkingCopy.getName()));
        } else {
            editor.setTitle(Messages.bind(Messages.editContentToConstraint, constraintWorkingCopy.getName()));
        }
        addPage(editor);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        final IPropertySource constraintPropertySource = propertySourceProvider.getPropertySource(constraint);
        constraintPropertySource.setPropertyValue(ProcessPackage.Literals.CONTRACT_CONSTRAINT__EXPRESSION, constraintWorkingCopy.getExpression());
        constraintPropertySource.setPropertyValue(ProcessPackage.Literals.CONTRACT_CONSTRAINT__INPUT_NAMES, constraintWorkingCopy.getInputNames());
        return true;
    }


}
