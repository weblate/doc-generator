/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.builder

import org.hyacinthbots.docgenerator.annotations.ConfigurationBuilderDSL
import java.util.Locale

/**
 * Builder class for configuring the translation support.
 */
@ConfigurationBuilderDSL
public open class TranslationSupportBuilder {
	/**
	 * Whether to enable translations.
	 */
	public open var enableTranslations: Boolean = false

	/**
	 * The languages to support. Defaults to English.
	 */
	public open var supportedLanguages: List<Locale> = emptyList()
}
