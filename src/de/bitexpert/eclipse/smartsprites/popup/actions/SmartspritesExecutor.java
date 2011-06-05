/*
 * Copyright (c) 2007-2011 bitExpert AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package de.bitexpert.eclipse.smartsprites.popup.actions;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.carrot2.labs.smartsprites.SmartSpritesParameters;
import org.carrot2.labs.smartsprites.SpriteBuilder;
import org.carrot2.labs.smartsprites.SmartSpritesParameters.PngDepth;
import org.carrot2.labs.smartsprites.message.MemoryMessageSink;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message.MessageLevel;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.collect.Lists;


/**
 * The main plugin action which executes the Smartsprite CSS Generator.
 *
 * @author	Stephan Hochdoerfer <S.Hochdoerfer@bitExpert.de>
 */


public class SmartspritesExecutor implements IObjectActionDelegate
{
	private IFile selectedFile;
	private String rootDir;
	private String outputDir;
	private String documentRootDir;
	private MessageLevel logLevel;
	private String cssFileSuffix;
	private String cssFileEncoding;
	private PngDepth spritePngDepth;
	private boolean spritePngIe6;


	/**
	 * Creates a new {@link SmartspritesExecutor}.
	 *
	 * @throws IOException
	 */
	public SmartspritesExecutor() throws IOException
	{
		this.cssFileSuffix   = SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX;
		this.cssFileEncoding = SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING;
		this.spritePngDepth  = SmartSpritesParameters.DEFAULT_SPRITE_PNG_DEPTH;
		this.spritePngIe6    = SmartSpritesParameters.DEFAULT_SPRITE_PNG_IE6;
	}


	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart)
	{
	}


	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action)
	{
		if(null != this.selectedFile)
		{
			List<String> cssFiles = Lists.newArrayList();
			cssFiles.add(this.selectedFile.getLocation().toOSString());

			final SmartSpritesParameters params = new SmartSpritesParameters(
				rootDir, cssFiles, outputDir, documentRootDir, logLevel,
				cssFileSuffix, spritePngDepth, spritePngIe6, cssFileEncoding);

			final MemoryMessageSink messageSink = new MemoryMessageSink();
			MessageLog log = new MessageLog(messageSink);

			if(params.validate(log))
			{
				try
				{
					/**
					 * Upon successful completion, SmartSprites will create all
					 * the sprite images in the locations specified by the
					 * sprite image directives. Also, next to each processed
					 * CSS file, SmartSprites will create a corresponding CSS
					 * file with a -sprite suffix.
					 */
					new SpriteBuilder(params, log).buildSprites();

					// refresh workspace project to see the newly created file
					this.selectedFile.getProject().refreshLocal(
						IResource.DEPTH_INFINITE, null);
				}
				catch (FileNotFoundException e)
				{
					MessageDialog.openInformation(null, "Smartsprite",
						"Input file not found!");
				}
				catch (IOException e)
				{
					MessageDialog.openInformation(null, "Smartsprite",
						"Accessing the input file failed!");
				}
				catch (CoreException e)
				{
					MessageDialog.openInformation(null, "Smartsprite",
						"Undefined error!");
				}
			}
		}
	}


	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection strucSelect = (IStructuredSelection) selection;
			this.selectedFile = (IFile) strucSelect.getFirstElement();
		}
	}
}