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
package org.bonitasoft.studio.businessobject.ui.wizard.editingsupport;

import org.bonitasoft.studio.businessobject.ui.wizard.listener.ColumnViewerUpdateListener;
import org.bonitasoft.studio.businessobject.ui.wizard.validator.BusinessObjectNameCellEditorValidator;
import org.bonitasoft.studio.common.NamingUtils;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.Query;

/**
 * @author Romain Bioteau
 * 
 */
public class BusinessObjectNameEditingSupport extends ObservableValueEditingSupport {

    private DataBindingContext dbc;

    private IObservableValue packageNameObservableValue;

    private BusinessObjectModel businessObjectModel;

    private IViewerObservableValue businessObjectObservableValue;

    /**
     * @param viewer
     * @param dbc
     */
    public BusinessObjectNameEditingSupport(BusinessObjectModel businessObjectModel, IViewerObservableValue businessObjectObservableValue,
            IObservableValue packageNameObservableValue, ColumnViewer viewer, DataBindingContext dbc) {
        super(viewer, dbc);
        this.dbc = dbc;
        this.packageNameObservableValue = packageNameObservableValue;
        this.businessObjectModel = businessObjectModel;
        this.businessObjectObservableValue = businessObjectObservableValue;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        TextCellEditor textCellEditor = new TextCellEditor((Composite) getViewer().getControl());
        Text textControl = (Text) textCellEditor.getControl();
        textControl.setTextLimit(BusinessObjectNameCellEditorValidator.MAX_TABLE_NAME_LENGTH + 5);
        return textCellEditor;
    }

    @Override
    protected IObservableValue doCreateElementObservable(final Object element, ViewerCell cell) {
        IObservableValue observeValue = PojoObservables.observeValue(element, "qualifiedName");
        observeValue.addValueChangeListener(new ColumnViewerUpdateListener(getViewer(), element));
        observeValue.addValueChangeListener(new IValueChangeListener() {

            @Override
            public void handleValueChange(ValueChangeEvent event) {
                BusinessObject businessObject = (BusinessObject) businessObjectObservableValue.getValue();
                String oldName = (String) event.diff.getOldValue();
                String newName = (String) event.diff.getNewValue();
                for (Query q : businessObject.getQueries()) {
                    if (q.getReturnType().equals(oldName)) {
                        q.setReturnType(newName);
                    }
                }

            }
        });
        return observeValue;
    }

    @Override
    protected IObservableValue doCreateCellEditorObservable(CellEditor cellEditor) {
        return SWTObservables.observeText(cellEditor.getControl(), SWT.Modify);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport#createBinding(org.eclipse.core.databinding.observable.value.IObservableValue,
     * org.eclipse.core.databinding.observable.value.IObservableValue)
     */
    @Override
    protected Binding createBinding(IObservableValue target, IObservableValue model) {
        UpdateValueStrategy targetToModel = new UpdateValueStrategy(UpdateValueStrategy.POLICY_CONVERT);
        BusinessObject value = (BusinessObject) businessObjectObservableValue.getValue();
        targetToModel.setAfterGetValidator(new BusinessObjectNameCellEditorValidator(businessObjectModel, value));
        targetToModel.setConverter(new Converter(String.class, String.class) {

            @Override
            public Object convert(Object fromObject) {
                return packageNameObservableValue.getValue() + "." + fromObject.toString();
            }
        });
        UpdateValueStrategy modelToTarget = new UpdateValueStrategy();
        modelToTarget.setConverter(new Converter(String.class, String.class) {

            @Override
            public Object convert(Object fromObject) {
                if (fromObject != null) {
                    return NamingUtils.getSimpleName(fromObject.toString());
                }
                return null;
            }
        });
        return dbc.bindValue(target, model, targetToModel, modelToTarget);
    }

}