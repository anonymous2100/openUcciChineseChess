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
#include ".\board.h"
#include "attack.h"
 
int piece_material[ColourNb]; // Thomas
int Square[SquareNb];         //����λ����
sq_t Piece[32];               //��������0~15Ϊ�죬16~31Ϊ�ڣ�ֵΪ0ʱ����ʾ�����Ѿ�����
int Number[16];               // ǰ14λΪ������ͳ��
uint16 BitRanks[16];         // λ�����飬ע���÷���"bitRanks[Squares >> 4]"
uint16 BitFiles[16];         // λ�����飬ע���÷���"bitRanks[Squares & 0xf]"
int Turn;                     //0Ϊ�죬1Ϊ��
int Opening;
int Endgame;
unsigned long Key;           // Zobrist��ֵ
uint64 Lock;                 // ZobristУ����
uint64 Stack[StackSize];
int Piece_size[ColourNb];
int Sp;
int Piece_nb;
int Ply_nb;

bool board_is_check() {

   return is_in_check(Turn);
}

bool board_is_mate() {

   attack_t attack[1];

   attack_set(attack);

   if (!ATTACK_IN_CHECK(attack)) return false; // not in check => not mate

   return true; // in check and no legal move => mate
}

void board_clear()
{
   int sq, sq_90;

   for (sq = 0; sq < 16; sq ++)
      BitRanks[sq] = 0;
   
   for (sq = 0; sq < 16; sq ++)
      BitFiles[sq] = 0;

   for (sq = 0; sq < SquareNb; sq++) {
      Square[sq] = Edge;
   }

   for (sq_90 = 0; sq_90 < 90; sq_90++) {
      sq = SQUARE_FROM_90(sq_90);
      Square[sq] = Empty;
   }
   
   for (sq=0; sq < 32; sq ++)
   {
	   Piece[sq]=0;
   }
   Piece_nb = 0;

   for (sq = 0;sq <14; sq ++)
	   Number[sq]=0;

   Piece_size[0]=Piece_size[1]=0;

   Opening=Endgame=0;

   Turn=0;

   Key=0;
   Lock=0;

}
/*
void board_copy(board_t * dst, const board_t * src)
{
	*dst = *src;
}
*/

void board_init_list() 
{

		
}
