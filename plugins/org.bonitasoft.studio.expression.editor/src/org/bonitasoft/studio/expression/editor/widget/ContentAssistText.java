/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.expression.editor.widget;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.studio.common.jface.SWTBotConstants;
import org.bonitasoft.studio.expression.editor.autocompletion.AutoCompletionField;
import org.bonitasoft.studio.expression.editor.autocompletion.IBonitaContentProposalListener2;
import org.bonitasoft.studio.expression.editor.autocompletion.IExpressionProposalLabelProvider;
import org.bonitasoft.studio.pics.Pics;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Romain Bioteau
 *
 */
public class ContentAssistText extends Composite implements SWTBotConstants {


	private Text textControl;
	private AutoCompletionField autoCompletion;
	private boolean drawBorder = true;
	private ToolBar tb;
	private boolean isReadOnly = false;
	private List<IBonitaContentProposalListener2> contentAssistListerners = new ArrayList<IBonitaContentProposalListener2>();

	public ContentAssistText(Composite parent, IExpressionProposalLabelProvider contentProposalLabelProvider, int style) {
		super(parent, SWT.NONE);
		Point margins = new Point(3, 3);
		if ((style & SWT.BORDER) == 0){
			drawBorder = false;
			margins = new Point(0, 0);
		}else{
			style = style ^ SWT.BORDER;
		}
		if((style & SWT.READ_ONLY) != 0){
			isReadOnly = true;
		}
		int indent = 32;
		if(isReadOnly){
			indent = 18;
		}
		setLayout(GridLayoutFactory.fillDefaults().numColumns(2).margins(margins).spacing(indent, 0).create());
		setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));


		textControl = new Text(this,style | SWT.SINGLE);
		textControl.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		textControl.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		textControl.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				if(textControl.equals(e.widget)){
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							if(!ContentAssistText.this.isDisposed()){
								ContentAssistText.this.redraw();
								if(!autoCompletion.getContentProposalAdapter().hasProposalPopupFocus()){
									if(autoCompletion.getContentProposalAdapter().isProposalPopupOpen()){
										autoCompletion.getContentProposalAdapter().closeProposalPopup();
									}
								}
							}
						}
					});

				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				if(textControl.equals(e.widget)){
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
							if(!ContentAssistText.this.isDisposed()){
								ContentAssistText.this.redraw();
							}

						}
					});
				}
			}
		});
		/*Data for test purpose*/
		textControl.setData(SWTBOT_WIDGET_ID_KEY, SWTBOT_ID_EXPRESSIONVIEWER_TEXT);
		tb = new ToolBar(this, SWT.FLAT | SWT.NO_FOCUS);
		tb.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		tb.setLayoutData(GridDataFactory.swtDefaults().create());
		final ToolItem ti = new ToolItem(tb, SWT.FLAT | SWT.NO_FOCUS);
		ti.setData(SWTBOT_WIDGET_ID_KEY, SWTBOT_ID_EXPRESSIONVIEWER_DROPDOWN);
		ti.setImage(Pics.getImage("resize_S.gif"));
		ti.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setFocus();
				BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
					public void run() {
						fireOpenProposalEvent();
						if(autoCompletion.getContentProposalAdapter().isProposalPopupOpen()){
							autoCompletion.getContentProposalAdapter().closeProposalPopup();
						}else{
							autoCompletion.getContentProposalAdapter().showProposalPopup();
						}
					}
				});
			}
		});

		addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				if(drawBorder){
					paintControlBorder(e);
				}
			}
		});
		autoCompletion = new AutoCompletionField(textControl, new TextContentAdapter(), contentProposalLabelProvider) ;
	}


	protected void fireOpenProposalEvent() {
		for(IBonitaContentProposalListener2 listener : contentAssistListerners){
			listener.proposalPopupOpened(autoCompletion.getContentProposalAdapter());
		}
	}


	protected void paintControlBorder(PaintEvent e) {
		GC gc = e.gc;
		Display display = e.display ;
		if(display!= null && gc != null && !gc.isDisposed()){
			Control focused = display.getFocusControl() ;
			GC parentGC  = gc;
			parentGC.setAdvanced(true);
			Rectangle r = ContentAssistText.this.getBounds();
			if(focused == null || (focused.getParent() != null && !focused.getParent().equals(ContentAssistText.this))){
				parentGC.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
			}else{
				parentGC.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_BORDER));
			}
			parentGC.setLineWidth(1);
			parentGC.drawRectangle(0, 0, r.width-1, r.height-1);
		}
	}


	public Text getTextControl() {
		return textControl;
	}

	public AutoCompletionField getAutocompletion() {
		return autoCompletion;
	}

	public ToolBar getToolbar() {
		return tb;
	}


	public void addContentAssistListener(IBonitaContentProposalListener2 listener) {
		contentAssistListerners.add(listener);
	}

}
