/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hyacinthbots.docgenerator.generator.ConverterFormatter
import org.hyacinthbots.docgenerator.generator.DocsGenerator
import java.io.IOException
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.createFile
import kotlin.io.path.exists

/**
 * Searches for the target [path] and either finds it, creates it or complains.
 *
 * @param path The path to search
 */
internal suspend inline fun findOrCreateDocumentsFile(path: Path) {
	if (!path.exists()) {
		DocsGenerator.generatorLogger.debug("File does not exist, creating...")
		try {
			withContext(Dispatchers.IO) {
				path.createFile()
			}
		} catch (e: IOException) {
			DocsGenerator.generatorLogger.error(e) { e.message }
			return
		}
		DocsGenerator.generatorLogger.debug("File created successfully...")
	}
}

/**
 * Extension function for translating strings to a language.
 *
 * @param provider The translation provider
 * @param language The [Locale] to translate into
 * @param bundle The bundle that has the translation, `doc-generator` by default
 */
internal fun String.translate(
	provider: TranslationsProvider,
	language: Locale?,
	bundle: String? = DEFAULT_BUNDLE_NAME
): String =
	provider.translate(this, language ?: SupportedLocales.ENGLISH, bundle)

/**
 * Extension function for formatting a [MutableSet] of [Permission]s into a string, possible localised if necessary.
 *
 * @param language The [Locale] to translate into
 *
 * @return A [String] list of the required permissions
 */
internal fun MutableSet<Permission>.formatPermissionsSet(language: Locale?): String {
	val permissionsSet: MutableSet<String> = mutableSetOf()
	this.forEach {
		permissionsSet.add(it.translate(language ?: SupportedLocales.ENGLISH))
	}

	return permissionsSet.toString().replace("[", "").replace("]", "")
}

/**
 * Extension function for formatting [Permissions] into a string, possibly localised if necessary.
 *
 * @param language The [Locale] to translate into
 *
 * @return A [String] list of the required permissions, or null if there are non
 */
internal fun Permissions?.formatPermissionsSet(language: Locale?): String? {
	this ?: return null
	val permissionsSet: MutableSet<String> = mutableSetOf()

	this.values.forEach { perm ->
		permissionsSet.add(
			Permission.values.find { it.code.value == perm.code.value }
				?.translate(language ?: SupportedLocales.ENGLISH)
				?: Permission.Unknown().translate(language ?: SupportedLocales.ENGLISH)
		)
	}
	return permissionsSet.toString().replace("[", "").replace("]", "")
}

/**
 * function for formatting the arguments of a command into a nice string.
 *
 * @param arg The argument to translate
 * @param subCommand Whether this is part of a subcommand or not
 * @param provider The translation provider
 * @param bundle The bundle to get the translations from
 * @param language the [Locale] to translate into
 */
internal fun formatArguments(
	arg: Argument<*>,
    subCommand: Boolean,
    provider: TranslationsProvider,
    bundle: String?,
    language: Locale?
): String =
	// Sub commands require and extra tab of indentation
	if (subCommand) {
		"\t\t\t* **${"header.arguments.name".translate(provider, language)}**: " +
				"${arg.displayName.translate(provider, language, bundle)}\n" +

				"\t\t\t* **${"header.arguments.description".translate(provider, language)}**: " +
				"${arg.description.translate(provider, language, bundle)}\n" +

				"\t\t\t* **${"header.arguments.type".translate(provider, language)}**: ${
					if (language != null) {
						ConverterFormatter(
							"${arg.converter}", arg.converter.signatureTypeString, language
						).formatConverter(language)
					} else {
						ConverterFormatter(
							"${arg.converter}", arg.converter.signatureTypeString
						).formatConverter()
					}
				}\n"
	} else {
		"\t\t* **${"header.arguments.name".translate(provider, language)}**: " +
				"${arg.displayName.translate(provider, language, bundle)}\n" +

				"\t\t* **${"header.arguments.description".translate(provider, language)}**: " +
				"${arg.description.translate(provider, language, bundle)}\n" +

				"\t\t* **${"header.arguments.type".translate(provider, language)}**: ${
					if (language != null) {
						ConverterFormatter(
							"${arg.converter}", arg.converter.signatureTypeString, language
						).formatConverter(language)
					} else {
						ConverterFormatter(
							"${arg.converter}", arg.converter.signatureTypeString
						).formatConverter()
					}
				}\n"
	}

/** The name of the bundle containing this projects translations. */
internal const val DEFAULT_BUNDLE_NAME = "doc-generator"
