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
#include ".\move.h"

int move_order(int move) {

  // ASSERT(move_is_ok(move));
  return move;
  // return ((move & 07777) << 2) | ((move >> 12) & 3);
}

bool move_is_tactical(int move) {

   ASSERT(move_is_ok(move));
   

  // return (move & (1 << 15)) != 0 || Square[MOVE_TO(move)] != Empty; // HACK
   return Square[MOVE_TO(move)] != Empty;
}

// move_capture()

int move_capture(int move) {

   ASSERT(move_is_ok(move));
   

   return Square[MOVE_TO(move)];
}

const long move_to_string(int move)
{
	int from,to;
    char ret_val[4];

	from=MOVE_FROM(move);
	to=MOVE_TO(move);

    ret_val[0] = (from & 0xf) + 'a' - 3;
    ret_val[1] = '9' + 3-(from>>4);
    ret_val[2] = (to & 0xf) + 'a' - 3;
    ret_val[3] = '9' + 3-(to>>4);
    return *(long *) ret_val;
  }
int move_from_string(const unsigned int move_str) 
{
	unsigned char *argptr = (unsigned char *) &move_str;
	unsigned int src = ((12-argptr[1]+'0')<<4) + argptr[0]-'a'+3;	// y0x0
	unsigned int dst = ((12-argptr[3]+'0')<<4) + argptr[2]-'a'+3;	// y1x1
	return ( src << 8 ) | dst;										// y0x0y1x1
}