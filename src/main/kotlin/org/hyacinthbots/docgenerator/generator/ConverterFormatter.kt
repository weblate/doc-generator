/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.generator

import com.kotlindiscord.kord.extensions.i18n.ResourceBundleTranslations
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import org.hyacinthbots.docgenerator.DEFAULT_BUNDLE_NAME
import org.hyacinthbots.docgenerator.excpetions.InvalidConverterException
import org.hyacinthbots.docgenerator.translate
import java.util.*

private const val STANDARD_PREFIX: String = "com.kotlindiscord.kord.extensions.commands.converters"
private const val APPLICATION_PREFIX: String = "com.kotlindiscord.kord.extensions.commands.application.slash.converters"

// TODO docs maybe? Internal so idk
internal class ConverterFormatter(
	private val converterString: String,
	private val signatureType: String,
	private val language: Locale = SupportedLocales.ENGLISH
) : KordExKoinComponent {

	private val translationsProvider: TranslationsProvider = ResourceBundleTranslations { language }

	private val cleanConverterString: String =
		if (converterString.contains("commands.converters.impl.")) {
			converterString.removePrefix("$STANDARD_PREFIX.impl.").split("@")[0]
		} else if (converterString.contains("commands.application.slash")) {
			converterString.removePrefix("$APPLICATION_PREFIX.impl.").split("@")[0]
		} else {
			converterString.removePrefix("$STANDARD_PREFIX.").split("@")[0]
		}

	private val nonObviousConverterMap: Map<String, String> = mapOf(
		"CoalescingToDefaultingConverter" to "Coalescing Defaulting",
		"CoalescingToOptionalConverter" to "Coalescing Optional",
		"SingleToDefaultingConverter" to "Defaulting",
		"SingleToListConverter" to "List",
		"SingleToOptionalConverter" to "Optional"
	)

	private val indicativeConverterMap: Map<String, String> = mapOf(
		"AttachmentConverter" to "Attachment",
		"BooleanConverter" to "Boolean",
		"ChannelConverter" to "Channel",
		"ColorConverter" to "Color",
		"DecimalConverter" to "Decimal",
		"DurationConverter" to "Duration",
		"DurationCoalescingConverter" to "Coalescing Duration",
		"EmailConverter" to "Email",
		"EmojiConverter" to "Emoji",
		"EnumConverter" to "Enum",
		"GuildConverter" to "Guild",
		"IntConverter" to "Int",
		"LongConverter" to "Long",
		"MemberConverter" to "Member",
		"MessageConverter" to "Message",
		"RegexConverter" to "Regex",
		"RegexCoalescingConverter" to "Coalescing Regex",
		"RoleConverter" to "Role",
		"SnowflakeConverter" to "Snowflake",
		"StringCoalescingConverter" to "Coalescing String",
		"StringConverter" to "String",
		"SupportedLocaleConverter" to "Supported Locale",
		"TimestampConverter" to "Timestamp",
		"UserConverter" to "User",

		// Slash Converters

		"StringChoiceConverter" to "String Choice",
		"NumberChoiceConverter" to "Number Choice",
		"EnumChoiceConverter" to "Enum Choice"
	)

	private val signatureToType: Map<String, String> = mapOf(
		"attachment" to translationsProvider.translate("signature.attachment", bundleName = DEFAULT_BUNDLE_NAME),
		"yes/no" to translationsProvider.translate("signature.boolean", bundleName = DEFAULT_BUNDLE_NAME),
		"channel" to translationsProvider.translate("signature.channel", bundleName = DEFAULT_BUNDLE_NAME),
		"color" to translationsProvider.translate("signature.color", bundleName = DEFAULT_BUNDLE_NAME),
		"decimal" to translationsProvider.translate("signature.decimal", bundleName = DEFAULT_BUNDLE_NAME),
		"duration" to translationsProvider.translate("signature.duration", bundleName = DEFAULT_BUNDLE_NAME),
		"email" to translationsProvider.translate("signature.email", bundleName = DEFAULT_BUNDLE_NAME),
		"server emoji" to translationsProvider.translate("signature.emoji", bundleName = DEFAULT_BUNDLE_NAME),
		"server" to translationsProvider.translate("signature.server", bundleName = DEFAULT_BUNDLE_NAME),
		"number" to translationsProvider.translate("signature.number", bundleName = DEFAULT_BUNDLE_NAME),
		"member" to translationsProvider.translate("signature.member", bundleName = DEFAULT_BUNDLE_NAME),
		"message" to translationsProvider.translate("signature.message", bundleName = DEFAULT_BUNDLE_NAME),
		"regex" to translationsProvider.translate("signature.regex", bundleName = DEFAULT_BUNDLE_NAME),
		"regexes" to translationsProvider.translate("signature.regexes", bundleName = DEFAULT_BUNDLE_NAME),
		"role" to translationsProvider.translate("signature.role", bundleName = DEFAULT_BUNDLE_NAME),
		"ID" to translationsProvider.translate("signature.snowflake", bundleName = DEFAULT_BUNDLE_NAME),
		"text" to translationsProvider.translate("signature.string", bundleName = DEFAULT_BUNDLE_NAME),
		"locale name/code" to translationsProvider.translate("signature.locale", bundleName = DEFAULT_BUNDLE_NAME),
		"timestamp" to translationsProvider.translate("signature.timestamp", bundleName = DEFAULT_BUNDLE_NAME),
		"user" to translationsProvider.translate("signature.user", bundleName = DEFAULT_BUNDLE_NAME)
	)

	fun formatConverter(language: Locale? = null): String {
		val translatedSignatureType = translationsProvider.translate(signatureType, SupportedLocales.ENGLISH)

		return if (converterString.contains(".impl.")) {
			indicativeConverterMap[cleanConverterString] ?: throw InvalidConverterException(cleanConverterString)
		} else {
			"${nonObviousConverterMap[cleanConverterString]} ${
				(signatureToType[translatedSignatureType] ?: signatureType).translate(
					translationsProvider, language, DEFAULT_BUNDLE_NAME
				)
			}"
		}
	}
}
