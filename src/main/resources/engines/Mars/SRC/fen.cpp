/********************************************************************

	----------------------------------------------------------------
    ������֤ �� GPL
	��Ȩ���� (C) 2005-2008 �����˹������о�
	----------------------------------------------------------------
	��һ����������������������������������������GNU ͨ�ù�����
	��֤�������޸ĺ����·�����һ���򡣻��������֤�ĵڶ��棬���ߣ���
	�����ѡ�����κθ��µİ汾��

    ������һ�����Ŀ����ϣ�������ã���û���κε���������û���ʺ��ض�
	Ŀ�ص������ĵ���������ϸ����������GNUͨ�ù������֤��

    ��Ӧ���Ѿ��ͳ���һ���յ�һ��GNUͨ�ù������֤�ĸ�������Ŀ¼
	GPL.txt�ļ����������û�У�д�Ÿ���
    The Free Software Foundation, Inc.,  675  Mass Ave,  Cambridge,
    MA02139,  USA
	----------------------------------------------------------------
	�������ʹ�ñ����ʱ��ʲô������飬�������µ�ַ������ȡ����ϵ��

			http://www.jsmaster.com

	���ŵ���

			jschess##163.com
	----------------------------------------------------------------
	���ļ���;��	��
	
	  
	���ļ���д�ˣ�	
				�˽���			jschess##163.com
		
	���ļ��汾��	03
	����޸��ڣ�	2006-1-16
		  
	ע������E-Mail��ַ�е�##����@�滻����������Ϊ�˵��ƶ����E-Mail
	��ַ�ռ������
	----------------------------------------------------------------
	������ʷ��
			
		  2006-1		��һ�淢��

********************************************************************/
#include ".\fen.h"
#include "pregen.h"
#include "piece.h"
#include "random.h"
#include "pst.h"
#include "value.h"

void add_piece(int square,int piece)
{

	Square[square] = PieceFrom32[piece] | piece;

	Piece[piece] = square;

	Piece_size[PIECE_COLOUR(PieceFrom32[piece])]++;

	Number[PieceTo32[piece]]++;

	BitRanks[square >> 4] ^= g_BitRankMask[square];
	BitFiles[square & 0xf] ^= g_BitFileMask[square];
 
	int piece_key=PieceToKey32[piece];
	Key  ^= g_ZobristKeyTable[piece_key][square];
	Lock ^= g_ZobristLockTable[piece_key][square];

	//λ�÷�ͳ��
	if (piece_key < 5) {
        Opening +=g_PST[piece_key][square];
 	    Endgame +=g_PST[piece_key+5][square];
    } else {
	    Opening -=g_PST[piece_key-5][254 - square];
	    Endgame -=g_PST[piece_key][254 - square];
    }
	
    if(piece<16)
	{
		Opening+=MaterialPiece[piece];
		Endgame+=MaterialPiece[piece];
	}else
	{
		Opening-=MaterialPiece[piece];
		Endgame-=MaterialPiece[piece];
	}
}
int fen_piece(char arg) {
  switch (arg) {
  case 'K':
    return 0;
  case 'A':
    return 1;
  case 'B':
  case 'E':
    return 2;
  case 'N':
  case 'H':
    return 3;
  case 'R':
    return 4;
  case 'C':
    return 5;
  default: // ����'P'
    return 6;
  }
}
void board_from_fen( const char fen[])
{
    int i, j, k;
    int red_piece[7];
    int black_piece[7];
    const char *char_ptr;

    red_piece[0] = 0;
	red_piece[1] = 1;
    for (i = 2; i < 7; i ++) {
        red_piece[i] = ((i-1) << 1)+1;
    }
    for (i = 0; i < 7; i ++) {
        black_piece[i] = red_piece[i] + 16;
    }
	char_ptr=fen;
	board_clear();
	if (*char_ptr == '\0') {
      return;
    }
	
	// 2. ��ȡ�����ϵ�����
    i = 3;
    j = 3;
    while (*char_ptr != ' ') {
      if (*char_ptr == '/') {
        j = 3;
        i ++;
        if (i > 12) {
          break;
        }
      } else if (*char_ptr >= '1' && *char_ptr <= '9') {
        for (k = 0; k < (*char_ptr - '0'); k ++) {
          if (j >= 11) {
            break;
          }
          j ++;
        }
      } else if (*char_ptr >= 'A' && *char_ptr <= 'Z') {
        k = fen_piece(*char_ptr);
        if (j <= 11) {
          if (red_piece[k] < 16) {
            if (Piece[red_piece[k]] == 0) {
              add_piece((i << 4) + j, red_piece[k]);
              red_piece[k] ++;
            }
          }
        }
        j ++;
      } else if (*char_ptr >= 'a' && *char_ptr <= 'z') {
        k = fen_piece(*char_ptr + 'A' - 'a');
        if (j <= 11) {
          if (black_piece[k] < 32) {
            if (Piece[black_piece[k]] == 0) {
              add_piece((i << 4) + j, black_piece[k]);
              black_piece[k] ++;
            }
          }
        }
        j ++;
      }
      char_ptr ++;
      if (*char_ptr == '\0') {
        return;
      }
    }
    char_ptr ++;
    
    Turn = (*char_ptr == 'b' ? 1 : 0);
	if(Turn!=0)
	{
		Key ^= g_ZobristKeyPlayer;
        Lock ^= g_ZobristLockPlayer;
	}

}
bool board_to_fen( char fen[], int size) {
	return false;
}