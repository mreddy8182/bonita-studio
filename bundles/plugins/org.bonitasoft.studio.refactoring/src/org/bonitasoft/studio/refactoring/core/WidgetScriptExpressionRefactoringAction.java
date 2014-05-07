/**
 * Copyright (C) 2014 Bonitasoft S.A.
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
package org.bonitasoft.studio.refactoring.core;

import java.util.List;

import org.bonitasoft.studio.model.expression.Expression;
import org.bonitasoft.studio.model.form.Widget;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.EditingDomain;

/**
 * @author Romain Bioteau
 * 
 */
public class WidgetScriptExpressionRefactoringAction extends AbstractScriptExpressionRefactoringAction {

    public WidgetScriptExpressionRefactoringAction(EObject newValue, String oldName, String newName, List<Expression> groovyScriptExpressions,
            List<Expression> updatedGroovyScriptExpressions, CompoundCommand cc, EditingDomain domain, RefactoringOperationType operationKind) {
        super(newValue, oldName, newName, groovyScriptExpressions, updatedGroovyScriptExpressions, cc, domain, operationKind);
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.studio.common.refactoring.BonitaGroovyRefactoringAction#getReferencedObjectInScriptsOperation(org.bonitasoft.studio.model.expression.
     * Expression)
     */
    @Override
    protected EObject getReferencedObjectInScriptsOperation(Expression expr) {
        for (EObject reference : expr.getReferencedElements()) {
            if (reference instanceof Widget) {
                if (("field_"+((Widget) reference).getName()).equals(getOldName())) {
                    return reference;
                }
            }
        }
        return null;
    }

}