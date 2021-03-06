package org.rileyberton.eclipse.smart_tab.handlers;

import java.lang.reflect.Field;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.jface.action.IAction;
//import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class TabHandler extends AbstractHandler {
	
	private boolean hippieExpandFixed = false;
	/**
	 * The constructor.
	 */
	public TabHandler() {
	}
	
	private IAction getHippieCompleteAction(ITextEditor editor) {
		IAction action = editor.getAction(ITextEditorActionConstants.HIPPIE_COMPLETION);
		return action;
	}
	
	/* this is extreme hackery, thank you java */
	private void fixHippieCompletionCommandId(IAction action) {
		if (hippieExpandFixed) {
			return;
		}
		try {
			Field f = action.getClass().getDeclaredField("fExitStrategy");
			f.setAccessible(true);
			
			Object exitStrat = f.get(action);
			
			Field commands = exitStrat.getClass().getDeclaredField("fCommandIds");
			commands.setAccessible(true);
			
			commands.set(exitStrat, new String[] {"org.rileyberton.eclipse.smart_tab.command"});
			hippieExpandFixed = true;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Command getCommand(IEditorPart editor, final String cmdName) {
    	ICommandService commandService = (ICommandService) editor.getSite().getService(ICommandService.class);
   		return commandService.getCommand(cmdName);
	}

	private void executeCommand(IEditorPart win, final String cmdName) {
    	ICommandService commandService = (ICommandService) win.getSite().getService(ICommandService.class);
    	try {
    		Command command = commandService.getCommand(cmdName);
    		command.executeWithChecks(new ExecutionEvent());
    	} catch (NotDefinedException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (NotEnabledException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (NotHandledException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IWorkbenchWindow win = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPage page = win.getActivePage();
        IEditorPart editor = page.getActiveEditor();
        if(editor instanceof ITextEditor){
            ISelectionProvider selectionProvider = ((ITextEditor) editor).getSelectionProvider();
            ISelection selection = selectionProvider.getSelection();
            if (selection instanceof ITextSelection) {
                ITextSelection textSelection = (ITextSelection)selection;
                IDocumentProvider provider = ((ITextEditor)editor).getDocumentProvider();
                IDocument document = provider.getDocument(editor.getEditorInput());
                
                String selectionText = textSelection.getText();
                if (selectionText.trim().length() > 0) {
                	/* call the indent command */
                	executeCommand(editor, "org.eclipse.ui.edit.text.shiftRight");
                } else {
                
	                String textBehindCursor = null;
	                try {
	                    textBehindCursor = document.get(textSelection.getOffset()-1, 1);
	                } catch (BadLocationException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                    textBehindCursor = null;
	                }
	                if (textBehindCursor == null || textBehindCursor.trim().length() == 0) {
	                	/* case of a blank behind the cursor, insert a tab. */
	                    try {
							document.replace(textSelection.getOffset(), 0, "\t");
							((ITextEditor)editor).selectAndReveal(textSelection.getOffset()+1, 0); 
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                } else if (textBehindCursor.equals(".") || textBehindCursor.equals(">") || textBehindCursor.equals(":")) {
	                	/* case where there is a separator, invoke the auto completion */
	                	executeCommand(editor, "org.eclipse.ui.edit.text.contentAssist.proposals");
	                } else {
	                	/* case where there is any other character, invoke word completion */
	                	IAction a = getHippieCompleteAction((ITextEditor)editor);
	                	fixHippieCompletionCommandId(a);
	                	a.run();
	                }
	                
                }
            }
        }



        return null;
		
	}
}
