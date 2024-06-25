package com.ctgu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IniFileEntity
{
	private String section;

	private String key;

	private String value;
}