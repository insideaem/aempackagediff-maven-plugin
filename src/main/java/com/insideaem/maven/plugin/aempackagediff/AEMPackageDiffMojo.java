/**
 * Copyright (C) 2015, InsideAEM
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - The name of its contributor may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.insideaem.maven.plugin.aempackagediff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import com.thoughtworks.xstream.XStream;

/**
 * Goal which generates a filter.xml file containing only the changes between
 * two revisions
 *
 * @goal aempackagediff
 */
public class AEMPackageDiffMojo extends AbstractMojo {

	/**
	 * Location of the output directory.
	 *
	 * @parameter
	 * @required
	 */
	private final File outputDirectory = null;

	/**
	 * Location of the original output directory.
	 *
	 * @parameter expression="${project.build.outputDirectory}"
	 */
	private final File originalOutputDirectory = null;

	/**
	 * Project's source directory as specified in the POM.
	 *
	 * @parameter expression="${basedir}"
	 * @readonly
	 * @required
	 */
	private final File sourceDirectory = new File("");

	/**
	 * Command to get changes.
	 *
	 * @parameter expression="${aempackagediff.cmd}"
	 */
	private String diffCmd;

	/**
	 * Encoding of source files
	 *
	 * @parameter expression="${encoding}"
	 *            default-value="${project.build.sourceEncoding}"
	 */
	private final String encoding = "UTF-8";

	private boolean ensureTargetDirectoryExists() {
		this.originalOutputDirectory.mkdirs();
		if (this.outputDirectory.exists()) {
			return true;
		}
		return this.outputDirectory.mkdirs();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!this.ensureTargetDirectoryExists()) {
			this.getLog().error("Could not create target directory");
			return;
		}

		try {
			String line;
			final Process p = Runtime.getRuntime().exec(this.diffCmd);
			final BufferedReader input = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			final WorkspaceFilter workspaceFilter = new WorkspaceFilter();
			final String fileSeperator = System.getProperty("file.separator");

			// Delete everything under orignalOutputDirectory/classes
			final File classes = new File(
					this.originalOutputDirectory.getPath());
			this.getLog().info(
					String.format("Classes path %s", classes.getPath()));
			final File[] content = classes.listFiles();
			for (final File file : content) {
				this.getLog()
						.info(String.format("Deleting %s", file.getPath()));
				FileUtils.deleteDirectory(file);
			}
			while ((line = input.readLine()) != null) {
				final int beginIndex = line.indexOf("/jcr_root/");
				if (beginIndex > 0) {
					final String sourcePath = this.sourceDirectory
							+ fileSeperator + line;
					this.getLog().info(
							String.format("Source path %s", sourcePath));
					final File sourceFile = new File(sourcePath);
					final String diffFilePath = line.substring(beginIndex
							+ "/jcr_root/".length() - 1);
					final boolean isDir = diffFilePath.endsWith(".content.xml");
					final String diffRootPath = diffFilePath.replaceAll(
							"/.content.xml", "");

					// copy diff content under classes
					final File diffFile = new File(this.originalOutputDirectory
							+ fileSeperator + diffFilePath);
					// Ensure directory structure exists
					diffFile.getParentFile().mkdirs();
					this.getLog().info(
							String.format("Diff path %s", diffFilePath));
					if (sourceFile.exists()) {
						this.getLog().info(
								String.format("Copying file %s to %s",
										sourceFile.getPath(),
										diffFile.getPath()));
						FileUtils.copyFile(sourceFile, diffFile);
					}

					final Filter filter = new Filter(diffRootPath, isDir);
					workspaceFilter.addFilter(filter);
				}
			}
			input.close();

			// write workspace filter
			final StringBuffer path = new StringBuffer();
			path.append(this.outputDirectory);

			path.append(fileSeperator);
			path.append("filter.xml");
			final File file = new File(path.toString());
			this.getLog().info(file.getPath());
			file.createNewFile();

			try (OutputStreamWriter out = new OutputStreamWriter(
					new FileOutputStream(file), this.encoding)) {

				final XStream xstream = new XStream();
				xstream.processAnnotations(WorkspaceFilter.class);
				xstream.processAnnotations(Filter.class);
				xstream.processAnnotations(IncludeExclude.class);

				out.write(xstream.toXML(workspaceFilter));

			}

		} catch (final Exception err) {
			this.getLog().error(err);
		}

	}
}
