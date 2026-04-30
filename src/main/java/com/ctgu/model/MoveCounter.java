package com.ctgu.model;

import com.ctgu.enums.Side;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveCounter
{
	public int fullMove;
	public int halfMove;

	public void copy(MoveCounter other)
	{
		this.halfMove = other.halfMove;
		this.fullMove = other.fullMove;
	}

	public void update(Move move, Side side)
	{
		if (move.isCaptured())
		{
			this.halfMove = 0;
		}
		else
		{
			this.halfMove++;
		}
		if (this.fullMove == 0)
		{
			this.fullMove++;
		}
		else if (side.isBlack())
		{
			this.fullMove++;
		}
	}

	public String getFenCounterMarks()
	{
		return new StringBuffer().append(this.halfMove).append(' ').append(this.fullMove).toString();
	}

	public boolean load(String counterMark)
	{
		int splitPos = counterMark.indexOf(32);
		if (splitPos < 0 || counterMark.length() <= splitPos + 1)
		{
			return false;
		}
		try
		{
			this.halfMove = Integer.parseInt(counterMark.substring(0, splitPos));
			this.fullMove = Integer.parseInt(counterMark.substring(splitPos + 1));
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}

	public String toString()
	{
		return String.format("%d %d", new Object[] { Integer.valueOf(this.fullMove), Integer.valueOf(this.halfMove) });
	}
}
