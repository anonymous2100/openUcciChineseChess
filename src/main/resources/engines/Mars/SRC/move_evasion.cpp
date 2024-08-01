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



#include ".\move_evasion.h"
#include "attack.h"
#include "list.h"
#include "move.h"
#include "move_check.h"
#include "board.h"
#include "move_do.h"

int nCheck;
//��

//ɱ
static void gen_attack(list_t * list,int to,int colour);
//�ƽ�
static void gen_move_king(list_t * list,const attack_t * attack);
//�� ��
static void gen_block(list_t * list,const attack_t * attack);

static void add_block(list_t * list,int square,int to,int colour);
//���������ȫ�㷨
void gen_evasions(list_t * list,const attack_t * attack) 
{
    int dn,move,size;
	list_t temp[1];
    LIST_CLEAR(list);
	LIST_CLEAR(temp);
    dn=attack->dn;
    if(dn==1)
	{   
		nCheck++;
		gen_move_king(list,attack); //�ƽ�
		gen_attack(list,attack->ds[0],Turn);              //ɱ
	    gen_block(list, attack);                          //�� ��
	}else if(dn>1)
	{
		gen_move_king(temp,attack); //�ƽ�
		gen_attack(temp,attack->ds[0],Turn);              //ɱ
	    gen_block(temp, attack);  //�� ��
		size=LIST_SIZE(temp);
        for(int i=0;i<size;++i)
		{
			move=LIST_MOVE(temp,i);
			if(!move_is_check(move))
			{
				LIST_ADD(list,move);
			}
		}


	}
}
static void gen_attack(list_t * list,int to,int colour)
{
	int i, piece_tag, from, disp, x, y,pawn,opp;
    slide_mask_t *rank_mask_ptr, *file_mask_ptr;

    piece_tag =colour<<4;

	//pawn=PawnFlag|(1<<(5+colour));
	pawn=PawnFlag | (32 <<colour);
	opp = COLOUR_OPP(colour);

  if (colour == 0 ? (to & 0x80) != 0 : (to & 0x80) == 0) {
    if (c_InCity[to]) {

      // 1. ��˧(��)ɱ
		
      from = Piece[piece_tag];
      if (from != 0 && !is_checked(to,opp)) {
        if (ProtectTab[from - to + 256] == 1) {
          LIST_ADD(list,MOVE_MAKE(from,to));
        }
      }

      // 2. ����(ʿ)ɱ
      for (i = 1; i <= 2; i ++) {
        from = Piece[piece_tag + i];
        if (from != 0 && !is_piece_pinned(from,colour)) {
          if (ProtectTab[from - to + 256] == 2) {
            LIST_ADD(list,MOVE_MAKE(from,to));
          }
        }
      }
    }

    // 3. ����(��)ɱ
    for (i = 3; i <= 4; i ++) {
      from = Piece[piece_tag + i];
      if (from != 0 && !is_piece_pinned(from,colour)) {
        if (ProtectTab[from - to + 256] == 3 && Square[(to + from) >> 1] == 0) {
          LIST_ADD(list,MOVE_MAKE(from,to));
        }
      }
    }
  } else {

    // 4. �ñ�(��)����ɱ
    for (from = to - 1; from <= to + 1; from += 2) {
		if((Square[from]&pawn)==pawn && !is_piece_pinned(from,colour))
		{
			LIST_ADD(list,MOVE_MAKE(from,to));
		}
    }
  }

  // 5. �ñ�(��)����ɱ
  from = to + (colour == 0 ? 16 : -16);
  if((Square[from]&pawn)==pawn && !is_piece_pinned(from,colour))
  {
	  LIST_ADD(list,MOVE_MAKE(from,to));
  }

  // 6. ����ɱ
  for (i = 5; i <= 6; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0 && !is_piece_pinned(from,colour)) {
      disp = c_HorseLegTab[to - from + 256];
      if (disp != 0 && Square[from + disp]==0) {
        LIST_ADD(list,MOVE_MAKE(from,to));
      }
    }
  }

  x = to & 0xf;
  y = to >> 4;
  rank_mask_ptr = g_RankMaskTab[x - 3] + BitRanks[y];
  file_mask_ptr = g_FileMaskTab[y - 3] + BitFiles[x];

  // 7. �ó�ɱ
  for (i = 7; i <= 8; i ++) {
    from = Piece[piece_tag + i];

    if (from != 0 && from != to && !is_piece_pinned(from,colour)) {
      if (x == (from & 0xf)) {
        if ((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0) {
          LIST_ADD(list,MOVE_MAKE(from,to));
        }
      } else if (y == (from >> 4)) {
        if ((rank_mask_ptr->rook_cap & g_BitRankMask[from]) != 0) {
          LIST_ADD(list,MOVE_MAKE(from,to));
        }
      }
    }
  }

  // 8. ����ɱ
  
  for (i = 9; i <= 10; i ++) {
    from = Piece[piece_tag + i];
    if (from!=0 && from != to && !is_piece_pinned(from,colour)) {
      if (x == (from & 0xf)) {
        if ((file_mask_ptr->cannon_cap & g_BitFileMask[from]) != 0) {
          LIST_ADD(list,MOVE_MAKE(from,to));
        }
      } else if (y == (from >> 4)) {
        if ((rank_mask_ptr->cannon_cap & g_BitRankMask[from]) != 0) {
          LIST_ADD(list,MOVE_MAKE(from,to));
        }
      }
    }
  }
  
}
static void gen_move_king(list_t * list,const attack_t* attack)
{
   int me, opp,dn;
   int opp_flag;
   int from, to;
   int piece_tag;
   uint8 * ptr;
   
   dn=attack->dn;
   me = Turn;
   piece_tag =me << 4;
   
   opp = COLOUR_OPP(me);
   opp_flag = COLOUR_FLAG(opp);
 
   // ˧(��)���ƶ��ŷ�
   from = Piece[piece_tag];
   if (from != 0) {
       ptr = g_KingMoves[from];
       to = *ptr;
       while (to != 0) {
           if (Square[to]==Empty ) {	
               if(!is_checked(to,opp))
			   {
				   
				   for(int i=0;i<dn;++i)
				   {
					   if(PIECE_IS_SLIDER(Square[attack->ds[i]]))   //���˻��ӽ�����ͬ�л�ͬ���ƶ�
					   {
					      if((attack->ds[i]&0xf) ==(to&0xf)|| (attack->ds[i]>>4) == (to>>4))
                                goto I;  
					   }
				   }

				   LIST_ADD(list,MOVE_MAKE(from,to));

I:                 ;
			   }
		   
           }else if(FLAG_IS(Square[to],opp_flag))
		   {
			    if(!is_checked(to,opp))
			   {
				   
				   for(int i=0;i<dn;++i)
				   {
					   if(PIECE_IS_SLIDER(Square[attack->ds[i]]))   //���˻��ӽ�����ͬ�л�ͬ���ƶ�
					   {
					       //if((attack->ds[i]&0xf) ==(to&0xf)|| (attack->ds[i]>>4) == (to>>4))
						   if(AttackTab[to-from + 256]==AttackTab[attack->ds[i]-from + 256]) 
						   LIST_ADD(list,MOVE_MAKE(from,to));  
					   }
				   }

			   }
		   }
           ptr ++;
           to = *ptr;
       }
    }
}
//�� ��
static void gen_block(list_t * list,  const attack_t * attack)
{
	int from,to,paojiazi,sq,temp,leg;
	int piece,opp,opp_flag;
	int inc;
	int me;

	me=Turn;
	opp = COLOUR_OPP(me);
    opp_flag = COLOUR_FLAG(opp);

	if(attack->dn>0)
	{
		from=attack->ds[0];
		piece=Square[from];
		
        to=Piece[me<<4];

		if(PIECE_IS_CANNON(piece))               //�з��ڽ�
		{
			inc=AttackTab[to-from + 256];
            sq = from;
		    do
			{
			    sq += inc; 	  
			}while (Square[sq]==Empty);    //ͳ���ڼ��� ��ע��Ҳ������λ��λ����ͳ�ƣ�
			paojiazi=sq;  
			
			sq = from;
			temp=to-inc;
           // printf("sq=%d,temp=%d\n",sq,temp);
            do
			{
			     sq += inc; 
				 if(sq!=paojiazi)
				 {
					 //printf("sq=%d,paojiazi=%d\n",sq,paojiazi);
			         add_block(list,paojiazi,sq,me);  //��
				 }else
				 {
					 if(PIECE_IS_CANNON(Square[paojiazi]) && FLAG_IS(Square[paojiazi],opp_flag)) //�ڼ���Ϊ�з��ڣ�����˫�ڽ��������ڵ�����·�ĵ�
						 break;
				 }

			 }while (sq!=temp);
            
		    /*sq = from;			
			temp=to-inc;

            if(PIECE_IS_ROOK(Square[paojiazi]) && FLAG_IS(Square[paojiazi],opp_flag))
			{
				sq=paojiazi;  //�ڳ����������ܼ���-�ڵĽ⽫
			}else if(PIECE_IS_CANNON(Square[paojiazi]) && FLAG_IS(Square[paojiazi],opp_flag))
			{
                temp=paojiazi-inc;
			}
            do
			{
			     sq += inc; 
			     add_block(list,paojiazi,sq,me);  //��

			 }while (sq!=temp);
			 */
            //�ڼ���Ϊ��������
			if(FLAG_IS(Square[paojiazi],(32<<me)))  //��
			{
			    if((inc==16)||(inc==-16))
			       add_flee(list,paojiazi,true); //�� �� �� ��
			    else
			       add_flee(list,paojiazi,false);//���� ��
			}


		}else if(PIECE_IS_ROOK(piece)) //�з�����
		{
			inc=AttackTab[to-from + 256];
			sq = from;
			sq += inc; 	  
			while (Square[sq]==Empty)
			{
				add_block(list,0,sq,me);    //��
				sq += inc;
			}

		}else if(PIECE_IS_KNIGHT(piece))          //�з���
		{
			 leg = c_HorseLegTab[to - from + 256];
			 add_block(list,0,from+leg,me); //������
		}
	}
}
/*
static void gen_multi_evasion(list_t * list, const attack_t * attack)
{
	int from1,from2,to;
	from1=attack->dn[0];
	from2=attack->dn[1];
	if((PIECE_IS_PAWN(Square[from1]) && (PIECE_IS_CANNON(Square[from2])))
	{
	}
}*/
/*
//�� ��
static void gen_multi_block(list_t * list,  const attack_t * attack)
{
	int from,to,paojiazi,sq,temp,leg;
	int piece,opp,opp_flag;
	int inc;
	int me;

	me=Turn;
	opp = COLOUR_OPP(me);
    opp_flag = COLOUR_FLAG(opp);

	if(attack->dn>1)
	{
		from=attack->ds[0];
		piece=Square[from];
		
        to=Piece[me<<4];

		if(PIECE_IS_CANNON(piece))               //�з��ڽ�
		{
			inc=AttackTab[to-from + 256];
            sq = from;
		    do
			{
			    sq += inc; 	  
			}while (Square[sq]==Empty);    //ͳ���ڼ��� ��ע��Ҳ������λ��λ����ͳ�ƣ�
			paojiazi=sq;  
			
			sq = from;
			temp=to-inc;
            do
			{
			     sq += inc; 
				 if(sq!=paojiazi)
				 {
			         add_block(list,paojiazi,sq,me);  //��
				 }else
				 {
					 if(PIECE_IS_CANNON(Square[paojiazi]) && FLAG_IS(Square[paojiazi],opp_flag)) //�ڼ���Ϊ�з��ڣ�����˫�ڽ��������ڵ�����·�ĵ�
						 break;
				 }

			 }while (sq!=temp);
            
            //�ڼ���Ϊ��������
			if(FLAG_IS(Square[paojiazi],(32<<me)))  //��
			{
			    if((inc==16)||(inc==-16))
			       add_flee(list,paojiazi,true); //�� �� �� ��
			    else
			       add_flee(list,paojiazi,false);//���� ��
			}


		}else if(PIECE_IS_ROOK(piece)) //�з�����
		{
			inc=AttackTab[to-from + 256];
			sq = from;
			sq += inc; 	  
			while (Square[sq]==Empty)
			{
				add_block(list,0,sq,me);    //��
				sq += inc;
			}

		}else if(PIECE_IS_KNIGHT(piece))          //�з���
		{
			 leg = c_HorseLegTab[to - from + 256];
			 add_block(list,0,from+leg,me); //������
		}
	}
}
*/
static void add_block(list_t * list, int square, int to,int colour)
{	
	int i, piece_tag, from, disp, x, y;
    
    slide_mask_t *rank_mask_ptr, *file_mask_ptr;
    int pawn;

    piece_tag =colour<<4;

	//pawn=PawnFlag|(1<<(5+colour));
    pawn=PawnFlag | (32 <<colour);

  if (colour == 0 ? (to & 0x80) != 0 : (to & 0x80) == 0) {
    if (c_InCity[to]) {

      // 1. ��˧(��)�������Ϸ�

      // 2. ����(ʿ)��
      for (i = 1; i <= 2; i ++) {
        from = Piece[piece_tag + i];
        if (from != 0 && !is_piece_pinned(from,colour)) {
          if (ProtectTab[from - to + 256] == 2 ) {
            LIST_ADD(list,MOVE_MAKE(from,to));
          }
        }
      }
	}

    // 3. ����(��)��
    for (i = 3; i <= 4; i ++) {
      from = Piece[piece_tag + i];
      if (from != 0 && !is_piece_pinned(from,colour)) {
        if (ProtectTab[from - to + 256] == 3 && Square[(to + from) >> 1] == 0 ) {
          LIST_ADD(list,MOVE_MAKE(from,to));
        }
      }
    }
  } else {
    
    // 4. �ñ�(��)����
    for (from = to - 1; from <= to + 1; from += 2) {

		if(from!=square && (Square[from]&pawn)==pawn && !is_piece_pinned(from,colour))
		{
			LIST_ADD(list,MOVE_MAKE(from,to));
		}

    }
  }

  // 5. �ñ�(��)����
  from = to + (colour == 0 ? 16 : -16);
   if(from!=square && (Square[from] & pawn)==pawn && !is_piece_pinned(from,colour))
  {
	  LIST_ADD(list,MOVE_MAKE(from,to));
  }
  // 6. ����
  for (i = 5; i <= 6; i ++) {
    from = Piece[piece_tag + i];
    if (from != 0 && !is_piece_pinned(from,colour)) {
      disp = c_HorseLegTab[to - from + 256];
      if (disp != 0 && Square[from + disp]==0 ) {
        LIST_ADD(list,MOVE_MAKE(from,to));
      }
    }
  }
  
  x = to & 0xf;
  y = to >> 4;
  rank_mask_ptr = g_RankMaskTab[x - 3] + BitRanks[y];
  file_mask_ptr = g_FileMaskTab[y - 3] + BitFiles[x];

  // 7. �ó����ڵ�
  for (i = 7; i <= 10; i ++) {
    from = Piece[piece_tag + i];

    if (square !=from && from != 0 && from != to) {
      if (x == (from & 0xf) && !is_piece_pinned(from,colour)) {
        if ((file_mask_ptr->rook_cap & g_BitFileMask[from]) != 0) {
          LIST_ADD(list,MOVE_MAKE(from,to));
        }
      } else if (y == (from >> 4) && !is_piece_pinned(from,colour)) {
        if ((rank_mask_ptr->rook_cap & g_BitRankMask[from]) != 0) {
          LIST_ADD(list,MOVE_MAKE(from,to));
        }
      }
    }
  }
  
}

void add_flee(list_t * list,int from,bool up)
{
    int me, opp;
    int opp_flag;
    int to;
    int x, y, disp;
    uint8 * ptr,*eye_leg_ptr;
    slide_move_t *slide_move_ptr;
	int piece;

	piece=Square[from];

    me = Turn;
  
    opp = COLOUR_OPP(me);
    opp_flag = COLOUR_FLAG(opp);

	if(PIECE_IS_ADVISOR(piece)) //ʿ��
	{
		ptr = g_AdvisorMoves[from];
        to = *ptr;
        while (to != 0) {
           if (FLAG_IS(Square[to],opp_flag)||Square[to]==Empty) {
               LIST_ADD(list,MOVE_MAKE(from,to));
           }
           ptr ++;
           to = *ptr;
        }
	
	}else if (PIECE_IS_BISHOP(piece))//����
	{
		ptr = g_BishopMoves[from];
        eye_leg_ptr = g_ElephantEyes[from];
        to = *ptr;
        while (to != 0) {
           if (Square[*eye_leg_ptr] == 0) {
              if (FLAG_IS(Square[to],opp_flag)||Square[to]==Empty) {
                  LIST_ADD(list,MOVE_MAKE(from,to));
              }
           }
           ptr ++;
           to = *ptr;
           eye_leg_ptr ++;
         }
	
	}else if (PIECE_IS_KNIGHT(piece))//����
	{
		ptr = g_KnightMoves[from];
        eye_leg_ptr = g_HorseLegs[from];
        to = *ptr;
        while (to != 0) {
           if (Square[*eye_leg_ptr] == 0) {
              if (FLAG_IS(Square[to],opp_flag)||Square[to]==Empty) {
                  LIST_ADD(list,MOVE_MAKE(from,to));
              }
           }
           ptr ++;
           to = *ptr;
           eye_leg_ptr ++;
         }
	
	}else if (PIECE_IS_ROOK(piece))//����
	{	
		x = from & 0xf;    
	    y = from >> 4;		
		if(up)
		{      
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
		}else
		{
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
	
	}else if (PIECE_IS_CANNON(piece)) //����
	{
		x = from & 0xf;
        y = from >> 4;
	    if(up)
		{
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
		}else
		{
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
	
	}else if (PIECE_IS_PAWN(piece)) //����
	{
		int inc;
		ptr = g_PawnMoves[Turn][from];
        to = *ptr;
        while (to != 0) {
			
			inc=AttackTab[to-from + 256];
			if(up)
			{  //�����߷�
			   if(inc==-1 || inc==1)
               if (FLAG_IS(Square[to],opp_flag)||Square[to]==Empty) {
                   LIST_ADD(list,MOVE_MAKE(from,to));
               }
			}else
			{
			   //�����߷�
			   if(inc==-16 || inc==16)
			   if (FLAG_IS(Square[to],opp_flag)||Square[to]==Empty) {
                   LIST_ADD(list,MOVE_MAKE(from,to));
               }

			}
           ptr ++;
           to = *ptr;
         }
	
	}
}