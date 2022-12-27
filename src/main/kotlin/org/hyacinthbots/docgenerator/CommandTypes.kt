/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator

/**
 * The types of commands that can be documented.
 *
 * **Note**: Chat commands are not included as they are being phased out of Discord.
 *
 * @property SLASH Slash Commands
 * @property MESSAGE Message commands
 * @property USER User commands
 */
public sealed class CommandTypes {
	internal object SLASH : CommandTypes()
	internal object MESSAGE : CommandTypes()
	internal object USER : CommandTypes()
	public companion object {
		public fun values(): Array<CommandTypes> = arrayOf(SLASH, MESSAGE, USER)

		public fun valueOf(value: String): CommandTypes =
			when (value) {
				"SLASH" -> SLASH
				"MESSAGE" -> MESSAGE
				"USER" -> USER
				else -> throw IllegalArgumentException("No object org.hyacinthbots.docgenerator.CommandTypes.$value")
			}
	}
}
