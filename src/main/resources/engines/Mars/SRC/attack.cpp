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
#include ".\attack.h"
#include "piece.h"
#include "move.h"

const char ProtectTab[512] = {
                       0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 2, 1, 2, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 2, 1, 2, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0, 0, 0
};
//if (ProtectTab[from - to + 256] == 1) {

const char AttackTab[512] = {
                          0,  0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,-16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,-16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,-16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,-16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,-16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,-16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,-16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,-16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,-16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0,-1, -1,-1, -1,-1,  0,  1,  1, 1, 1, 1, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0, 16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0, 16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0, 16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0, 16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0, 16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0, 16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0, 16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0, 16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0, 16,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0,  0,  0,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0,  0
};

/*

 0, 0, 0, 0, 0,  0,-33,-16,-31,  0, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,-18,  0,-16,  0,-14, 0, 0, 0, 0, 0, 0,
  0, 0,-1, -1,-1, -1,-1,  0,  1,  1, 1, 1, 1, 0, 0, 0,
  0, 0, 0, 0, 0, 14,  0, 16,  0, 18, 0, 0, 0, 0, 0, 0,
  0, 0, 0, 0, 0,  0, 31, 16, 33,  0, 0, 0, 0, 0, 0, 0,
const int c_Rook[4] = {-16, -1, 1, 16};
const int c_Knight[8] = {-33, -31, -18, -14, 14, 18, 31, 33};

int AttackTab[512];

void attack_init()
{
	int i;
	int delta, inc;
    int piece;
    int dir, dist;
	for(i=0;i<512;++i)
	{
		AttackTab[i]=0;
	}
	for (dir = 0; dir < 8; dir++) {

      delta = c_Knight[dir];
      AttackTab[256+delta] = delta;
   }

   for (dir = 0; dir < 4; dir++) {

      inc = c_Rook[dir];

      for (dist = 1; dist < 10; dist++) {

         delta = inc*dist;
         AttackTab[256+delta] = inc;
      }
   }

}
*/

//to �Ƿ��ܵ�colour��ɫ�Ĺ���
bool is_attacked(int to, int colour) 
{
	int i, piece_tag, from, disp, x, y,pawn;
    slide_mask_t *rank_mask_ptr, *file_mask_ptr;
    // ���ӱ����жϰ������¼������裺

    piece_tag =colour<<4;

	//pawn=PawnFlag|(1<<(5+colour));
    pawn=PawnFlag | (32 <<colour);

  if (colour == 0 ? (to & 0x80) != 0 : (to & 0x80) == 0) {
    if (c_InCity[to]) {

      // 1. �ж��ܵ�˧(��)�ı���
      from = Piece[piece_tag];
      if (from != 0) {
        if (ProtectTab[from - to + 256] == 1) {
          return true;
        }
      }

      // 2. �ж��ܵ���(ʿ)�ı���
      for (i = 1; i <= 2; i ++) {
        from = Piece[piece_tag + i];
        if (from != 0 ) {
          if (ProtectTab[from - to + 256] == 2) {
            return true;
          }
        }
      }
    }

    // 3. �ж��ܵ���(��)�ı���
    for (i = 3; i <= 4; i ++) {
      from = Piece[piece_tag + i];
      if (from != 0 ) {
        if (ProtectTab[from - to + 256] == 3 && Square[(to + from) >> 1] == 0) {
          return true;
        }
      }
    }
  } else {

    // 4. �ж��ܵ����ӱ�(��)����ı���
    for (from = to - 1; from <= to + 1; from += 2) {
		if((Square[from]&pawn)==pawn)
		{
			return true;
		}
    }
  }

  // 5. �ж��ܵ����ӱ�(��)����ı���
  from = to + (colour == 0 ? 16 : -16);
  if((Square[from]&pawn)==pawn)
  {
	  return true;
  }

  // 6. �ж��ܵ���ı���
  for (i = 5; i <= 6; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      disp = c_HorseLegTab[to - from + 256];
      if (disp != 0 && Square[from + disp]==0) {
        return true;
      }
    }
  }

  x = to & 0xf;
  y = to >> 4;
  rank_mask_ptr = g_RankMaskTab[x - 3] + BitRanks[y];
  file_mask_ptr = g_FileMaskTab[y - 3] + BitFiles[x];

  // 7. �ж��ܵ����ı���������"struct.cpp"���"Checked()"����
  for (i = 7; i <= 8; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0 && from != to) {
      if (x == (from & 0xf)) {
        if ((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0) {
          return true;
        }
      } else if (y == (from >> 4)) {
        if ((rank_mask_ptr->rook_cap & g_BitRankMask[from]) != 0) {
          return true;
        }
      }
    }
  }

  // 8. �ж��ܵ��ڵı���������"struct.cpp"���"Checked()"����
  for (i = 9; i <= 10; i ++) {
    from = Piece[piece_tag + i];
    if (from!=0 && from != to) {
      if (x == (from & 0xf)) {
        if ((file_mask_ptr->cannon_cap & g_BitFileMask[from]) != 0) {
          return true;
        }
      } else if (y == (from >> 4)) {
        if ((rank_mask_ptr->cannon_cap & g_BitRankMask[from]) != 0) {
          return true;
        }
      }
    }
  }
  return false;
}
//�жϽ��Ƿ��ܵ�ǣ��
bool is_pinned(int colour)
{
	int i, piece_tag, from,to, disp, x, y,me_flag;
    slide_mask_t *rank_mask_ptr, *file_mask_ptr;

    piece_tag =16-(colour<<4);
	to=Piece[(colour<<4)];
 
	me_flag=COLOUR_FLAG(colour);

    x = to & 0xf;
    y = to >> 4;
    rank_mask_ptr = g_RankMaskTab[x - 3] + BitRanks[y];
    file_mask_ptr = g_FileMaskTab[y - 3] + BitFiles[x];

	//�����ڡ�������ǣ��
   for (i = 9; i <= 10; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0 && from != to) {
      if ((x == (from & 0xf)) && (x == (to & 0xf))) {
        if (((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0) || ((file_mask_ptr->super_cap & g_BitFileMask[to]) != 0)) {
          return true;
        }
      } else if ((y == (from >> 4)) && (y == (to >> 4))) {
        if (((rank_mask_ptr->rook_cap & g_BitRankMask[from]) != 0) || ((rank_mask_ptr->super_cap & g_BitRankMask[to]) != 0)) {
          return true;
        }
      }
     }
   }

	//��ǣ��
    from=Piece[piece_tag];
    if (from != 0 && from != to) {
      if ((x == (from & 0xf)) && (x == (to & 0xf))) {
        if (((file_mask_ptr->cannon_cap & g_BitFileMask[from]) != 0)) {
          return true;
        }
      } 
    }
	// 7. ����ǣ�Ƶ�����
   for (i = 7; i <= 8; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0 && from != to) {
      if ((x == (from & 0xf)) && (x == (to & 0xf))) {
        if (((file_mask_ptr->cannon_cap & g_BitFileMask[from]) != 0)) {
          return true;
        }
      } else if ((y == (from >> 4)) && (y == (to >> 4))) {
        if (((rank_mask_ptr->cannon_cap & g_BitRankMask[from]) != 0)) {
          return true;
        }
      }
    }
  }
   	//���ǣ��
	for (i = 5; i <= 6; i ++) {
       from = Piece[piece_tag + i];
          if (from != 0) {
              disp = c_HorseLegTab[to - from + 256];
              if (disp != 0 && FLAG_IS(Square[from + disp],me_flag)) {
                  return true;
              }
           }
     }
	return false;
}

//�ж������Ƿ��ܵ�ǣ��
bool is_piece_pinned(int square, int colour) 
{
	int i, piece_tag, from,to, disp, x, y,inc;
    slide_mask_t *rank_mask_ptr, *file_mask_ptr;
    // ���ӱ����жϰ������¼������裺

    piece_tag =16-(colour<<4);

	to=Piece[colour<<4];

  // ����ǣ��
  for (i = 5; i <= 6; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      disp = c_HorseLegTab[to - from + 256];
      if (disp != 0 && Square[from + disp] && from + disp==square) {
        return true;
      }
    }
  }
  inc=AttackTab[square-to+256];
  if (inc == 0) return false;


  x = square & 0xf;
  y = square >> 4;
  rank_mask_ptr = g_RankMaskTab[x - 3] + BitRanks[y];
  file_mask_ptr = g_FileMaskTab[y - 3] + BitFiles[x];

  //˫������ǣ��
  from=Piece[piece_tag];
  if (from != 0 && from != square) {
      if ((x == (from & 0xf)) && (x == (to & 0xf))) {
        if (((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0) && ((file_mask_ptr->rook_cap & g_BitFileMask[to]) != 0) ) {
          return true;
        }
      } 
    }

  // 7. ����ǣ�Ƶ�����
  for (i = 7; i <= 8; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0 && from != square) {
      if ((x == (from & 0xf)) && (x == (to & 0xf))) {
        if (((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0) && ((file_mask_ptr->rook_cap & g_BitFileMask[to]) != 0)) {
          return true;
        }
      } else if ((y == (from >> 4)) && (y == (to >> 4))) {
        if (((rank_mask_ptr->rook_cap & g_BitRankMask[from]) != 0) && ((rank_mask_ptr->rook_cap & g_BitRankMask[to]) != 0)) {
          return true;
        }
      }
    }
  }


  //����ǣ��
  for (i = 9; i <= 10; i ++) {
    from = Piece[piece_tag + i];
    if (from!=0 && from != square) {
      if ((x == (from & 0xf)) && (x == (to & 0xf))) {
        if ((((file_mask_ptr->cannon_cap & g_BitFileMask[from]) != 0) && ((file_mask_ptr->rook_cap & g_BitFileMask[to]) != 0))
			|| (((file_mask_ptr->cannon_cap & g_BitFileMask[to]) != 0) && ((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0))) {
          return true;
        }
      } else if ((y == (from >> 4)) && (y == (to >> 4))) {
        if ((((rank_mask_ptr->cannon_cap & g_BitRankMask[from]) != 0) && ((rank_mask_ptr->rook_cap & g_BitRankMask[to]) != 0))
			|| (((rank_mask_ptr->cannon_cap & g_BitRankMask[to]) != 0) && ((rank_mask_ptr->rook_cap & g_BitRankMask[from]) != 0))) {
          return true;
        }
      }
    }
  }
  return false;
}

//�жϽ��Ƿ��ܵ�ǣ�� ������
bool is_cannon_pinned(int square, int colour) 
{
    int i, piece_tag, from,to, x, y,inc;
    slide_mask_t *rank_mask_ptr, *file_mask_ptr;
    // ���ӱ����жϰ������¼������裺

	to=Piece[colour<<4];

	inc=AttackTab[square-to+256];
	if (inc == 0) return false;

  piece_tag =16-(colour<<4);

  x = square & 0xf;
  y = square >> 4;
  rank_mask_ptr = g_RankMaskTab[x - 3] + BitRanks[y];
  file_mask_ptr = g_FileMaskTab[y - 3] + BitFiles[x];

  // 7. ������ǣ�Ƶ�����
  for (i = 9; i <= 10; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0 && from != square) {
      if ((x == (from & 0xf)) && (x == (to & 0xf))) {
        if (((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0) && ((file_mask_ptr->rook_cap & g_BitFileMask[to]) != 0)) {
          return true;
        }
      } else if ((y == (from >> 4)) && (y == (to >> 4))) {
        if (((rank_mask_ptr->rook_cap & g_BitRankMask[from]) != 0) && ((rank_mask_ptr->rook_cap & g_BitRankMask[to]) != 0)) {
          return true;
        }
      }
    }
  }

  return false;
}
//0��ʾû��ǣ��
bool is_knight_pinned(int move, int colour) 
{
	int i, piece_tag, from,to, disp,inc,square;
	int count;
	
	square=MOVE_FROM(move);

	to=Piece[colour<<4];
	inc=ProtectTab[square-to+256];
    if (inc != 2) return false;
 
	count=0;
	piece_tag =16-(colour<<4);
  // ����ǣ��
  for (i = 5; i <= 6; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      disp = c_HorseLegTab[to - from + 256];
      if (disp != 0 && Square[from + disp] && from + disp==square) {
		  if(from!=MOVE_TO(move)||(Square[from + disp]&RookFlag)==0)
		  {
			  return true;
		  }else
		  {
			  ++count;
		  }
      }
    }
  }
  if(count==2)
	  return true;

  return false;
}
bool is_filter_pinned(int move, int colour) 
{
	int i, piece_tag, from,to, x, y,inc,square;
	int move_to;
    slide_mask_t *rank_mask_ptr, *file_mask_ptr;    

	square=MOVE_FROM(move);
	to=Piece[colour<<4];

    inc=AttackTab[square-to+256];
    if (inc == 0) return false;

	move_to=MOVE_TO(move);

    piece_tag =16-(colour<<4);
    x = square & 0xf;
    y = square >> 4;
    rank_mask_ptr = g_RankMaskTab[x - 3] + BitRanks[y];
    file_mask_ptr = g_FileMaskTab[y - 3] + BitFiles[x];

  //˫������ǣ��
  from=Piece[piece_tag];
  if (from != 0 && from != square) {
      if ((x == (from & 0xf)) && (x == (from & 0xf))) {
        if (((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0) && ((file_mask_ptr->rook_cap & g_BitFileMask[to]) != 0) ) {
			if((from & 0xf)==(move_to & 0xf))
				return false;
			return true;
        }
      } 
    }

  // 7. ����ǣ�Ƶ�����
  for (i = 7; i <= 8; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0 && from != square) {
      if ((x == (from & 0xf)) && (x == (to & 0xf))) {
        if (((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0) && ((file_mask_ptr->rook_cap & g_BitFileMask[to]) != 0)) {
            if(x==(move_to & 0xf))//ͬһ��Ϊ�Ϸ�
			{
				if( Square[move_to]!=Empty && from!=move_to)//���ӱ����ǣ����
					return true;
				return false;
			}
			return true;
        }
      } else if ((y == (from >> 4)) && (y == (to >> 4))) {
        if (((rank_mask_ptr->rook_cap & g_BitRankMask[from]) != 0) && ((rank_mask_ptr->rook_cap & g_BitRankMask[to]) != 0)) {
          if(y==(move_to >> 4))
		  {
			  	if(Square[move_to]!=Empty && from!=move_to)
					return true;
				return false;
		  }
			return true;
        }
      }
    }
  }


  //����ǣ��
  for (i = 9; i <= 10; i ++) {
    from = Piece[piece_tag + i];
    if (from!=0 && from != square) {
      if ((x == (from & 0xf)) && (x == (to & 0xf))) {
        if ((((file_mask_ptr->cannon_cap & g_BitFileMask[from]) != 0) && ((file_mask_ptr->rook_cap & g_BitFileMask[to]) != 0))
			|| (((file_mask_ptr->cannon_cap & g_BitFileMask[to]) != 0) && ((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0))) 
		{
			if(x==(move_to & 0xf))
			{
			   if(Square[move_to]!=0 && from!=move_to)
				   return true;
				return false;
			}
          return true;
        }
      } else if ((y == (from >> 4)) && (y == (to >> 4))) {
        if ((((rank_mask_ptr->cannon_cap & g_BitRankMask[from]) != 0) && ((rank_mask_ptr->rook_cap & g_BitRankMask[to]) != 0))
			|| (((rank_mask_ptr->cannon_cap & g_BitRankMask[to]) != 0) && ((rank_mask_ptr->rook_cap & g_BitRankMask[from]) != 0))) 
		{
			if(y==(move_to >> 4))
			{
			  if(Square[move_to]!=0 && from!=move_to)
				  return true;
				return false;
			}
          return true;
        }
      }
    }
  }
  return false;
}
//��������������λ��
void attack_set(attack_t * attack)
{
	 int i, piece_tag, from, to, x, y, disp;
     slide_mask_t *rank_mask_ptr, *file_mask_ptr;
 
	 attack->dn=0;

	 piece_tag = 16-(Turn<<4);
     // �����жϰ������¼��������ݣ�
     // 1. �ж�˧(��)�Ƿ���������
     from = Piece[16-piece_tag];
     
	 if (from == 0) {
         return; 
     }
	   // 7. �ж��Ƿ񱻱�(��)����
   for (to = from - 1; to <= from + 1; to += 2) {
      if(PIECE_IS_PAWN(Square[to]))
	  {
		  attack->ds[attack->dn] = to;
          attack->dn++;
	  }
   }
   to= from - 16 + (Turn << 5);
   if(PIECE_IS_PAWN(Square[to]))
   {
		  attack->ds[attack->dn] = to;
          attack->dn++;
    }
     // 2. ���˧(��)���ڸ��ӵ�λ�к�λ��
     x = from & 0xf;
     y = from >> 4;
     rank_mask_ptr = g_RankMaskTab[x - 3] + BitRanks[y];
     file_mask_ptr = g_FileMaskTab[y - 3] + BitFiles[x];
     // 3. �ж��Ƿ�˧����
     /*
	 to = Piece[piece_tag];
     if (to != 0) {
        if (x == (to & 0xf) && (file_mask_ptr->rook_cap & g_BitFileMask[to]) != 0) {
              attack->ds[attack->dn] = to;
              attack->di[attack->dn] = 0;
              attack->dn++;
        }
     }
	 */
	 
  // 4. �ж��Ƿ�����
  for (i = 5; i <= 6; i ++) {
    to = Piece[piece_tag + i];
    if (to != 0) {
      disp = c_HorseLegTab[from - to + 256];
      if (disp != 0 && Square[to + disp] == 0) {
              attack->ds[attack->dn] = to;
              attack->dn++;
      }
    }
  }
 
  // 5. �ж��Ƿ񱻳�������˧����
  for (i = 7; i <= 8; i ++) {
    to = Piece[piece_tag + i];
    if (to != 0) {
      if (x == (to & 0xf)) {
        if ((file_mask_ptr->rook_cap & g_BitFileMask[to]) != 0) {
              attack->ds[attack->dn] = to;
              attack->dn++; 
        }
      } else if (y == (to >> 4)) {
        if ((rank_mask_ptr->rook_cap & g_BitRankMask[to]) != 0) {
              attack->ds[attack->dn] = to;
              attack->dn++; 
        }
      }
    }
  }

 // 6. �ж��Ƿ��ڽ���
  for (i = 9; i <= 10; i ++) {
    to = Piece[piece_tag + i];
    if (to != 0) {
      if (x == (to & 0xf)) {
        if ((file_mask_ptr->cannon_cap & g_BitFileMask[to]) != 0) {
              attack->ds[attack->dn] = to;
              attack->dn++;
        }
      } else if (y == (to >> 4)) {
        if ((rank_mask_ptr->cannon_cap & g_BitRankMask[to]) != 0) {
              attack->ds[attack->dn] = to;
              attack->dn++; 
        }
      }
    }
  }
}

//�Ƿ񱻽���
bool is_in_check(int colour)
{
     int i, piece_tag, from, to, x, y, disp;
     slide_mask_t *rank_mask_ptr, *file_mask_ptr;
 
	 piece_tag = 16-(colour<<4);
     // �����жϰ������¼��������ݣ�
     // 1. �ж�˧(��)�Ƿ���������
     from = Piece[colour<<4];
     
	 if (from == 0) {
         return true; 
     }
     // 2. ���˧(��)���ڸ��ӵ�λ�к�λ��
     x = from & 0xf;
     y = from >> 4;
     rank_mask_ptr = g_RankMaskTab[x - 3] + BitRanks[y];
     file_mask_ptr = g_FileMaskTab[y - 3] + BitFiles[x];
     // 3. �ж��Ƿ�˧����
     to = Piece[piece_tag];
     if (to != 0) {
        if (x == (to & 0xf) && (file_mask_ptr->rook_cap & g_BitFileMask[to]) != 0) {
               return true; 
        }
     }
  // 4. �ж��Ƿ�����
  for (i = 5; i <= 6; i ++) {
    to = Piece[piece_tag + i];
    if (to != 0) {
      disp = c_HorseLegTab[from - to + 256];
      if (disp != 0 && Square[to + disp] == 0) {
               return true; 
      }
    }
  }
  // 5. �ж��Ƿ񱻳�������˧����
  for (i = 7; i <= 8; i ++) {
    to = Piece[piece_tag + i];
    if (to != 0) {
      if (x == (to & 0xf)) {
        if ((file_mask_ptr->rook_cap & g_BitFileMask[to]) != 0) {
              return true; 
        }
      } else if (y == (to >> 4)) {
        if ((rank_mask_ptr->rook_cap & g_BitRankMask[to]) != 0) {
              return true; 
        }
      }
    }
  }
  // 6. �ж��Ƿ��ڽ���
  for (i = 9; i <= 10; i ++) {
    to = Piece[piece_tag + i];
    if (to != 0) {
      if (x == (to & 0xf)) {
        if ((file_mask_ptr->cannon_cap & g_BitFileMask[to]) != 0) {
             return true; 
        }
      } else if (y == (to >> 4)) {
        if ((rank_mask_ptr->cannon_cap & g_BitRankMask[to]) != 0) {
             return true; 
        }
      }
    }
  }
  // 7. �ж��Ƿ񱻱�(��)����
  for (to = from - 1; to <= from + 1; to += 2) {
	if(PIECE_IS_PAWN(Square[to]))
	{
		return true;
	}
  }
  to= from - 16 + (colour << 5);
  if(PIECE_IS_PAWN(Square[to]))
  {
		return true;
  }
    return false;
}

//�Ƿ񱻽���
//�ƶ��� �Ƿ��ܵ�colour��ɫ�Ľ��� �����ƶ�����ʱ��ʹ��
bool is_checked(int to, int colour) 
{
	int i, piece_tag, from, disp, x, y;
	int pawn,king;
    slide_mask_t *rank_mask_ptr, *file_mask_ptr;
    
    piece_tag =colour<<4;
    pawn=PawnFlag | (32 <<colour);

	king=Piece[16-piece_tag];

  if (colour == 0 ? (to & 0x80) != 0 : (to & 0x80) == 0) {
      ;
  } else {

    // 4. �ж��ܵ����ӱ�(��)����ı���
    for (from = to - 1; from <= to + 1; from += 2) {
		if((Square[from]&pawn)==pawn)
		{
			return true;
		}
    }
  }

  // 5. �ж��ܵ����ӱ�(��)����ı���
  from = to + (colour == 0 ? 16 : -16);
  if((Square[from]&pawn)==pawn)
  {
	  return true;
  }

  // 6. �ж��ܵ���ı���
  for (i = 5; i <= 6; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      disp = c_HorseLegTab[to - from + 256];
      if (disp != 0 && !Square[from + disp]) {
        return true;
      }
    }
  }

  x = to & 0xf;
  y = to >> 4;
  rank_mask_ptr = g_RankMaskTab[x - 3] + BitRanks[y];
  file_mask_ptr = g_FileMaskTab[y - 3] + BitFiles[x];

  // 7. �ж��ܵ����ı���������"struct.cpp"���"Checked()"����
  for (i = 7; i <= 8; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0 && from != to) {
      if (x == (from & 0xf)) {
        if ((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0) {
          return true;
        }
      } else if (y == (from >> 4)) {
        if ((rank_mask_ptr->rook_cap & g_BitRankMask[from]) != 0) {
          return true;
        }
      }
    }
  }

  //�Ƿ���˫�������Σ��
  from=Piece[piece_tag];
  if (from != 0 && from != to) {
      if (x == (from & 0xf)) {
        if ((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0) {
          return true;
        }
      } 
    }

  // 8. �ж��ܵ��ڵı���
  for (i = 9; i <= 10; i ++) {
    from = Piece[piece_tag + i];
    if (from!=0 && from != to) {
      if (x == (from & 0xf)) {
        if ((file_mask_ptr->cannon_cap & g_BitFileMask[from]) != 0) {
           if(to+AttackTab[from-to+256]==king)
			   return false;
           return true;
        }
      } else if (y == (from >> 4)) {
        if ((rank_mask_ptr->cannon_cap & g_BitRankMask[from]) != 0) {
		   if(to+AttackTab[from-to+256]==king)
			   return false;
           return true;
        }
      }
    }
  }
  return false;
}