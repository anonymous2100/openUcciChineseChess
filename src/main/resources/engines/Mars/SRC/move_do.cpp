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
#include ".\move_do.h"
#include "pregen.h"
#include "memory.h"
#include "move.h"
#include "piece.h"
#include "pst.h"

int MoveRecords[256];
int nCurrentStep=0;
// move_do()

void move_do(int move, undo_t * undo) {

   int me, opp;
   int from, to;
   int piece, capture,pos;
   int piece_key;

   
   ASSERT(undo!=NULL);

   //ASSERT(board_is_legal(board));

   // initialise undo

   undo->capture = false;
   undo->turn = Turn;
   undo->opening = Opening;
   undo->endgame = Endgame;
   undo->key = Key;
   undo->lock = Lock;

   // init

   me = Turn;
   opp =COLOUR_OPP(me);

   from = MOVE_FROM(move);
   to = MOVE_TO(move);

   piece = Square[from];
   if(piece==0)
	   printf("Error move piece %x\n ",move);
   pos=piece & 31;
   // update key stack
   Stack[Sp++] = Lock;

   // update turn

   Turn = opp;
   Key ^= g_ZobristKeyPlayer;
   Lock ^= g_ZobristLockPlayer;

   piece_key=PieceToKey32[pos];


   if ((capture=Square[to]) != Empty) {

      undo->capture = true;
      undo->capture_square = to;
      undo->capture_piece = capture;
      
	  //�Ե����ӽ������
	  Piece[capture & 31]=0;
	  Piece_size[PIECE_COLOUR(capture)]--;

	  Number[PieceTo32[capture & 31]]--;

	  Key ^= g_ZobristKeyTable[piece_key][to];
      Lock ^= g_ZobristLockTable[piece_key][to];

   }else
   {
       //û�г��ӣ�ֻ����λ�С�λ��
	   BitRanks[to>>4] ^= g_BitRankMask[to];
       BitFiles[to & 0xf] ^= g_BitFileMask[to];
   }

   Square[from] = Empty;

   Square[to] = piece;
   
   Piece[pos] = to;

   BitRanks[from>>4] ^= g_BitRankMask[from];
   BitFiles[from & 0xf] ^= g_BitFileMask[from];

   if (piece_key < 5) {
        Opening +=g_PST[piece_key][to] - g_PST[piece_key][from];
 	    Endgame +=g_PST[piece_key+5][to] - g_PST[piece_key+5][from];
    } else {
	    Opening -=g_PST[piece_key-5][254 - to] - g_PST[piece_key-5][254 - from];
	    Endgame -=g_PST[piece_key][254 - to]   - g_PST[piece_key][254 - from];
    }
 
   Key ^= g_ZobristKeyTable[piece_key][to] ^ g_ZobristKeyTable[piece_key][from];
   Lock ^= g_ZobristLockTable[piece_key][to] ^ g_ZobristLockTable[piece_key][from];
   
   // move the piece
}

// move_undo()

void move_undo(int move, const undo_t * undo) {

   int me;
   int from, to;
   int piece,pos;

   
   ASSERT(move_is_ok(move));
   ASSERT(undo!=NULL);

   // init

   me = undo->turn;

   from = MOVE_FROM(move);
   to = MOVE_TO(move);

   piece = Square[to];
   pos=piece & 31;


   ASSERT(COLOUR_IS(piece,me));
   
   Square[from]=piece;
   Piece[pos]=from;

   BitRanks[from>>4] ^= g_BitRankMask[from];
   BitFiles[from & 0xf]^= g_BitFileMask[from];
 
  
   if(undo->capture)//���Ӹ���
   {
	   Square[to]=undo->capture_piece;
	   Piece[undo->capture_piece & 31]=to;
       
	   Piece_size[PIECE_COLOUR(undo->capture_piece)]++;
	   Number[PieceTo32[undo->capture_piece & 31]]++;
        
   }else
   { 
	   Square[to]=Empty;

	   BitRanks[to>>4] ^= g_BitRankMask[to];
	   BitFiles[to&0xf]^= g_BitFileMask[to];
   }
   
   Turn = undo->turn;

   Opening = undo->opening;
   Endgame = undo->endgame;

   Key = undo->key;
   Lock = undo->lock;

   Sp--;
}

// move_do_null()

void move_do_null( undo_t * undo) {

   
   ASSERT(undo!=NULL);

   //ASSERT(board_is_legal(board));

   // initialise undo

   undo->turn = Turn;
   undo->key = Key;
   undo->lock = Lock;


   // update key stack
   Stack[Sp++] = Lock;
   
   Ply_nb = 0;
   // update turn

   Turn = COLOUR_OPP(Turn);
   Key ^= g_ZobristKeyPlayer;
   Lock ^= g_ZobristLockPlayer;
}

// move_undo_null()

void move_undo_null( const undo_t * undo) {

   
   ASSERT(undo!=NULL);

   // update board info

   Turn = undo->turn;
   Key = undo->key;
   Lock= undo->lock;

   // update key stack

   ASSERT(Sp>0);
   Sp--;

   // debug

   ASSERT(board_is_ok(board));
}
