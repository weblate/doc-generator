/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.builder

import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import org.hyacinthbots.docgenerator.annotations.ConfigurationBuilderDSL
import java.util.*

@ConfigurationBuilderDSL
public open class TranslationSupportConfigurationBuilder {
	public open var enableTranslations: Boolean = false

	public open var baseLanguage: Locale = SupportedLocales.ENGLISH

	public open var supportedLanguages: MutableList<Locale> = mutableListOf(baseLanguage)
}
