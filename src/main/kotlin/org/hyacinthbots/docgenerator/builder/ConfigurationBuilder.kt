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
import org.hyacinthbots.docgenerator.enums.CommandTypes
import org.hyacinthbots.docgenerator.enums.SupportedFileFormat
import java.nio.file.Path

@ConfigurationBuilderDSL
public open class ConfigurationBuilder {
	public open var enabled: Boolean = false

	public open lateinit var fileFormat: SupportedFileFormat

	public open lateinit var commandTypes: MutableList<CommandTypes>

	public open lateinit var filePath: Path

	public open lateinit var environment: String

	public val translationSupport: TranslationSupportConfigurationBuilder = TranslationSupportConfigurationBuilder()

	public fun translationSupport(builder: TranslationSupportConfigurationBuilder.() -> Unit) {
		builder(translationSupport)
	}
}
