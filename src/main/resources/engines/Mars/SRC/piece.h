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
#include "colour.h"

// ���������ڡ�����ʿ����
const int KingFlag    =1<<7;
const int AdvisorFlag =1<<8;
const int BishopFlag  =1<<9;
const int KnightFlag  =1<<10;
const int RookFlag    =1<<11;
const int CannonFlag  =1<<12;
const int PawnFlag    =1<<13;

const int SliderFlag  =RookFlag|CannonFlag;
const int RookPawnFlag=RookFlag|PawnFlag;

const int King90   =KingFlag;
const int Advisor90=AdvisorFlag;
const int Bishop90 =BishopFlag;
const int Knight90 =KnightFlag;
const int Rook90   =RookFlag;
const int Cannon90 =CannonFlag;
const int Pawn90   =PawnFlag;

//�췽���ӱ�ʾ ˧�������ڱ�
const int RedKing    =King90   | RedFlag;
const int RedAdvisor =Advisor90| RedFlag;
const int RedBishop  =Bishop90 | RedFlag;
const int RedKnight  =Knight90 | RedFlag;
const int RedRook    =Rook90   | RedFlag;
const int RedCannon  =Cannon90 | RedFlag;
const int RedPawn    =Pawn90   | RedFlag;

//�ڷ����ӱ�ʾ ��ʿ��������
const int BlackKing    =King90   | BlackFlag;
const int BlackAdvisor =Advisor90| BlackFlag;
const int BlackBishop  =Bishop90 | BlackFlag;
const int BlackKnight  =Knight90 | BlackFlag;
const int BlackRook    =Rook90   | BlackFlag;
const int BlackCannon  =Cannon90 | BlackFlag;
const int BlackPawn    =Pawn90   | BlackFlag;

const int PieceNb        = 256;

const int RedKing14     =0;
const int RedAdvisor14  =1;
const int RedBishop14   =2;
const int RedKnight14   =3;
const int RedRook14     =4;
const int RedCannon14   =5;
const int RedPawn14     =6;

const int BlackKing14   =7;
const int BlackAdvisor14=8;
const int BlackBishop14 =9;
const int BlackKnight14 =10;
const int BlackRook14   =11;
const int BlackCannon14 =12;
const int BlackPawn14   =13;

#define PIECE_COLOUR(piece)      (((127 & piece)>>5)-1)
#define PIECE_TYPE(piece)        ((piece)&~127)
/*
#define PIECE_IS_KING(piece)     (((piece)&KingFlag)!=0)
#define PIECE_IS_ADVISOR(piece)  (((piece)&AdvisorFlag)!=0) 
#define PIECE_IS_BISHOP(piece)   (((piece)&BishopFlag)!=0)
#define PIECE_IS_KNIGHT(piece)   (((piece)&KnightFlag)!=0)
#define PIECE_IS_ROOK(piece)     (((piece)&RookFlag)!=0)
#define PIECE_IS_CANNON(piece)   (((piece)&CannonFlag)!=0)
#define PIECE_IS_PAWN(piece)     (((piece)&PawnFlag)!=0)
#define PIECE_IS_SLIDER(piece)   (((piece)&SliderFlag)!=0)  //�Ƿ�Ϊ����
#define PIECE_IS_ROOKPAWN(piece) (((piece)&RookPawnFlag)!=0)
*/

#define PIECE_IS_KING(piece)     (((piece)&KingFlag))
#define PIECE_IS_ADVISOR(piece)  (((piece)&AdvisorFlag)) 
#define PIECE_IS_BISHOP(piece)   (((piece)&BishopFlag))
#define PIECE_IS_KNIGHT(piece)   (((piece)&KnightFlag))
#define PIECE_IS_ROOK(piece)     (((piece)&RookFlag))
#define PIECE_IS_CANNON(piece)   (((piece)&CannonFlag))
#define PIECE_IS_PAWN(piece)     (((piece)&PawnFlag))
#define PIECE_IS_SLIDER(piece)   (((piece)&SliderFlag))  //�Ƿ�Ϊ����
#define PIECE_IS_ROOKPAWN(piece) (((piece)&RookPawnFlag))

#define PAWN_MAKE(colour)        (PawnMake[colour])

typedef int inc_t;

extern const int PawnMake[ColourNb];
extern const int PieceFrom32[32];
extern const int PieceTo32[32];
extern const int PieceToKey32[32];
extern const int MvvValues[32];