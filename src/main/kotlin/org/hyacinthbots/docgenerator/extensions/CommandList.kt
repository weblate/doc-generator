/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.pagination.PublicResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import org.hyacinthbots.docgenerator.formatArguments
import org.hyacinthbots.docgenerator.formatPermissionsSet
import org.hyacinthbots.docgenerator.subCommandAdditionalDocumentation
import org.hyacinthbots.docgenerator.translate

public class CommandList : Extension() {
	override val name: String = "command-list"

	override suspend fun setup() {
		val pagesObj = Pages()

		@Suppress("DuplicatedCode")
		bot.extensions.values.toMutableList().forEach {
			it.slashCommands.forEach { slashCommand ->
				if (slashCommand.subCommands.isNotEmpty()) {
					slashCommand.subCommands.forEach { subCommand ->
						var arguments: String? = ""
						val provider = subCommand.translationsProvider
						val bundle = subCommand.extension.bundle
						subCommand.arguments?.invoke()?.args?.forEach { arg ->
							arguments += formatArguments(arg, provider, bundle, null)
						}
						if (arguments?.isEmpty() == true) arguments = null
						var subExtraDocs = subCommand.subCommandAdditionalDocumentation[subCommand.name]
						pagesObj.addPage(
							Page {
								title = "${
									subCommand.parentCommand?.name?.translate(provider, null, bundle)
								} ${subCommand.name.translate(provider, null, bundle)}"
								description = subCommand.description
								field {
									name = "Arguments"
									value = arguments ?: "None"
								}
								field {
									name = "Permissions"
									value = if (subCommand.requiredPerms.isNotEmpty()) {
										"* ${"header.permissions.bot".translate(provider, null, bundle)}:${
											subCommand.requiredPerms.formatPermissionsSet(null)
										}\n"
									} else {
										"None"
									}
								}
								if (subExtraDocs?.commandResult != null) {
									field {
										name = "Result"
										value = "* **${
											"header.result".translate(provider, null, bundle)
										}**:${
											subExtraDocs!!.commandResult!!.translate(provider, null, bundle)
										}\n"
									}
								}
							}
						)
						subExtraDocs = null
					}
				} else {
					var arguments: String? = ""
					val provider = slashCommand.translationsProvider
					val bundle = slashCommand.extension.bundle
					slashCommand.arguments?.invoke()?.args?.forEach { arg ->
						arguments += formatArguments(arg, provider, bundle, null)
					}
					if (arguments?.isEmpty() == true) arguments = null
					var subExtraDocs = slashCommand.subCommandAdditionalDocumentation[slashCommand.name]
					pagesObj.addPage(
						Page {
							title = slashCommand.name.translate(provider, null, bundle)
							description = slashCommand.description.translate(provider, null, bundle)
							field {
								name = "Arguments"
								value = arguments ?: "None"
							}
							field {
								name = "Permissions"
								value = if (slashCommand.requiredPerms.isNotEmpty()) {
									"* ${"header.permissions.bot".translate(provider, null, bundle)
									}:${slashCommand.requiredPerms.formatPermissionsSet(null)}\n"
								} else {
									"None"
								}
							}
							if (subExtraDocs?.commandResult != null) {
								field {
									name = "Result"
									value =
										"* **${"header.result".translate(provider, null, bundle)}**:${
											subExtraDocs!!.commandResult!!.translate(provider, null, bundle)
										}\n"
								}
							}
						}
					)
					subExtraDocs = null
				}
			}
		}

		publicSlashCommand {
			name = "command-list"
			description = "Shows a list of ${kord.getSelf().username}'s commands!"

			action {
				val paginator = PublicResponsePaginator(
					pages = pagesObj,
					keepEmbed = true,
					owner = event.interaction.user,
					timeoutSeconds = 500,
					locale = this.getLocale(),
					interaction = interactionResponse
				)

				paginator.send()
			}
		}
	}
}
