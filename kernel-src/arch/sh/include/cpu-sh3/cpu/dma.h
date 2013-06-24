#ifndef __ASM_CPU_SH3_DMA_H
#define __ASM_CPU_SH3_DMA_H


#if defined(CONFIG_CPU_SUBTYPE_SH7720) || \
    defined(CONFIG_CPU_SUBTYPE_SH7721)
#define SH_DMAC_BASE	0xa4010020
#else
#define SH_DMAC_BASE	0xa4000020
#endif

#if defined(CONFIG_CPU_SUBTYPE_SH7720) || defined(CONFIG_CPU_SUBTYPE_SH7709)
#define DMTE0_IRQ	48
#define DMTE1_IRQ	49
#define DMTE2_IRQ	50
#define DMTE3_IRQ	51
#define DMTE4_IRQ	76
#define DMTE5_IRQ	77
#endif

/* Definitions for the SuperH DMAC */
#define TM_BURST	0x00000020
#define TS_8		0x00000000
#define TS_16		0x00000008
#define TS_32		0x00000010
#define TS_128		0x00000018

#define CHCR_TS_MASK	0x18
#define CHCR_TS_SHIFT	3

#define DMAOR_INIT	DMAOR_DME

/*
 * The SuperH DMAC supports a number of transmit sizes, we list them here,
 * with their respective values as they appear in the CHCR registers.
 */
enum {
	XMIT_SZ_8BIT,
	XMIT_SZ_16BIT,
	XMIT_SZ_32BIT,
	XMIT_SZ_128BIT,
};

static unsigned int ts_shift[] __maybe_unused = {
	[XMIT_SZ_8BIT]		= 0,
	[XMIT_SZ_16BIT]		= 1,
	[XMIT_SZ_32BIT]		= 2,
	[XMIT_SZ_128BIT]	= 4,
};

#endif /* __ASM_CPU_SH3_DMA_H */
