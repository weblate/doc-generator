/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.generator

import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import org.hyacinthbots.docgenerator.excpetions.InvalidConverterException
import org.koin.core.component.inject

private const val STANDARD_PREFIX: String = "com.kotlindiscord.kord.extensions.commands.converters"
private const val APPLICATION_PREFIX: String = "com.kotlindiscord.kord.extensions.commands.application.slash.converters"

internal class ConverterFormatter(
	private val converterString: String,
	private val signatureType: String
) : KordExKoinComponent {

	private val translationsProvider: TranslationsProvider by inject()

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
		"attachment" to "Attachment",
		"yes/no" to "Boolean",
		"channel" to "Channel",
		"color" to "Color",
		"decimal" to "Decimal",
		"duration" to "Duration",
		"email" to "Email",
		"server emoji" to "Emoji",
		"server" to "Guild",
		"number" to "Int/Long",
		"member" to "Member",
		"message" to "Message",
		"regex" to "Regex",
		"regexes" to "Coalescing Regex",
		"role" to "Role",
		"ID" to "Snowflake",
		"text" to "String",
		"locale name/code" to "Supported Locale",
		"timestamp" to "Timestamp",
		"user" to "User"
	)

	fun formatConverter(): String {
		val translatedSignatureType = translationsProvider.translate(signatureType, SupportedLocales.ENGLISH)

		return if (converterString.contains(".impl.")) {
			indicativeConverterMap[cleanConverterString] ?: throw InvalidConverterException(cleanConverterString)
		} else {
			"${nonObviousConverterMap[cleanConverterString]} ${signatureToType[translatedSignatureType] ?: signatureType}"
		}
	}
}
