/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.generator

import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.hyacinthbots.docgenerator.additionalDocumentation
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.enums.SupportedFileFormat
import org.hyacinthbots.docgenerator.excpetions.ConflictingFileFormatException
import org.hyacinthbots.docgenerator.findOrCreateDocumentsFile
import org.hyacinthbots.docgenerator.formatArguments
import org.hyacinthbots.docgenerator.formatPermissionsSet
import org.hyacinthbots.docgenerator.subCommandAdditionalDocumentation
import org.hyacinthbots.docgenerator.translate
import java.io.IOException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.bufferedWriter

// TODO At least comment some of this code if you don't doc it or people reading it will melt
internal object DocsGenerator {
	internal val generatorLogger = KotlinLogging.logger { }

	// TODO Permissions for each command & subcommand
	private suspend inline fun generateMarkdownContents(
		commandTypes: List<CommandTypes>,
		loadedExtensions: List<Extension>,
		language: Locale? = null
	): String {
		var totalOutput = ""
		@Suppress("UNUSED_VALUE") // You do not understand
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
						val parentProvider = slashCommand.translationsProvider
						if (slashCommand.subCommands.isNotEmpty()) {
							var parentExtraDocs = slashCommand.additionalDocumentation[slashCommand.name]
							commandInfo += "### ${"header.parentcommand.name".translate(parentProvider, language)}: `${
								slashCommand.name.translate(parentProvider, language, slashCommand.bundle)
							}`\n* **${"header.parentcommand.description".translate(parentProvider, language)}**: ${
								slashCommand.description.translate(parentProvider, language, slashCommand.bundle)
							}\n${
								if (language != null && parentExtraDocs?.extraInformation != null) {
									"* **${"header.additionalinfo".translate(parentProvider, language)}**: ${
										parentExtraDocs.extraInformation!!.translate(
											parentProvider, language, slashCommand.bundle
										)
									}\n"
								} else {
									""
								}
							}"

							slashCommand.subCommands.forEach { subCommand ->
								var arguments = ""
								val subProvider = subCommand.translationsProvider
								var subExtraDocs = subCommand.subCommandAdditionalDocumentation[subCommand.name]
								val bundle = subCommand.bundle
								subCommand.arguments?.invoke()?.args?.forEach { arg ->
									arguments += formatArguments(
										arg, true, subProvider, subCommand.bundle, language
									)
								}

								if (arguments.isEmpty()) arguments = "arguments.none".translate(subProvider, language)

								commandInfo += "\t#### ${"header.subcommand.name".translate(subProvider, language)}: `${
									subCommand.name.translate(subProvider, language, bundle)
								}`\n\t* **${"header.subcommand.description".translate(subProvider, language)}**: ${
									subCommand.description.translate(subProvider, language, bundle)
								}\n${
									if (language != null && subExtraDocs?.commandResult != null) {
										"\t* **${"header.result".translate(subProvider, language)}**:${
											subExtraDocs.commandResult!!.translate(subProvider, language, bundle)
										}\n"
									} else {
										""
									}
								}${
									if (language != null && subExtraDocs?.extraInformation != null) {
										"\t* **${"header.additionalinfo".translate(subProvider, language)}**:${
											subExtraDocs.extraInformation!!.translate(subProvider, language, bundle)
										}\n"
									} else {
										""
									}
								}${
									if (language != null && subCommand.requiredPerms.isNotEmpty()) {
										"\t* ${"header.permissions.required".translate(subProvider, language)}:${
											subCommand.requiredPerms.formatPermissionsSet(language)
										}\n"
									} else {
										""
									}
								}${
									if (language != null && subCommand.defaultMemberPermissions != null) {
										"\t* ${"header.permissions.default".translate(subProvider, language)}: ${
											subCommand.defaultMemberPermissions.formatPermissionsSet(language)
										}\n"
									} else {
										""
									}
								}\n\t\t* **${"header.arguments".translate(subProvider, language)}**:\n$arguments\n"
								subExtraDocs = null
							}
							parentExtraDocs = null
						} else {
							var arguments = ""
							val slashProvider = slashCommand.translationsProvider
							var extraDocs = slashCommand.additionalDocumentation[slashCommand.name]
							val bundle = slashCommand.bundle
							slashCommand.arguments?.invoke()?.args?.forEach { arg ->
								arguments += formatArguments(
									arg, false, slashProvider, bundle, language
								)
							}
							if (arguments.isEmpty()) {
								arguments = "arguments.none".translate(slashProvider, language)
							}
							commandInfo +=
								"### ${"header.command.name".translate(slashProvider, language)}: `${
									slashCommand.name.translate(slashProvider, language, bundle)
								}`\n* ${"header.command.description".translate(slashProvider, language)}: ${
									slashCommand.description.translate(slashProvider, language, bundle)
								}\n${
									if (language != null && extraDocs?.commandResult != null) {
										"* **${"header.result".translate(slashProvider, language)}**:${
											extraDocs.commandResult!!.translate(slashProvider, language, bundle)
										}\n"
									} else {
										""
									}
								}${
									if (language != null && extraDocs?.extraInformation != null) {
										"* **${"header.additionalinfo".translate(slashProvider, language)}**:${
											extraDocs.extraInformation!!.translate(slashProvider, language, bundle)
										}\n"
									} else {
										""
									}
								}${
									if (language != null && slashCommand.requiredPerms.isNotEmpty()) {
										"\t* ${"header.permissions.required".translate(slashProvider, language)}:${
											slashCommand.requiredPerms.formatPermissionsSet(language)
										}\n"
									} else {
										""
									}
								}${
									if (language != null && slashCommand.defaultMemberPermissions != null) {
										"\t* ${"header.permissions.default".translate(slashProvider, language)}: ${
											slashCommand.defaultMemberPermissions.formatPermissionsSet(language)
										}\n"
									} else {
										""
									}
								}\n\t* ${"header.arguments".translate(slashProvider, language)}:\n$arguments\n"
							extraDocs = null
						}

						output += commandInfo
					}

					totalOutput += output
				}

				CommandTypes.MESSAGE -> {
					val messageCommands: MutableList<MessageCommand<*>> = mutableListOf()
					loadedExtensions.forEach { extension ->
						extension.messageCommands.forEach { messageCommands.add(it) }
					}

					var output = "## Message Commands\n\n"

					for (messageCommand in messageCommands) {
						var additionalDocs = messageCommand.additionalDocumentation[messageCommand.name]
						val provider = messageCommand.translationsProvider
						val bundle = messageCommand.bundle
						output +=
							"### ${"header.messagecommand.name".translate(provider, language)}: `${
								messageCommand.name.translate(provider, language, bundle)
							}`\n${
								if (language != null && additionalDocs?.commandResult != null) {
									"* **${"header.result".translate(provider, language)}**:${
										additionalDocs.commandResult!!.translate(provider, language, bundle)
									}\n"
								} else {
									""
								}
							}${
								if (language != null && additionalDocs?.extraInformation != null) {
									"* **${"header.additionalinfo".translate(provider, language)}**:${
										additionalDocs.extraInformation!!.translate(provider, language, bundle)
									}\n"
								} else {
									""
								}
							}${
								if (language != null && messageCommand.requiredPerms.isNotEmpty()) {
									"\t* ${"header.permissions.required".translate(provider, language)}:${
										messageCommand.requiredPerms.formatPermissionsSet(language)
									}\n"
								} else {
									""
								}
							}${
								if (language != null && messageCommand.defaultMemberPermissions != null) {
									"\t* ${"header.permissions.default".translate(provider, language)}: ${
										messageCommand.defaultMemberPermissions.formatPermissionsSet(language)
									}\n"
								} else {
									""
								}
							}"
						additionalDocs = null
					}

					totalOutput += output
				}

				CommandTypes.USER -> {
					val userCommands: MutableList<UserCommand<*>> = mutableListOf()
					loadedExtensions.forEach { extension ->
						extension.userCommands.forEach { userCommands.add(it) }
					}

					var output = "## User Commands\n\n"

					for (userCommand in userCommands) {
						var additionalDocs = userCommand.additionalDocumentation[userCommand.name]
						val provider = userCommand.translationsProvider
						val bundle = userCommand.bundle
						output +=
							"### ${"header.usercommand.name".translate(provider, language)}: `${
								userCommand.name.translate(provider, language, userCommand.bundle)
							}\n${
								if (language != null && additionalDocs?.commandResult != null) {
									"* **${"header.result".translate(provider, language)}**:${
										additionalDocs.commandResult!!.translate(provider, language, bundle)
									}\n"
								} else {
									""
								}
							}${
								if (language != null && additionalDocs?.extraInformation != null) {
									"* **${"header.additionalinfo".translate(provider, language)}**:${
										additionalDocs.extraInformation!!.translate(provider, language, bundle)
									}\n"
								} else {
									""
								}
							}${
								if (language != null && userCommand.requiredPerms.isNotEmpty()) {
									"\t* ${"header.permissions.required".translate(provider, language)}:${
										userCommand.requiredPerms.formatPermissionsSet(language)
									}\n"
								} else {
									""
								}
							}${
								if (language != null && userCommand.defaultMemberPermissions != null) {
									"\t* ${"header.permissions.default".translate(provider, language)}: ${
										userCommand.defaultMemberPermissions.formatPermissionsSet(language)
									}\n"
								} else {
									""
								}
							}"
						additionalDocs = null
					}

					totalOutput += output
				}
			}
		}

		return totalOutput
	}

	// TODO I'm going to die making this
	@Suppress("UnusedPrivateMember", "EmptyFunctionBlock")
	private suspend inline fun generateTextContents(
		commandTypes: List<CommandTypes>,
		loadedExtensions: List<Extension>
	) {
	}

	suspend inline fun updateDocumentsFile(
		path: Path,
		fileFormat: SupportedFileFormat,
		commandTypes: List<CommandTypes>,
		loadedExtensions: List<Extension>,
		languages: List<Locale>? = null
	) {
		if (!languages.isNullOrEmpty()) {
			languages.forEach { language ->
				val i18nPath = Path(path.toString().replace(".md", "-${language.toLanguageTag()}.md"))
				findOrCreateDocumentsFile(i18nPath)

				if (!i18nPath.toFile().canRead() || !i18nPath.toFile().canWrite()) {
					throw IOException(
						"Unable to read/write documents file for ${language.toLanguageTag()}! Please check the permissions"
					)
				} else if (i18nPath.toFile().extension != fileFormat.fileExtension) {
					throw ConflictingFileFormatException(fileFormat.fileExtension, path.toFile().extension)
				}
			}
		} else {
			findOrCreateDocumentsFile(path)

			if (!path.toFile().canRead() || !path.toFile().canWrite()) {
				throw IOException("Unable to read/write documents file! Please check the permissions!")
			} else if (path.toFile().extension != fileFormat.fileExtension) {
				throw ConflictingFileFormatException(fileFormat.fileExtension, path.toFile().extension)
			}
		}

		@Suppress("OptionalWhenBraces") // How about no
		when (fileFormat) {
			SupportedFileFormat.MARKDOWN -> {
				if (!languages.isNullOrEmpty()) {
					languages.forEach { language ->
						val contents = generateMarkdownContents(commandTypes, loadedExtensions, language)
						val translatedPath = Path(path.toString().replace(".md", "-${language.toLanguageTag()}.md"))
						val writer = translatedPath.bufferedWriter(Charsets.UTF_8)
						withContext(Dispatchers.IO) {
							writer.write("")
							writer.write(contents)
							writer.flush()
							writer.close()
						}
						generatorLogger.info("Written documents for ${language.toLanguageTag()}!")
					}
				} else {
					val contents = generateMarkdownContents(commandTypes, loadedExtensions)
					val writer = path.bufferedWriter(Charsets.UTF_8)
					withContext(Dispatchers.IO) {
						writer.write("")
						writer.write(contents)
						writer.flush()
						writer.close()
					}
					generatorLogger.info("Written documents!")
				}
			}

			SupportedFileFormat.TEXT -> generateTextContents(commandTypes, loadedExtensions) // TODO funni translations
		}
	}
}
