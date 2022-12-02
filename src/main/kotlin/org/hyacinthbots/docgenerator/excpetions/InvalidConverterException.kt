/*
 * Copyright (c) 2022 HyacinthBots <hyacinthbots@outlook.com>
 *
 * This file is part of doc-generator.
 *
 * Licensed under the MIT license. For more information,
 * please see the LICENSE file or https://mit-license.org/
 */

package org.hyacinthbots.docgenerator.excpetions

public class InvalidConverterException(converter: String) : Exception(
	"Invalid/Unsupported converter type: $converter. Please report this to the developers."
)
