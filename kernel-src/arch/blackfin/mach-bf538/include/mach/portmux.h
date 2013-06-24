#ifndef _MACH_PORTMUX_H_
#define _MACH_PORTMUX_H_

#define MAX_RESOURCES	MAX_BLACKFIN_GPIOS

#define P_TMR2		(P_DONTCARE)
#define P_TMR1		(P_DONTCARE)
#define P_TMR0		(P_DONTCARE)
#define P_TMRCLK	(P_DONTCARE)
#define P_PPI0_CLK	(P_DONTCARE)
#define P_PPI0_FS1	(P_DONTCARE)
#define P_PPI0_FS2	(P_DONTCARE)

#define P_TWI0_SCL	(P_DONTCARE)
#define P_TWI0_SDA	(P_DONTCARE)
#define P_TWI1_SCL	(P_DONTCARE)
#define P_TWI1_SDA	(P_DONTCARE)

#define P_SPORT1_TSCLK	(P_DONTCARE)
#define P_SPORT1_RSCLK	(P_DONTCARE)
#define P_SPORT0_TSCLK	(P_DONTCARE)
#define P_SPORT0_RSCLK	(P_DONTCARE)
#define P_SPORT1_DRSEC	(P_DONTCARE)
#define P_SPORT1_RFS	(P_DONTCARE)
#define P_SPORT1_DTPRI	(P_DONTCARE)
#define P_SPORT1_DTSEC	(P_DONTCARE)
#define P_SPORT1_TFS	(P_DONTCARE)
#define P_SPORT1_DRPRI	(P_DONTCARE)
#define P_SPORT0_DRSEC	(P_DONTCARE)
#define P_SPORT0_RFS	(P_DONTCARE)
#define P_SPORT0_DTPRI	(P_DONTCARE)
#define P_SPORT0_DTSEC	(P_DONTCARE)
#define P_SPORT0_TFS	(P_DONTCARE)
#define P_SPORT0_DRPRI	(P_DONTCARE)

#define P_UART0_RX	(P_DONTCARE)
#define P_UART0_TX	(P_DONTCARE)

#define P_SPI0_MOSI	(P_DONTCARE)
#define P_SPI0_MISO	(P_DONTCARE)
#define P_SPI0_SCK	(P_DONTCARE)

#define P_PPI0_D0	(P_DONTCARE)
#define P_PPI0_D1	(P_DONTCARE)
#define P_PPI0_D2	(P_DONTCARE)
#define P_PPI0_D3	(P_DONTCARE)

#define P_CAN0_TX	(P_DEFINED | P_IDENT(GPIO_PC0))
#define P_CAN0_RX	(P_DEFINED | P_IDENT(GPIO_PC1))

#define P_SPI1_MOSI	(P_DEFINED | P_IDENT(GPIO_PD0))
#define P_SPI1_MISO	(P_DEFINED | P_IDENT(GPIO_PD1))
#define P_SPI1_SCK	(P_DEFINED | P_IDENT(GPIO_PD2))
#define P_SPI1_SS	(P_DEFINED | P_IDENT(GPIO_PD3))
#define P_SPI1_SSEL1	(P_DEFINED | P_IDENT(GPIO_PD4))
#define P_SPI2_MOSI	(P_DEFINED | P_IDENT(GPIO_PD5))
#define P_SPI2_MISO	(P_DEFINED | P_IDENT(GPIO_PD6))
#define P_SPI2_SCK	(P_DEFINED | P_IDENT(GPIO_PD7))
#define P_SPI2_SS	(P_DEFINED | P_IDENT(GPIO_PD8))
#define P_SPI2_SSEL1	(P_DEFINED | P_IDENT(GPIO_PD9))
#define P_UART1_RX	(P_DEFINED | P_IDENT(GPIO_PD10))
#define P_UART1_TX	(P_DEFINED | P_IDENT(GPIO_PD11))
#define P_UART2_RX	(P_DEFINED | P_IDENT(GPIO_PD12))
#define P_UART2_TX	(P_DEFINED | P_IDENT(GPIO_PD13))

#define P_SPORT2_RSCLK	(P_DEFINED | P_IDENT(GPIO_PE0))
#define P_SPORT2_RFS	(P_DEFINED | P_IDENT(GPIO_PE1))
#define P_SPORT2_DRPRI	(P_DEFINED | P_IDENT(GPIO_PE2))
#define P_SPORT2_DRSEC	(P_DEFINED | P_IDENT(GPIO_PE3))
#define P_SPORT2_TSCLK	(P_DEFINED | P_IDENT(GPIO_PE4))
#define P_SPORT2_TFS	(P_DEFINED | P_IDENT(GPIO_PE5))
#define P_SPORT2_DTPRI	(P_DEFINED | P_IDENT(GPIO_PE6))
#define P_SPORT2_DTSEC	(P_DEFINED | P_IDENT(GPIO_PE7))
#define P_SPORT3_RSCLK	(P_DEFINED | P_IDENT(GPIO_PE8))
#define P_SPORT3_RFS	(P_DEFINED | P_IDENT(GPIO_PE9))
#define P_SPORT3_DRPRI	(P_DEFINED | P_IDENT(GPIO_PE10))
#define P_SPORT3_DRSEC	(P_DEFINED | P_IDENT(GPIO_PE11))
#define P_SPORT3_TSCLK	(P_DEFINED | P_IDENT(GPIO_PE12))
#define P_SPORT3_TFS	(P_DEFINED | P_IDENT(GPIO_PE13))
#define P_SPORT3_DTPRI	(P_DEFINED | P_IDENT(GPIO_PE14))
#define P_SPORT3_DTSEC	(P_DEFINED | P_IDENT(GPIO_PE15))

#define P_PPI0_FS3	(P_DEFINED | P_IDENT(GPIO_PF3))
#define P_PPI0_D15	(P_DEFINED | P_IDENT(GPIO_PF4))
#define P_PPI0_D14	(P_DEFINED | P_IDENT(GPIO_PF5))
#define P_PPI0_D13	(P_DEFINED | P_IDENT(GPIO_PF6))
#define P_PPI0_D12	(P_DEFINED | P_IDENT(GPIO_PF7))
#define P_PPI0_D11	(P_DEFINED | P_IDENT(GPIO_PF8))
#define P_PPI0_D10	(P_DEFINED | P_IDENT(GPIO_PF9))
#define P_PPI0_D9	(P_DEFINED | P_IDENT(GPIO_PF10))
#define P_PPI0_D8	(P_DEFINED | P_IDENT(GPIO_PF11))

#define P_PPI0_D4	(P_DEFINED | P_IDENT(GPIO_PF15))
#define P_PPI0_D5	(P_DEFINED | P_IDENT(GPIO_PF14))
#define P_PPI0_D6	(P_DEFINED | P_IDENT(GPIO_PF13))
#define P_PPI0_D7	(P_DEFINED | P_IDENT(GPIO_PF12))
#define P_SPI0_SSEL7	(P_DEFINED | P_IDENT(GPIO_PF7))
#define P_SPI0_SSEL6	(P_DEFINED | P_IDENT(GPIO_PF6))
#define P_SPI0_SSEL5	(P_DEFINED | P_IDENT(GPIO_PF5))
#define P_SPI0_SSEL4	(P_DEFINED | P_IDENT(GPIO_PF4))
#define P_SPI0_SSEL3	(P_DEFINED | P_IDENT(GPIO_PF3))
#define P_SPI0_SSEL2	(P_DEFINED | P_IDENT(GPIO_PF2))
#define P_SPI0_SSEL1	(P_DEFINED | P_IDENT(GPIO_PF1))
#define P_SPI0_SS	(P_DEFINED | P_IDENT(GPIO_PF0))
#define P_DEFAULT_BOOT_SPI_CS P_SPI0_SSEL2

#endif /* _MACH_PORTMUX_H_ */
