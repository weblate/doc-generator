/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.generator

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.enums.SupportedFileFormat
import org.hyacinthbots.docgenerator.excpetions.ConflictingFileFormatException
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createFile
import kotlin.io.path.exists

internal object DocsGenerator {
	private val generatorLogger = KotlinLogging.logger { }

	private suspend inline fun findOrCreateDocumentsFile(path: Path) {
		if (!path.exists()) {
			generatorLogger.debug("File does not exist, creating...")
			try {
				withContext(Dispatchers.IO) {
					path.createFile()
				}
			} catch (e: IOException) {
				generatorLogger.error(e) { e.message }
				return
			}
			generatorLogger.debug("File created successfully...")
		}
	}

	// TODO support generating multiple files for each language
	private suspend inline fun generateMarkdownContents(
		commandTypes: MutableList<CommandTypes>,
		loadedExtensions: MutableList<Extension>
	): String {
		var totalOutput = ""
		commandTypes.forEach { type ->
			when (type) {
				CommandTypes.SLASH -> {
					val slashCommands: MutableList<SlashCommand<*, *>> = mutableListOf()
					loadedExtensions.forEach { extension ->
						extension.slashCommands.forEach { slashCommands.add(it) }
					}

					var output = "## Slash Commands\n\n"

					for (slashCommand in slashCommands) {
						var commandInfo = ""
						if (slashCommand.subCommands.isNotEmpty()) {
							commandInfo += "### Parent Command name: `${slashCommand.name}`\n" +
									"* **Parent command description**: ${slashCommand.description}\n"
							slashCommand.subCommands.forEach { subCommand ->
								var arguments = ""
								subCommand.arguments?.invoke()?.args?.forEach { arg ->
									arguments += "\t\t\t* **Name**: ${arg.displayName}" +
											"\n\t\t\t* **Description**: ${arg.description}" +
											"\n\t\t\t* **Type**: ${arg.converter.signatureTypeString}\n"
								}

								if (arguments.isEmpty()) arguments = "None"
								commandInfo += "\t#### Sub-command name: `${subCommand.name}`\n" +
										"\t* **Sub-command description**: ${subCommand.description}\n" +
										"\t\t* **Arguments**:\n$arguments\n"
							}
						} else {
							var arguments = ""
							slashCommand.arguments?.invoke()?.args?.forEach { arg ->
								arguments += "\t\t* **Name**: ${arg.displayName}" +
										"\n\t\t* **Description**: ${arg.description}" +
										"\n\t\t* **Type**: ${arg.converter.signatureTypeString}\n"
							}
							if (arguments.isEmpty()) arguments = "None"
							commandInfo +=
								"### Command name: `${slashCommand.name}`\n" +
										"* Description: ${slashCommand.description}\n" +
										"\t* Arguments:\n$arguments\n"
						}

						output += commandInfo
					}

					totalOutput += output
				}

				CommandTypes.MESSAGE -> {
				}

				CommandTypes.USER -> {
				}
			}
		}

		return totalOutput
	}

	@Suppress("UnusedPrivateMember", "EmptyFunctionBlock")
	private suspend inline fun generateTextContents(
		commandTypes: MutableList<CommandTypes>,
		loadedExtensions: MutableList<Extension>
	) {
	}

	suspend inline fun updateDocumentsFile(
		path: Path,
		fileFormat: SupportedFileFormat,
		commandTypes: MutableList<CommandTypes>,
		loadedExtensions: MutableList<Extension>
	) {
		findOrCreateDocumentsFile(path)

		if (!path.toFile().canRead()) {
			throw IOException("Unable to read documents file! Please check the permissions!")
		} else if (path.toFile().extension != fileFormat.fileExtension) {
			throw ConflictingFileFormatException(fileFormat.fileExtension, path.toFile().extension)
		}

		when (fileFormat) {
			SupportedFileFormat.MARKDOWN -> {
				val contents = generateMarkdownContents(commandTypes, loadedExtensions)
				val writer = path.bufferedWriter(Charsets.UTF_8)
				val debugWriter = Path("./build/debugDocs.txt").bufferedWriter()
				withContext(Dispatchers.IO) {
					writer.write("")
					writer.write(contents)
					writer.flush()
					writer.close()
				}
				withContext(Dispatchers.IO) {
					debugWriter.write("")
					debugWriter.write("`$contents`")
					debugWriter.flush()
					debugWriter.close()
				}
				generatorLogger.info("Written documents!")
			}

			SupportedFileFormat.TEXT -> generateTextContents(commandTypes, loadedExtensions)
		}
	}
}
