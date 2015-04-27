/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.refactoring.core.script;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.bonitasoft.studio.refactoring.core.RefactorPair;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;

public abstract class ScriptContainer<T extends EObject> {

    private final T modelElement;

    private final EAttribute scriptFeature;

    private String newScript;

    private final EAttribute dependencyNameFeature;

    public ScriptContainer(final T modelElement,
            final EAttribute scriptFeature,
            final EAttribute dependencyNameFeature) {
        this.modelElement = modelElement;
        this.scriptFeature = scriptFeature;
        this.dependencyNameFeature = dependencyNameFeature;
    }

    public EAttribute getScriptFeature() {
        return scriptFeature;
    }

    public String getScript() {
        return (String) modelElement.eGet(scriptFeature);
    }

    public T getModelElement() {
        return modelElement;
    }

    public String getNewScript() {
        return newScript;
    }

    public void setNewScript(final String newScript) {
        this.newScript = newScript;
    }

    public abstract void updateScript(List<ReferenceDiff> referenceDiffs, final IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException;

    public CompoundCommand applyUpdate() {
        final CompoundCommand compoundCommand = new CompoundCommand();
        if (scriptHasChanged()) {
            compoundCommand.append(SetCommand.create(editingDomain(), modelElement, scriptFeature,
                    newScript));
        }
        return compoundCommand;
    }

    public abstract String getName();

    protected EditingDomain editingDomain() {
        return TransactionUtil.getEditingDomain(modelElement);
    }

    public abstract CompoundCommand updateDependencies(List<? extends RefactorPair<? extends EObject, ? extends EObject>> pairsToRefactor);

    public abstract CompoundCommand removeDependencies(List<? extends RefactorPair<? extends EObject, ? extends EObject>> pairsToRefactor);

    protected boolean scriptHasChanged() {
        return getScript() != getNewScript();
    }

    public EAttribute getDependencyNameFeature() {
        return dependencyNameFeature;
    }

}
