/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.generator

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.hyacinthbots.docgenerator.additionalDocumentation
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.enums.SupportedFileFormat
import org.hyacinthbots.docgenerator.excpetions.ConflictingFileFormatException
import org.hyacinthbots.docgenerator.generator.DocsGenerator.translate
import org.hyacinthbots.docgenerator.subCommandAdditionalDocumentation
import java.io.IOException
import java.nio.file.Path
import java.util.Locale
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
		loadedExtensions: MutableList<Extension>,
		language: Locale? = null
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
						val parentProvider = slashCommand.translationsProvider
						if (slashCommand.subCommands.isNotEmpty()) {
							val parentAdditionalDocs = slashCommand.additionalDocumentation
							commandInfo += "### ${"header.parentcommand.name".translate(parentProvider, language)}: `${
								slashCommand.name.translate(parentProvider, language, slashCommand.bundle)
							}`\n* **${"header.parentcommand.description".translate(parentProvider, language)}**: ${
								slashCommand.description.translate(parentProvider, language, slashCommand.bundle)
							}\n${
								// FIXME why do you not work on translated docs
								if (language != null && parentAdditionalDocs?.extraInformation != null) {
									"* **${"header.additionalinfo".translate(parentProvider, language)}**:${
										parentAdditionalDocs.extraInformation!!.translate(
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
								val subAdditionalDocs = subCommand.subCommandAdditionalDocumentation
								subCommand.arguments?.invoke()?.args?.forEach { arg ->
									arguments += formatArguments(
										arg, true, subProvider, subCommand.bundle, language
									)
								}

								if (arguments.isEmpty()) arguments = "arguments.none".translate(subProvider, language)

								commandInfo += "\t#### ${"header.subcommand.name".translate(subProvider, language)}: `${
									subCommand.name.translate(subProvider, language, subCommand.bundle)
								}`\n\t* **${"header.subcommand.description".translate(subProvider, language)}**: ${
									subCommand.description.translate(subProvider, language, subCommand.bundle)
								}\n\t\t* **${"header.arguments".translate(subProvider, language)}**:\n$arguments\n${
									if (language != null && subAdditionalDocs?.commandResult != null) {
										"* **${"header.result".translate(subProvider, language)}**:${
											subAdditionalDocs.commandResult!!.translate(
												subProvider, language, subCommand.bundle
											)
										}\n"
									} else {
										""
									}
								}${
									if (language != null && subAdditionalDocs?.extraInformation != null) {
										"* **${"header.additionalinfo".translate(subProvider, language)}**:${
											subAdditionalDocs.extraInformation!!.translate(
												subProvider, language, subCommand.bundle
											)
										}\n"
									} else {
										""
									}
								}"
								subCommand.subCommandAdditionalDocumentation = null
							}
							slashCommand.additionalDocumentation = null
						} else {
							var arguments = ""
							val slashProvider = slashCommand.translationsProvider
							val additionalDocs = slashCommand.additionalDocumentation
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
								}\n\t* ${"header.arguments".translate(slashProvider, language)}:\n$arguments\n${
									if (language != null && additionalDocs?.commandResult != null) {
										"* **${"header.result".translate(slashProvider, language)}**:${
											additionalDocs.commandResult!!.translate(slashProvider, language, bundle)
										}\n"
									} else {
										""
									}
								}${
									if (language != null && additionalDocs?.extraInformation != null) {
										"* **${"header.additionalinfo".translate(slashProvider, language)}**:${
											additionalDocs.extraInformation!!.translate(slashProvider, language, bundle)
										}\n"
									} else {
										""
									}
								}"

							slashCommand.additionalDocumentation = null
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
						val additionalDocs = messageCommand.additionalDocumentation
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
							}"
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
						val additionalDocs = userCommand.additionalDocumentation
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
							}"
					}

					totalOutput += output
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
		loadedExtensions: MutableList<Extension>,
		languages: MutableList<Locale>? = null
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

	private fun formatArguments(
		arg: Argument<*>,
		subCommand: Boolean,
		translationsProvider: TranslationsProvider,
		bundle: String?,
		language: Locale?
	): String =
		if (subCommand) {
			"\t\t\t* **${"header.arguments.name".translate(translationsProvider, language)}**: " +
					"${arg.displayName.translate(translationsProvider, language, bundle)}\n" +

					"\t\t\t* **${"header.arguments.description".translate(translationsProvider, language)}**: " +
					"${arg.description.translate(translationsProvider, language, bundle)}\n" +

					"\t\t\t* **${"header.arguments.type".translate(translationsProvider, language)}**: ${
						if (language != null) {
							ConverterFormatter(
								"${arg.converter}", // I will cry
								arg.converter.signatureTypeString,
								language
							).formatConverter(language)
						} else {
							ConverterFormatter(
								"${arg.converter}", // I will cry
								arg.converter.signatureTypeString
							).formatConverter()
						}
					}\n"
		} else {
			"\t\t* **${"header.arguments.name".translate(translationsProvider, language)}**: " +
					"${arg.displayName.translate(translationsProvider, language, bundle)}\n" +

					"\t\t* **${"header.arguments.description".translate(translationsProvider, language)}**: " +
					"${arg.description.translate(translationsProvider, language, bundle)}\n" +

					"\t\t* **${"header.arguments.type".translate(translationsProvider, language)}**: ${
						if (language != null) {
							ConverterFormatter(
								"${arg.converter}", // I will cry
								arg.converter.signatureTypeString,
								language
							).formatConverter(language)
						} else {
							ConverterFormatter(
								"${arg.converter}", // I will cry
								arg.converter.signatureTypeString
							).formatConverter()
						}
					}\n"
		}

	internal fun String.translate(
		provider: TranslationsProvider,
		language: Locale?,
		bundle: String? = "doc-generator"
	): String =
		if (language != null) {
			provider.translate(this, language, bundle)
		} else {
			this
		}
}
