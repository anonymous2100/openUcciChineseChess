package com.ctgu.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChessConfig
{
	private Boolean playBgSound;
	private Boolean computerFirst;
	private Integer timeLimit;
	private Integer engine;
	private Integer coordinate;
	private Integer pieces;
	private Integer board;
}
