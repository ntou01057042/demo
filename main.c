#include <INTRINS.H>
#include <REG52.H>
#include <string.h>

#define DataPort P0

#define MAX 8

typedef unsigned char byte;

sbit LATCH = P1 ^ 0;
sbit SRCLK = P1 ^ 1;
sbit SER = P1 ^ 2;

sbit LATCH_B = P2 ^ 2;
sbit SRCLK_B = P2 ^ 1;
sbit SER_B = P2 ^ 0;

sbit LATCH1 = P2 ^ 5;
sbit LATCH2 = P2 ^ 6;

code unsigned char segout[] = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80};
code unsigned char dofly_WeiMa[] = {0xfe, 0xfd, 0xfb, 0xf7, 0xef, 0xdf, 0xbf, 0x7f};
code unsigned char dofly_DuanMa[] = {
	0x3f, 0x06, 0x5b, 0x4f, 0x66, 0x6d, 0x7d, 0x07, 0x7f, 0x6f, 0x00};

unsigned char state[][8] = {{0, 0, 0, 0, 0, 0, 0, 0},
                            {0, 0, 0, 0, 0, 0, 0, 0},
                            {0, 0, 0, 0, 0, 0, 0, 0},
                            {0, 0, 0, 2, 1, 0, 0, 0},
                            {0, 0, 0, 1, 2, 0, 0, 0},
                            {0, 0, 0, 0, 0, 0, 0, 0},
                            {0, 0, 0, 0, 0, 0, 0, 0},
                            {0, 0, 0, 0, 0, 0, 0, 0}};
unsigned char display_data[8] = {0x3f, 0x78, 0x74, 0x7b, 0x06, 0x06, 0x5c, 0x00};
unsigned char gameover = 0;

void InitUART(void);
void init_timer0(void);
void display_state(const unsigned char tab1[], const unsigned char tab2[]);
void state_to_tab1(const unsigned char state[][8], unsigned char tab[]);
void state_to_tab2(const unsigned char state[][8], unsigned char tab[]);
void SendSeg(unsigned char dat);                        // !
void Send2Byte(unsigned char dat1, unsigned char dat2); // !
void SendByte(unsigned char dat);                       // !
void Out595(void);                                      // !
void DelayMs(unsigned char t);                          // !
void DelayUs2x(unsigned char t);                        // !
void display_score(void);

void main(void)
{
    unsigned char tab1[8]; // green
    unsigned char tab2[8]; // red
    unsigned char tab3[8] = {0};

    InitUART();
    init_timer0();

    while (1)
    {
        state_to_tab1(state, tab1);
        state_to_tab2(state, tab2);
        display_state(tab1, tab2);
    }
}

void InitUART(void)
{
    SCON = 0x50;  // SCON: 模式 1, 8-bit UART, 使能接收
    TMOD |= 0x20; // TMOD: timer 1, mode 2, 8-bit 重裝
    TH1 = 0xFD;   // TH1:  重裝值 9600 串列傳輸速率 晶振 11.0592MHz
    TR1 = 1;      // TR1:  timer 1 打開
    EA = 1;       // 打開總中斷
    ES = 1;       // 打開串口中斷
}

void init_timer0(void)
{
    TMOD |= 0x01; // set timer0 as mode 1 (16-bit timer)
    TR0 = 1;      // timer0 start running
    ET0 = 1;      // enable timer0 interrupt
}

void display_state(const unsigned char tab1[], const unsigned char tab2[])
{
    unsigned char i;

    for (i = 0; i < 8; ++i)
    {
        SendSeg(segout[i]);        // scan
        Send2Byte(~tab1[i], 0xff); // red
        Out595();
        Send2Byte(0xff, ~tab2[i]); // green
        Out595();
        DelayMs(1);
        Send2Byte(0xff, 0xff); // clear
        Out595();
    }
}

void state_to_tab1(const unsigned char state[][8], unsigned char tab[])
{
    unsigned char col, row, tmp;

    for (col = 0, tmp = 0; col < 8; col++)
    {
        for (row = 0; row < 8; row++)
        {
            tmp >>= 1;
            if (state[row][col] == 1)
            {
                tmp |= 0x80;
            }
        }
        tab[7 - col] = tmp;
    }
}

void state_to_tab2(const unsigned char state[][8], unsigned char tab[])
{
    unsigned char col, row, tmp;

    for (col = 0, tmp = 0; col < 8; col++)
    {
        for (row = 0; row < 8; row++)
        {
            tmp >>= 1;
            if (state[row][col] == 2)
            {
                tmp |= 0x80;
            }
        }
        tab[7 - col] = tmp;
    }
}

void SendSeg(unsigned char dat)
{
    unsigned char i;

    for (i = 0; i < 8; ++i)
    {
        SRCLK_B = 0;
        SER_B = dat & 0x80;
        dat <<= 1;
        SRCLK_B = 1;
    }
    LATCH_B = 1;
    _nop_();
    LATCH_B = 0;
}

void Send2Byte(unsigned char dat1, unsigned char dat2)
{
    SendByte(dat1);
    SendByte(dat2);
}

void SendByte(unsigned char dat)
{
    unsigned char i;

    for (i = 0; i < 8; ++i)
    {
        SRCLK = 0;
        SER = dat & 0x80;
        dat <<= 1;
        SRCLK = 1;
    }
}

void Out595(void)
{
    LATCH = 1;
    _nop_();
    LATCH = 0;
}

void DelayMs(unsigned char t)
{
    while (--t)
    {
        DelayUs2x(245);
        DelayUs2x(245);
    }
}

void DelayUs2x(unsigned char t)
{
    while (--t)
        ;
}

void display_score(void)
{
    static unsigned char i = 0;

    DataPort = 0;
    LATCH1 = 1;
    LATCH1 = 0;
    DataPort = dofly_WeiMa[i];
    LATCH2 = 1;
    LATCH2 = 0;
    DataPort = display_data[i];
    LATCH1 = 1;
    LATCH1 = 0;

    if (++i == 8)
    {
        i = 0;
    }
}

void UART_SER(void) interrupt 4 // 串列中斷服務程式
{
    unsigned char tmp; // 定義臨時
    static unsigned char cnt = 0;

    display_data[2] = 0x00;
    display_data[3] = 0x00;
    display_data[4] = 0x00;
    display_data[5] = 0x00;

    if (RI && gameover != 1) // 判斷是接收中斷產生
    {
        RI = 0;     // 標誌位元清零
        tmp = SBUF; // 讀入緩衝區的值
        if (tmp == 'G')
        {
            gameover = 1;
        }

        if (cnt == 64)
        {
            display_data[0] = dofly_DuanMa[tmp - '0'];
        }
        else if (cnt == 65)
        {
            display_data[1] = dofly_DuanMa[tmp - '0'];
        }
        else if (cnt == 66)
        {
            display_data[6] = dofly_DuanMa[tmp - '0'];
        }
        else if (cnt == 67)
        {
            display_data[7] = dofly_DuanMa[tmp - '0'];
        }
        else if (tmp == 0x30)
        {
            state[cnt / 8][cnt % 8] = 0;
        }
        else if (tmp == 0x31)
        {
            state[cnt / 8][cnt % 8] = 1;
        }
        else if (tmp == 0x32)
        {
            state[cnt / 8][cnt % 8] = 2;
        }

        cnt++;
        if (cnt == 68)
        {
            cnt = 0;
        }
    }
}

void timer0_isr(void) interrupt 1
{
    display_score();
    TH0 = (65536 - 2000) / 256;
    TL0 = (65536 - 2000) % 256;
}
