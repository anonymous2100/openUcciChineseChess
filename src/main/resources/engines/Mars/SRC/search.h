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
#pragma once
// includes

#include <csetjmp>

#include "board.h"
#include "list.h"
#include "move.h"
#include "util.h"

// constants

const int DepthMax = 64;
const int HeightMax = 256;

const int SearchNormal = 0;
const int SearchShort  = 1;

const int SearchUnknown = 0;
const int SearchUpper   = 1;
const int SearchLower   = 2;
const int SearchExact   = 3;

// types

struct search_input_t {
   ;
   list_t list[1];
   bool infinite;
   bool depth_is_limited;
   int depth_limit;
   bool time_is_limited;
   double time_limit_1;
   double time_limit_2;
};

struct search_info_t {
   jmp_buf buf;
   bool can_stop;
   bool stop;
   int check_nb;
   int check_inc;
   double last_time;
};

struct search_root_t {
   list_t list[1];
   int depth;
   int move;
   int move_pos;
   int move_nb;
   int last_value;
   bool bad_1;
   bool bad_2;
   bool change;
   bool easy;
   bool flag;
};

struct search_best_t {
   int move;
   int value;
   int flags;
   int depth;
   mv_t pv[HeightMax];
};

struct search_current_t {
   ;
   my_timer_t timer[1];
   int max_depth;
   sint64 node_nb;
   sint64 node_qs_nb;
   double time;
   double speed;
   double cpu;
};
struct search_param_t {
   int move;
   int best_move;
   int threat_move;
   bool reduced;
};

// variables
//extern search_param_t SearchStack[HeightMax];
extern int ponder;
extern int stop_thinking;
extern int batch;
extern search_input_t SearchInput[1];
extern search_info_t SearchInfo[1];
extern search_best_t SearchBest[1];
extern search_root_t SearchRoot[1];
extern search_current_t SearchCurrent[1];
extern int node_qs;
// functions

extern bool depth_is_ok           (int depth);
extern bool height_is_ok          (int height);

extern void search_clear          ();
extern void search                (int depth,long proper_timer, long limit_timer);

extern void search_update_best    ();
extern void search_update_root    ();
extern void search_update_current ();

extern void search_check          ();
extern int interrupt(void);



// end of search.h

