/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.enums

/**
 * The types of commands that can be documented.
 *
 * **Note**: Chat commands are not included as they are being phased out of Discord.
 *
 * @property SLASH Slash Commands
 * @property MESSAGE Message commands
 * @property USER User commands
 */
public enum class CommandTypes {
	SLASH,
	MESSAGE,
	USER
}

/**
 * A convenience list containing all types of supported commands, for use in the configuration builder.
 */
public val ALL_COMMAND_TYPES: List<CommandTypes> = listOf(
	CommandTypes.SLASH,
	CommandTypes.MESSAGE,
	CommandTypes.USER
)
