/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import mu.KotlinLogging
import org.hyacinthbots.docgenerator.annotations.ConfigurationBuilderDSL
import org.hyacinthbots.docgenerator.builder.ConfigurationBuilder
import org.hyacinthbots.docgenerator.enums.Environment
import org.hyacinthbots.docgenerator.excpetions.InvalidEnvironmentVariableException
import org.hyacinthbots.docgenerator.generator.DocsGenerator

internal val dslLogger = KotlinLogging.logger {}

@ConfigurationBuilderDSL
public suspend fun ExtensibleBotBuilder.docsGenerator(
	builder: suspend ConfigurationBuilder.() -> Unit
) {
	val action = ConfigurationBuilder()
	action.builder()

	hooks {
		afterExtensionsAdded {
			if (!action.enabled) {
				dslLogger.debug("Doc generation disabled, not generating!")
				return@afterExtensionsAdded
			}

			when (action.environment) {
				Environment.PRODUCTION.value -> {
					dslLogger.debug("Production environment detected, not generating!")
					return@afterExtensionsAdded
				}

				Environment.DEVELOPMENT.value -> {
					if (action.commandTypes.isEmpty()) {
						dslLogger.error("No command types have been specified! Please specify command types to document")
						return@afterExtensionsAdded
					}

					DocsGenerator.updateDocumentsFile(
						action.fileOutputLocation,
						action.fileFormat,
						action.commandTypes,
						extensions.values.toMutableList()
					)
				}

				else -> throw InvalidEnvironmentVariableException("environment", action.environment)
			}
		}
	}
}
