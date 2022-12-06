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

internal fun String.translate(
	provider: TranslationsProvider,
	language: Locale?,
	bundle: String? = DEFAULT_BUNDLE_NAME
): String =
	if (language != null) {
		provider.translate(this, language, bundle)
	} else {
		this
	}

internal fun MutableSet<Permission>.formatPermissionsSet(language: Locale?): MutableSet<String> {
	val permissionsSet: MutableSet<String> = mutableSetOf()
	this.forEach {
		permissionsSet.add(it.translate(language ?: SupportedLocales.ENGLISH))
	}

	return permissionsSet
}

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

internal fun formatArguments(
	arg: Argument<*>,
    subCommand: Boolean,
    provider: TranslationsProvider,
    bundle: String?,
    language: Locale?
): String =
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

internal const val DEFAULT_BUNDLE_NAME = "doc-generator"
