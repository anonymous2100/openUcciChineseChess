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
#include <cstring>

#include "board.h"
#include "colour.h"
//#include "hash.h"
#include "material.h"
//#include "option.h"
#include "piece.h"
//#include "protocol.h"
#include "square.h"
#include "util.h"

static const bool UseTable = true;
static const uint32 TableSize = 256; // 4kB

static const int PawnPhase   = 0;
static const int KnightPhase = 2;
static const int CannonPhase = 2;
static const int RookPhase   = 4;
static const int TotalPhase = PawnPhase * 10 + CannonPhase * 4 + KnightPhase * 4 + RookPhase * 4;

static const int PawnOpening    = 90; 
static const int PawnEndgame    = 90; 
static const int CannonOpening  = 960;
static const int CannonEndgame  = 880;
static const int KnightOpening  = 880;
static const int KnightEndgame  = 960;
static const int RookOpening    = 2000;
static const int RookEndgame    = 2000;
static const int BishopOpening  = 400;
static const int BishopEndgame  = 400;
static const int AdvisorOpening = 400;
static const int AdvisorEndgame = 400;


typedef material_info_t entry_t;

struct material_t {
   entry_t * table;
   uint32 size;
   uint32 mask;
   uint32 used;
   sint64 read_nb;
   sint64 read_hit;
   sint64 write_nb;
   sint64 write_collision;
};

// variables

static material_t Material[1];

// prototypes

static void material_comp_info (material_info_t * info);

void material_init() {

   // UCI options

//   MaterialWeight = (option_get_int("Material") * 256 + 50) / 100;
//   OpeningExchangePenalty = option_get_int("Toga Exchange Bonus");
//   EndgameExchangePenalty = OpeningExchangePenalty; 

   // material table

   Material->size = 0;
   Material->mask = 0;
   Material->table = NULL;
}

// material_alloc()

void material_alloc() {

   ASSERT(sizeof(entry_t)==16);

   if (UseTable) {

      Material->size = TableSize;
      Material->mask = TableSize - 1;
      Material->table = (entry_t *) my_malloc(Material->size*sizeof(entry_t));

      material_clear();
   }
}

// material_clear()

void material_clear() {

   if (Material->table != NULL) {
      memset(Material->table,0,Material->size*sizeof(entry_t));
   }

   Material->used = 0;
   Material->read_nb = 0;
   Material->read_hit = 0;
   Material->write_nb = 0;
   Material->write_collision = 0;
}
void material_get_info(material_info_t * info) 
{
   uint64 key;
   entry_t * entry;

   ASSERT(info!=NULL);
   

   // probe

   if (UseTable) {

      Material->read_nb++;

      key = Lock;
      entry = &Material->table[KEY_INDEX(key)&Material->mask];

      if (entry->lock == KEY_LOCK(key)) {

         // found

         Material->read_hit++;

         *info = *entry;

         return;
      }
   }

   // calculation

   material_comp_info(info);

   // store

   if (UseTable) {

      Material->write_nb++;

      if (entry->lock == 0) { // HACK: assume free entry
         Material->used++;
      } else {
         Material->write_collision++;
      }

      *entry = *info;
      entry->lock = KEY_LOCK(key);
   }

}

static void material_comp_info(material_info_t * info) 
{
	int wp, wc, wn, wr, wb, wa;
    int bp, bc, bn, br, bb, ba;

	int phase;
    int opening, endgame;
	
	wp=Number[RedPawn14];
	wc=Number[RedCannon14];
	wn=Number[RedKnight14];
	wr=Number[RedRook14];
	wb=Number[RedBishop14];
	wa=Number[RedAdvisor14];
	
	bp=Number[BlackPawn14];
	bc=Number[BlackCannon14];
	bn=Number[BlackKnight14];
	br=Number[BlackRook14];
	bb=Number[BlackBishop14];
	ba=Number[BlackAdvisor14];

	//phase
	phase = TotalPhase;

    phase -= wp * PawnPhase;
    phase -= wn * KnightPhase;
    phase -= wc * CannonPhase;
    phase -= wr * RookPhase;

    phase -= bp * PawnPhase;
    phase -= bn * KnightPhase;
    phase -= bc * CannonPhase;
    phase -= br * RookPhase;

   if (phase < 0) phase = 0;

    ASSERT(phase>=0&&phase<=TotalPhase);
    phase = (phase * 256 + (TotalPhase / 2)) / TotalPhase;


	opening = 0;
    endgame = 0;

	opening +=wp*PawnOpening;
    opening +=wc*CannonOpening;
    opening +=wn*KnightOpening;
    opening +=wr*RookOpening;
    opening +=wb*BishopOpening;
    opening +=wa*AdvisorOpening;

	opening -=bp*PawnOpening;
    opening -=bc*CannonOpening;
    opening -=bn*KnightOpening;
    opening -=br*RookOpening;
    opening -=bb*BishopOpening;
    opening -=ba*AdvisorOpening;

    endgame +=wp*PawnEndgame;    
    endgame +=wc*CannonEndgame;   
    endgame +=wn*KnightEndgame;   
    endgame +=wr*RookEndgame;   
    endgame +=wb*BishopEndgame; 
    endgame +=wa*AdvisorEndgame;

	endgame -=bp*PawnEndgame;    
    endgame -=bc*CannonEndgame;   
    endgame -=bn*KnightEndgame;   
    endgame -=br*RookEndgame;   
    endgame -=bb*BishopEndgame; 
    endgame -=ba*AdvisorEndgame;
    
	info->phase=phase;
	info->opening=opening;
	info->endgame=endgame;

}