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
#include ".\move_check.h"
#include "move_do.h"
#include "attack.h"
#include "move_evasion.h"


static void find_pins(int list[]);
static void add_quiet_checks(list_t * list);

void gen_quiet_checks(list_t * list) 
{
	LIST_CLEAR(list);
	add_quiet_checks(list);
}
//���ӽ����ŷ�
static void add_quiet_checks(list_t * list)
{
	int me, opp;
	const sq_t * ptr;
	int from;
	int king;
	int inc;
	int pin[4];
	
	me = Turn;
    opp = COLOUR_OPP(me);

	find_pins(pin);

    king=Piece[opp<<4];

	for (ptr = pin; (from=*ptr) != SquareNone; ptr++) {
		if(from!=0)
		{
			inc=AttackTab[from-king + 256];
			 if((inc==16)||(inc==-16))
			     add_flee(list,from,true);
			 else
				 add_flee(list,from,false);
		}
	}

}
bool move_is_check(int move) {
	undo_t undo[1];
	int me;
    bool check=false;
	me=Turn;
	move_do(move,undo);
    check = is_in_check(me);
    move_undo(move,undo);
    return check;
}
//�齫
static void find_pins(int list[]) {

   int me, opp;
   int piece_tag;
   int pin;

   me = Turn;
   opp = COLOUR_OPP(me);
   
   piece_tag=me<<4;
   
   for(int i=1;i<15;++i)
   {
	   pin=Piece[piece_tag+i];
	   if(is_piece_pinned(pin,opp))
		   *list++ = pin;   
   }

  *list = SquareNone; 

}