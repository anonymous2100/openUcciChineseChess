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
#include ".\move_legal.h"
#include "piece.h"
#include "attack.h"
#include "board.h"
#include "move.h"

bool move_is_pseudo(int move) {

   int me, opp;
   int from, to;
   int piece, capture;
   int disp,x,y;

   // init

   me = Turn;
   opp = COLOUR_OPP(Turn);

   // from

   from = MOVE_FROM(move);

   piece = Square[from];
   if (!COLOUR_IS(piece,me)) return false;

   // to

   to = MOVE_TO(move);
   ASSERT(SQUARE_IS_OK(to));

   capture = Square[to];
   if (COLOUR_IS(capture,me)) return false;

   if(PIECE_IS_KING(piece))
   {
	   return c_InCity[to] && ProtectTab[to - from + 256]== 1;
   }else if(PIECE_IS_ADVISOR(piece))
   {
	   return c_InCity[to] && ProtectTab[to - from + 256]== 2;
   }else if(PIECE_IS_BISHOP(piece))
   {
        return ((from ^ to) & 0x80) == 0 && ProtectTab[to - from + 256] == 3 && Square[(from + to) >> 1] == 0;
   }else if(PIECE_IS_KNIGHT(piece))
   {
	    disp = c_HorseLegTab[to - from + 256];
        return disp != 0 && Square[from + disp] == 0;
   }else if(PIECE_IS_ROOK(piece))
   {
	   x = from & 0xf;
       y = from >> 4;
       if (x == (to & 0xf)) {
          if (capture == 0) {
              return (g_FileMaskTab[y - 3][BitFiles[x]].non_cap & g_BitFileMask[to]) != 0;
          } else {
              return (g_FileMaskTab[y - 3][BitFiles[x]].rook_cap & g_BitFileMask[to]) != 0;
          }
        } else if (y == (to >> 4)) {
          if (capture == 0) {
              return (g_RankMaskTab[x - 3][BitRanks[y]].non_cap & g_BitRankMask[to]) != 0;
          } else {
              return (g_RankMaskTab[x - 3][BitRanks[y]].rook_cap & g_BitRankMask[to]) != 0;
          }
        } else {
         return false;
        }
   }else if(PIECE_IS_CANNON(piece))
   {
	    x = from & 0xf;
        y = from >> 4;
        if (x == (to & 0xf)) {
            if (capture == 0) {
                return (g_FileMaskTab[y - 3][BitFiles[x]].non_cap & g_BitFileMask[to]) != 0;
            } else {
                return (g_FileMaskTab[y - 3][BitFiles[x]].cannon_cap & g_BitFileMask[to]) != 0;
            }
        } else if (y == (to >> 4)) {
            if (capture == 0) {
                return (g_RankMaskTab[x - 3][BitRanks[y]].non_cap & g_BitRankMask[to]) != 0;
            } else {
                return (g_RankMaskTab[x - 3][BitRanks[y]].cannon_cap & g_BitRankMask[to]) != 0;
            }
        } else {
            return false;
        }

   }else if(PIECE_IS_PAWN(piece))
   {
	   if(COLOUR_IS(piece,0))
	   {
	       return to == from - 16 || ((to & 0x80) == 0 && (to == from - 1 || to == from + 1));
       } else {
           return to == from + 16 || ((to & 0x80) != 0 && (to == from - 1 || to == from + 1));
       }
	  
   }

   return false;
}
bool quiet_is_pseudo(int move) {

   int me, opp;
   int from, to;
   int piece;
   int disp,x,y;

   // init

   me = Turn;
   opp = COLOUR_OPP(Turn);

   // from

   from = MOVE_FROM(move);

   piece = Square[from];
   if (!COLOUR_IS(piece,me)) return false;

   ASSERT(piece_is_ok(piece));

   // to

   to = MOVE_TO(move);


   if (Square[to] != Empty) return false; // capture

  if(PIECE_IS_KING(piece))
   {
	   return c_InCity[to] && ProtectTab[to - from + 256]== 1;
   }else if(PIECE_IS_ADVISOR(piece))
   {
	   return c_InCity[to] && ProtectTab[to - from + 256]== 2;
   }else if(PIECE_IS_BISHOP(piece))
   {
        return ((from ^ to) & 0x80) == 0 && ProtectTab[to - from + 256] == 3 && Square[(from + to) >> 1] == 0;
   }else if(PIECE_IS_KNIGHT(piece))
   {
	    disp = c_HorseLegTab[to - from + 256];
        return disp != 0 && Square[from + disp] == 0;
   }else if(PIECE_IS_ROOK(piece) || PIECE_IS_CANNON(piece))
   {
	   x = from & 0xf;
       y = from >> 4;
       if (x == (to & 0xf)) {
              return (g_FileMaskTab[y - 3][BitFiles[x]].non_cap & g_BitFileMask[to]) != 0;        
        } else if (y == (to >> 4)) {
              return (g_RankMaskTab[x - 3][BitRanks[y]].non_cap & g_BitRankMask[to]) != 0;     
        } else {
         return false;
        }
   }else if(PIECE_IS_PAWN(piece))
   {
	   if(COLOUR_IS(piece,0))
	   {
	       return to == from - 16 || ((to & 0x80) == 0 && (to == from - 1 || to == from + 1));
       } else {
           return to == from + 16 || ((to & 0x80) != 0 && (to == from - 1 || to == from + 1));
       }  
   }
   return false;
}


bool pseudo_is_legal(int move) {
   int me, opp;
   int from, to;
   int piece;
   bool legal;

   // init
   
   me = Turn;
   opp = COLOUR_OPP(me);

   from = MOVE_FROM(move);
   to = MOVE_TO(move);

   piece = Square[from];

   if (PIECE_IS_KING(piece)) {

      legal = !is_checked(to,opp);

      return legal;
   }
   
   if(is_cannon_pinned(to,me))//������
   {
	   return false;
   }
   if(is_knight_pinned(move,me))
   {
	   return false;
   }
   if (is_filter_pinned(move,me)) {
      return false;
   }
   return true;
}