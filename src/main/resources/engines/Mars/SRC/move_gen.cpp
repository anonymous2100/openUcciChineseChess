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
#include ".\move_gen.h"

#include "move.h"
#include "pregen.h"
#include "attack.h"
#include "move_legal.h"
#include "move_evasion.h"

static void add_captures            (list_t * list);
static void add_quiet_moves         (list_t * list);

void gen_legal_moves(list_t * list) {

   attack_t attack[1];

   ASSERT(list!=NULL);
   

   attack_set(attack);

   if (ATTACK_IN_CHECK(attack)) {
      gen_evasions(list,attack);
   } else 
   {
      gen_moves(list);
      list_filter(list,&pseudo_is_legal,true);
   }

   
}

void test_gen_moves(list_t *list,int from,int to1)
{
   int x,y,x1,y1,disp,piece,to;
   uint8 * ptr;
   slide_move_t *slide_move_ptr;

   //ASSERT(alists!=NULL);
   //
   
   piece=Square[from];
   if (piece != 0) {
      x = from & 0xf;
      y = from >> 4;
      x1 = to1 & 0xf;
      y1 = to1 >> 4;
      if(y==y1)
	  {
         disp = y << 4;
         slide_move_ptr = g_RankMoveTab[x - 3] + BitRanks[y];
         ptr = slide_move_ptr->rook_cap;
		 
		 if(x>x1)
		 {
            to = ptr[0] + disp;
            if (to != from) {
          
               LIST_ADD(list,MOVE_MAKE(from,to));
           
            }
		   }else
		 {
            to = ptr[1] + disp;
            if (to != from) {
           
               LIST_ADD(list,MOVE_MAKE(from,to));
           
            }
			}
	  }
	  
	  if(x==x1)
	  {
		 
         disp = x;
         slide_move_ptr = g_FileMoveTab[y - 3] + BitFiles[x];
         ptr = slide_move_ptr->rook_cap;
         
		 if(y>y1)
		 {
             to = ptr[0] + disp;
             if (to != from) {
           
               LIST_ADD(list,MOVE_MAKE(from,to));
           
              }
		 }
		 else
		 {

              to = ptr[1] + disp;
              if (to != from) {
          
               LIST_ADD(list,MOVE_MAKE(from,to));
           
               }
		 }
	  }
	  
    }

}
void gen_moves(list_t * list) 
{
	ASSERT(list!=NULL);
    
	LIST_CLEAR(list);
	add_captures(list);
	add_quiet_moves(list);

}
void gen_captures(list_t * list) {
	
	LIST_CLEAR(list);
	add_captures(list);

}

void gen_quiet_moves(list_t * list) {

	LIST_CLEAR(list);
	add_quiet_moves(list);
}
//�����ŷ�
static void add_captures(list_t * list) 
{

   int me, opp;
   int opp_flag;
   int from, to;
   int piece_tag,x, y, disp,i;
   uint8 * ptr,*eye_leg_ptr;
   slide_move_t *slide_move_ptr;

   ASSERT(list!=NULL);
   

   me = Turn;
   piece_tag =me << 4;
   
   opp = COLOUR_OPP(me);
   opp_flag = COLOUR_FLAG(opp);

   // 1. ����˧(��)���ŷ�
   from = Piece[piece_tag];
   if (from != 0) {
       ptr = g_KingMoves[from];
       to = *ptr;
       while (to != 0) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
           ptr ++;
           to = *ptr;
       }
    }

  // 2. ������(ʿ)���ŷ�
  for (i = 1; i <= 2; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      ptr = g_AdvisorMoves[from];
      to = *ptr;
      while (to != 0) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
        ptr ++;
        to = *ptr;
      }
    }
  }

  // 3. ������(��)���ŷ�
  for (i = 3; i <= 4; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      ptr = g_BishopMoves[from];
      eye_leg_ptr = g_ElephantEyes[from];
      to = *ptr;
      while (to != 0) {
        if (Square[*eye_leg_ptr] == 0) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
        }
        ptr ++;
        to = *ptr;
        eye_leg_ptr ++;
      }
    }
  }

  // 4. ��������ŷ�
  for (i = 5; i <= 6; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      ptr = g_KnightMoves[from];
      eye_leg_ptr = g_HorseLegs[from];
      to = *ptr;
      while (to != 0) {
        if (Square[*eye_leg_ptr] == 0) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
        }
        ptr ++;
        to = *ptr;
        eye_leg_ptr ++;
      }
    }
  }

  // 5. ���ɳ����ŷ�
  for (i = 7; i <= 8; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      x = from & 0xf;
      y = from >> 4;
      disp = y << 4;
      slide_move_ptr = g_RankMoveTab[x - 3] + BitRanks[y];
      ptr = slide_move_ptr->rook_cap;
      to = ptr[0] + disp;
      if (to != from) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
      }
      to = ptr[1] + disp;
      if (to != from) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
      }
      disp = x;
      slide_move_ptr = g_FileMoveTab[y - 3] + BitFiles[x];
      ptr = slide_move_ptr->rook_cap;
      to = ptr[0] + disp;
      if (to != from) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
      }
      to = ptr[1] + disp;
      if (to != from) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
      }
    }
  }

  // 6. �����ڵ��ŷ�
  for (i = 9; i <= 10; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      x = from & 0xf;
      y = from >> 4;
      disp = y << 4;
      slide_move_ptr = g_RankMoveTab[x - 3] + BitRanks[y];
      ptr = slide_move_ptr->cannon_cap;
      to = ptr[0] + disp;
      if (to != from) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
      }
      to = ptr[1] + disp;
      if (to != from) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
      }
      disp = x;
      slide_move_ptr = g_FileMoveTab[y - 3] + BitFiles[x];
      ptr = slide_move_ptr->cannon_cap;
      to = ptr[0] + disp;
      if (to != from) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
      }
      to = ptr[1] + disp;
      if (to != from) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
      }
    }
  }

  // 7. ���ɱ�(��)���ŷ�
  for (i = 11; i <= 15; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      ptr = g_PawnMoves[Turn][from];
      to = *ptr;
      while (to != 0) {
           if (FLAG_IS(Square[to],opp_flag)) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
        ptr ++;
        to = *ptr;
      }
    }
  }

}
//�������ŷ�
static void add_quiet_moves(list_t * list)
{
   int from, to;
   int piece_tag,x, y, disp,i;
   uint8 * ptr,*eye_leg_ptr;
   slide_move_t *slide_move_ptr;

   ASSERT(list!=NULL);
   
  
   piece_tag = Turn<<4;

     // 1. ����˧(��)���ŷ�
  from = Piece[piece_tag];
  if (from != 0) {
    ptr = g_KingMoves[from];
    to = *ptr;
    while (to != 0) {
      // �ҵ�һ���ŷ��������ж��Ƿ�Ե�����
      if (Square[to] == 0) {
      LIST_ADD(list,MOVE_MAKE(from,to));
      }
      ptr ++;
      to = *ptr;
    }
  }

  // 2. ������(ʿ)���ŷ�
  for (i = 1; i <= 2; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      ptr = g_AdvisorMoves[from];
      to = *ptr;
      while (to != 0) {
        if (Square[to] == 0) {
          LIST_ADD(list,MOVE_MAKE(from,to));
        }
        ptr ++;
        to = *ptr;
      }
    }
  }

  // 3. ������(��)���ŷ�
  for (i = 3; i <= 4; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      ptr = g_BishopMoves[from];
      eye_leg_ptr = g_ElephantEyes[from];
      to = *ptr;
      while (to != 0) {
        if (Square[*eye_leg_ptr] == 0) {
          if (Square[to] == 0) {
           LIST_ADD(list,MOVE_MAKE(from,to));
          }
        }
        ptr ++;
        to = *ptr;
        eye_leg_ptr ++;
      }
    }
  }

  // 4. ��������ŷ�
  for (i = 5; i <= 6; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      ptr = g_KnightMoves[from];
      eye_leg_ptr = g_HorseLegs[from];
      to = *ptr;
      while (to != 0) {
        if (Square[*eye_leg_ptr] == 0) {
          if (Square[to] == 0) {
           LIST_ADD(list,MOVE_MAKE(from,to));
          }
        }
        ptr ++;
        to = *ptr;
        eye_leg_ptr ++;
      }
    }
  }

  // 5. ���ɳ����ڵ��ŷ���û�б�Ҫ�ж��Ƿ�Ե���������
  for (i = 7; i <= 10; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      x = from & 0xf;
      y = from >> 4;
      disp = y << 4;
      slide_move_ptr = g_RankMoveTab[x - 3] + BitRanks[y];
      ptr = slide_move_ptr->non_cap;
      to = ptr[0] + disp;
      while (to != from) {
       LIST_ADD(list,MOVE_MAKE(from,to));
        to --;
      }
      to = ptr[1] + disp;
      while (to != from) {
      LIST_ADD(list,MOVE_MAKE(from,to));
        to ++;
      }
      disp = x;
      slide_move_ptr = g_FileMoveTab[y - 3] + BitFiles[x];
      ptr = slide_move_ptr->non_cap;
      to = ptr[0] + disp;
      while (to != from) {
        LIST_ADD(list,MOVE_MAKE(from,to));
        to -= 16;
      }
      to = ptr[1] + disp;
      while (to != from) {
       LIST_ADD(list,MOVE_MAKE(from,to));
        to += 16;
      }
    }
  }

  // 6. ���ɱ�(��)���ŷ�
  for (i = 11; i <= 15; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0) {
      ptr = g_PawnMoves[Turn][from];
      to = *ptr;
      while (to != 0) {
        if (Square[to] == 0) {
          LIST_ADD(list,MOVE_MAKE(from,to));
        }
        ptr ++;
        to = *ptr;
      }
    }
  }

}