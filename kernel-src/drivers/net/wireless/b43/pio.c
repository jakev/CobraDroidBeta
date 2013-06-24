/*

  Broadcom B43 wireless driver

  PIO data transfer

  Copyright (c) 2005-2008 Michael Buesch <mb@bu3sch.de>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; see the file COPYING.  If not, write to
  the Free Software Foundation, Inc., 51 Franklin Steet, Fifth Floor,
  Boston, MA 02110-1301, USA.

*/

#include "b43.h"
#include "pio.h"
#include "dma.h"
#include "main.h"
#include "xmit.h"

#include <linux/delay.h>


static void b43_pio_rx_work(struct work_struct *work);


static u16 generate_cookie(struct b43_pio_txqueue *q,
			   struct b43_pio_txpacket *pack)
{
	u16 cookie;

	/* Use the upper 4 bits of the cookie as
	 * PIO controller ID and store the packet index number
	 * in the lower 12 bits.
	 * Note that the cookie must never be 0, as this
	 * is a special value used in RX path.
	 * It can also not be 0xFFFF because that is special
	 * for multicast frames.
	 */
	cookie = (((u16)q->index + 1) << 12);
	cookie |= pack->index;

	return cookie;
}

static
struct b43_pio_txqueue * parse_cookie(struct b43_wldev *dev,
				      u16 cookie,
				      struct b43_pio_txpacket **pack)
{
	struct b43_pio *pio = &dev->pio;
	struct b43_pio_txqueue *q = NULL;
	unsigned int pack_index;

	switch (cookie & 0xF000) {
	case 0x1000:
		q = pio->tx_queue_AC_BK;
		break;
	case 0x2000:
		q = pio->tx_queue_AC_BE;
		break;
	case 0x3000:
		q = pio->tx_queue_AC_VI;
		break;
	case 0x4000:
		q = pio->tx_queue_AC_VO;
		break;
	case 0x5000:
		q = pio->tx_queue_mcast;
		break;
	}
	if (B43_WARN_ON(!q))
		return NULL;
	pack_index = (cookie & 0x0FFF);
	if (B43_WARN_ON(pack_index >= ARRAY_SIZE(q->packets)))
		return NULL;
	*pack = &q->packets[pack_index];

	return q;
}

static u16 index_to_pioqueue_base(struct b43_wldev *dev,
				  unsigned int index)
{
	static const u16 bases[] = {
		B43_MMIO_PIO_BASE0,
		B43_MMIO_PIO_BASE1,
		B43_MMIO_PIO_BASE2,
		B43_MMIO_PIO_BASE3,
		B43_MMIO_PIO_BASE4,
		B43_MMIO_PIO_BASE5,
		B43_MMIO_PIO_BASE6,
		B43_MMIO_PIO_BASE7,
	};
	static const u16 bases_rev11[] = {
		B43_MMIO_PIO11_BASE0,
		B43_MMIO_PIO11_BASE1,
		B43_MMIO_PIO11_BASE2,
		B43_MMIO_PIO11_BASE3,
		B43_MMIO_PIO11_BASE4,
		B43_MMIO_PIO11_BASE5,
	};

	if (dev->dev->id.revision >= 11) {
		B43_WARN_ON(index >= ARRAY_SIZE(bases_rev11));
		return bases_rev11[index];
	}
	B43_WARN_ON(index >= ARRAY_SIZE(bases));
	return bases[index];
}

static u16 pio_txqueue_offset(struct b43_wldev *dev)
{
	if (dev->dev->id.revision >= 11)
		return 0x18;
	return 0;
}

static u16 pio_rxqueue_offset(struct b43_wldev *dev)
{
	if (dev->dev->id.revision >= 11)
		return 0x38;
	return 8;
}

static struct b43_pio_txqueue * b43_setup_pioqueue_tx(struct b43_wldev *dev,
						      unsigned int index)
{
	struct b43_pio_txqueue *q;
	struct b43_pio_txpacket *p;
	unsigned int i;

	q = kzalloc(sizeof(*q), GFP_KERNEL);
	if (!q)
		return NULL;
	spin_lock_init(&q->lock);
	q->dev = dev;
	q->rev = dev->dev->id.revision;
	q->mmio_base = index_to_pioqueue_base(dev, index) +
		       pio_txqueue_offset(dev);
	q->index = index;

	q->free_packet_slots = B43_PIO_MAX_NR_TXPACKETS;
	if (q->rev >= 8) {
		q->buffer_size = 1920; //FIXME this constant is wrong.
	} else {
		q->buffer_size = b43_piotx_read16(q, B43_PIO_TXQBUFSIZE);
		q->buffer_size -= 80;
	}

	INIT_LIST_HEAD(&q->packets_list);
	for (i = 0; i < ARRAY_SIZE(q->packets); i++) {
		p = &(q->packets[i]);
		INIT_LIST_HEAD(&p->list);
		p->index = i;
		p->queue = q;
		list_add(&p->list, &q->packets_list);
	}

	return q;
}

static struct b43_pio_rxqueue * b43_setup_pioqueue_rx(struct b43_wldev *dev,
						      unsigned int index)
{
	struct b43_pio_rxqueue *q;

	q = kzalloc(sizeof(*q), GFP_KERNEL);
	if (!q)
		return NULL;
	spin_lock_init(&q->lock);
	q->dev = dev;
	q->rev = dev->dev->id.revision;
	q->mmio_base = index_to_pioqueue_base(dev, index) +
		       pio_rxqueue_offset(dev);
	INIT_WORK(&q->rx_work, b43_pio_rx_work);

	/* Enable Direct FIFO RX (PIO) on the engine. */
	b43_dma_direct_fifo_rx(dev, index, 1);

	return q;
}

static void b43_pio_cancel_tx_packets(struct b43_pio_txqueue *q)
{
	struct b43_pio_txpacket *pack;
	unsigned int i;

	for (i = 0; i < ARRAY_SIZE(q->packets); i++) {
		pack = &(q->packets[i]);
		if (pack->skb) {
			dev_kfree_skb_any(pack->skb);
			pack->skb = NULL;
		}
	}
}

static void b43_destroy_pioqueue_tx(struct b43_pio_txqueue *q,
				    const char *name)
{
	if (!q)
		return;
	b43_pio_cancel_tx_packets(q);
	kfree(q);
}

static void b43_destroy_pioqueue_rx(struct b43_pio_rxqueue *q,
				    const char *name)
{
	if (!q)
		return;
	kfree(q);
}

#define destroy_queue_tx(pio, queue) do {				\
	b43_destroy_pioqueue_tx((pio)->queue, __stringify(queue));	\
	(pio)->queue = NULL;						\
  } while (0)

#define destroy_queue_rx(pio, queue) do {				\
	b43_destroy_pioqueue_rx((pio)->queue, __stringify(queue));	\
	(pio)->queue = NULL;						\
  } while (0)

void b43_pio_free(struct b43_wldev *dev)
{
	struct b43_pio *pio;

	if (!b43_using_pio_transfers(dev))
		return;
	pio = &dev->pio;

	destroy_queue_rx(pio, rx_queue);
	destroy_queue_tx(pio, tx_queue_mcast);
	destroy_queue_tx(pio, tx_queue_AC_VO);
	destroy_queue_tx(pio, tx_queue_AC_VI);
	destroy_queue_tx(pio, tx_queue_AC_BE);
	destroy_queue_tx(pio, tx_queue_AC_BK);
}

void b43_pio_stop(struct b43_wldev *dev)
{
	if (!b43_using_pio_transfers(dev))
		return;
	cancel_work_sync(&dev->pio.rx_queue->rx_work);
}

int b43_pio_init(struct b43_wldev *dev)
{
	struct b43_pio *pio = &dev->pio;
	int err = -ENOMEM;

	b43_write32(dev, B43_MMIO_MACCTL, b43_read32(dev, B43_MMIO_MACCTL)
		    & ~B43_MACCTL_BE);
	b43_shm_write16(dev, B43_SHM_SHARED, B43_SHM_SH_RXPADOFF, 0);

	pio->tx_queue_AC_BK = b43_setup_pioqueue_tx(dev, 0);
	if (!pio->tx_queue_AC_BK)
		goto out;

	pio->tx_queue_AC_BE = b43_setup_pioqueue_tx(dev, 1);
	if (!pio->tx_queue_AC_BE)
		goto err_destroy_bk;

	pio->tx_queue_AC_VI = b43_setup_pioqueue_tx(dev, 2);
	if (!pio->tx_queue_AC_VI)
		goto err_destroy_be;

	pio->tx_queue_AC_VO = b43_setup_pioqueue_tx(dev, 3);
	if (!pio->tx_queue_AC_VO)
		goto err_destroy_vi;

	pio->tx_queue_mcast = b43_setup_pioqueue_tx(dev, 4);
	if (!pio->tx_queue_mcast)
		goto err_destroy_vo;

	pio->rx_queue = b43_setup_pioqueue_rx(dev, 0);
	if (!pio->rx_queue)
		goto err_destroy_mcast;

	b43dbg(dev->wl, "PIO initialized\n");
	err = 0;
out:
	return err;

err_destroy_mcast:
	destroy_queue_tx(pio, tx_queue_mcast);
err_destroy_vo:
	destroy_queue_tx(pio, tx_queue_AC_VO);
err_destroy_vi:
	destroy_queue_tx(pio, tx_queue_AC_VI);
err_destroy_be:
	destroy_queue_tx(pio, tx_queue_AC_BE);
err_destroy_bk:
	destroy_queue_tx(pio, tx_queue_AC_BK);
	return err;
}

/* Static mapping of mac80211's queues (priorities) to b43 PIO queues. */
static struct b43_pio_txqueue * select_queue_by_priority(struct b43_wldev *dev,
							 u8 queue_prio)
{
	struct b43_pio_txqueue *q;

	if (b43_modparam_qos) {
		/* 0 = highest priority */
		switch (queue_prio) {
		default:
			B43_WARN_ON(1);
			/* fallthrough */
		case 0:
			q = dev->pio.tx_queue_AC_VO;
			break;
		case 1:
			q = dev->pio.tx_queue_AC_VI;
			break;
		case 2:
			q = dev->pio.tx_queue_AC_BE;
			break;
		case 3:
			q = dev->pio.tx_queue_AC_BK;
			break;
		}
	} else
		q = dev->pio.tx_queue_AC_BE;

	return q;
}

static u16 tx_write_2byte_queue(struct b43_pio_txqueue *q,
				u16 ctl,
				const void *_data,
				unsigned int data_len)
{
	struct b43_wldev *dev = q->dev;
	const u8 *data = _data;

	ctl |= B43_PIO_TXCTL_WRITELO | B43_PIO_TXCTL_WRITEHI;
	b43_piotx_write16(q, B43_PIO_TXCTL, ctl);

	ssb_block_write(dev->dev, data, (data_len & ~1),
			q->mmio_base + B43_PIO_TXDATA,
			sizeof(u16));
	if (data_len & 1) {
		/* Write the last byte. */
		ctl &= ~B43_PIO_TXCTL_WRITEHI;
		b43_piotx_write16(q, B43_PIO_TXCTL, ctl);
		b43_piotx_write16(q, B43_PIO_TXDATA, data[data_len - 1]);
	}

	return ctl;
}

static void pio_tx_frame_2byte_queue(struct b43_pio_txpacket *pack,
				     const u8 *hdr, unsigned int hdrlen)
{
	struct b43_pio_txqueue *q = pack->queue;
	const char *frame = pack->skb->data;
	unsigned int frame_len = pack->skb->len;
	u16 ctl;

	ctl = b43_piotx_read16(q, B43_PIO_TXCTL);
	ctl |= B43_PIO_TXCTL_FREADY;
	ctl &= ~B43_PIO_TXCTL_EOF;

	/* Transfer the header data. */
	ctl = tx_write_2byte_queue(q, ctl, hdr, hdrlen);
	/* Transfer the frame data. */
	ctl = tx_write_2byte_queue(q, ctl, frame, frame_len);

	ctl |= B43_PIO_TXCTL_EOF;
	b43_piotx_write16(q, B43_PIO_TXCTL, ctl);
}

static u32 tx_write_4byte_queue(struct b43_pio_txqueue *q,
				u32 ctl,
				const void *_data,
				unsigned int data_len)
{
	struct b43_wldev *dev = q->dev;
	const u8 *data = _data;

	ctl |= B43_PIO8_TXCTL_0_7 | B43_PIO8_TXCTL_8_15 |
	       B43_PIO8_TXCTL_16_23 | B43_PIO8_TXCTL_24_31;
	b43_piotx_write32(q, B43_PIO8_TXCTL, ctl);

	ssb_block_write(dev->dev, data, (data_len & ~3),
			q->mmio_base + B43_PIO8_TXDATA,
			sizeof(u32));
	if (data_len & 3) {
		u32 value = 0;

		/* Write the last few bytes. */
		ctl &= ~(B43_PIO8_TXCTL_8_15 | B43_PIO8_TXCTL_16_23 |
			 B43_PIO8_TXCTL_24_31);
		data = &(data[data_len - 1]);
		switch (data_len & 3) {
		case 3:
			ctl |= B43_PIO8_TXCTL_16_23;
			value |= (u32)(*data) << 16;
			data--;
		case 2:
			ctl |= B43_PIO8_TXCTL_8_15;
			value |= (u32)(*data) << 8;
			data--;
		case 1:
			value |= (u32)(*data);
		}
		b43_piotx_write32(q, B43_PIO8_TXCTL, ctl);
		b43_piotx_write32(q, B43_PIO8_TXDATA, value);
	}

	return ctl;
}

static void pio_tx_frame_4byte_queue(struct b43_pio_txpacket *pack,
				     const u8 *hdr, unsigned int hdrlen)
{
	struct b43_pio_txqueue *q = pack->queue;
	const char *frame = pack->skb->data;
	unsigned int frame_len = pack->skb->len;
	u32 ctl;

	ctl = b43_piotx_read32(q, B43_PIO8_TXCTL);
	ctl |= B43_PIO8_TXCTL_FREADY;
	ctl &= ~B43_PIO8_TXCTL_EOF;

	/* Transfer the header data. */
	ctl = tx_write_4byte_queue(q, ctl, hdr, hdrlen);
	/* Transfer the frame data. */
	ctl = tx_write_4byte_queue(q, ctl, frame, frame_len);

	ctl |= B43_PIO8_TXCTL_EOF;
	b43_piotx_write32(q, B43_PIO_TXCTL, ctl);
}

static int pio_tx_frame(struct b43_pio_txqueue *q,
			struct sk_buff *skb)
{
	struct b43_pio_txpacket *pack;
	struct b43_txhdr txhdr;
	u16 cookie;
	int err;
	unsigned int hdrlen;
	struct ieee80211_tx_info *info = IEEE80211_SKB_CB(skb);

	B43_WARN_ON(list_empty(&q->packets_list));
	pack = list_entry(q->packets_list.next,
			  struct b43_pio_txpacket, list);

	cookie = generate_cookie(q, pack);
	hdrlen = b43_txhdr_size(q->dev);
	err = b43_generate_txhdr(q->dev, (u8 *)&txhdr, skb->data,
				 skb->len, info, cookie);
	if (err)
		return err;

	if (info->flags & IEEE80211_TX_CTL_SEND_AFTER_DTIM) {
		/* Tell the firmware about the cookie of the last
		 * mcast frame, so it can clear the more-data bit in it. */
		b43_shm_write16(q->dev, B43_SHM_SHARED,
				B43_SHM_SH_MCASTCOOKIE, cookie);
	}

	pack->skb = skb;
	if (q->rev >= 8)
		pio_tx_frame_4byte_queue(pack, (const u8 *)&txhdr, hdrlen);
	else
		pio_tx_frame_2byte_queue(pack, (const u8 *)&txhdr, hdrlen);

	/* Remove it from the list of available packet slots.
	 * It will be put back when we receive the status report. */
	list_del(&pack->list);

	/* Update the queue statistics. */
	q->buffer_used += roundup(skb->len + hdrlen, 4);
	q->free_packet_slots -= 1;

	return 0;
}

int b43_pio_tx(struct b43_wldev *dev, struct sk_buff *skb)
{
	struct b43_pio_txqueue *q;
	struct ieee80211_hdr *hdr;
	unsigned long flags;
	unsigned int hdrlen, total_len;
	int err = 0;
	struct ieee80211_tx_info *info = IEEE80211_SKB_CB(skb);

	hdr = (struct ieee80211_hdr *)skb->data;

	if (info->flags & IEEE80211_TX_CTL_SEND_AFTER_DTIM) {
		/* The multicast queue will be sent after the DTIM. */
		q = dev->pio.tx_queue_mcast;
		/* Set the frame More-Data bit. Ucode will clear it
		 * for us on the last frame. */
		hdr->frame_control |= cpu_to_le16(IEEE80211_FCTL_MOREDATA);
	} else {
		/* Decide by priority where to put this frame. */
		q = select_queue_by_priority(dev, skb_get_queue_mapping(skb));
	}

	spin_lock_irqsave(&q->lock, flags);

	hdrlen = b43_txhdr_size(dev);
	total_len = roundup(skb->len + hdrlen, 4);

	if (unlikely(total_len > q->buffer_size)) {
		err = -ENOBUFS;
		b43dbg(dev->wl, "PIO: TX packet longer than queue.\n");
		goto out_unlock;
	}
	if (unlikely(q->free_packet_slots == 0)) {
		err = -ENOBUFS;
		b43warn(dev->wl, "PIO: TX packet overflow.\n");
		goto out_unlock;
	}
	B43_WARN_ON(q->buffer_used > q->buffer_size);

	if (total_len > (q->buffer_size - q->buffer_used)) {
		/* Not enough memory on the queue. */
		err = -EBUSY;
		ieee80211_stop_queue(dev->wl->hw, skb_get_queue_mapping(skb));
		q->stopped = 1;
		goto out_unlock;
	}

	/* Assign the queue number to the ring (if not already done before)
	 * so TX status handling can use it. The mac80211-queue to b43-queue
	 * mapping is static, so we don't need to store it per frame. */
	q->queue_prio = skb_get_queue_mapping(skb);

	err = pio_tx_frame(q, skb);
	if (unlikely(err == -ENOKEY)) {
		/* Drop this packet, as we don't have the encryption key
		 * anymore and must not transmit it unencrypted. */
		dev_kfree_skb_any(skb);
		err = 0;
		goto out_unlock;
	}
	if (unlikely(err)) {
		b43err(dev->wl, "PIO transmission failure\n");
		goto out_unlock;
	}
	q->nr_tx_packets++;

	B43_WARN_ON(q->buffer_used > q->buffer_size);
	if (((q->buffer_size - q->buffer_used) < roundup(2 + 2 + 6, 4)) ||
	    (q->free_packet_slots == 0)) {
		/* The queue is full. */
		ieee80211_stop_queue(dev->wl->hw, skb_get_queue_mapping(skb));
		q->stopped = 1;
	}

out_unlock:
	spin_unlock_irqrestore(&q->lock, flags);

	return err;
}

/* Called with IRQs disabled. */
void b43_pio_handle_txstatus(struct b43_wldev *dev,
			     const struct b43_txstatus *status)
{
	struct b43_pio_txqueue *q;
	struct b43_pio_txpacket *pack = NULL;
	unsigned int total_len;
	struct ieee80211_tx_info *info;

	q = parse_cookie(dev, status->cookie, &pack);
	if (unlikely(!q))
		return;
	B43_WARN_ON(!pack);

	spin_lock(&q->lock); /* IRQs are already disabled. */

	info = IEEE80211_SKB_CB(pack->skb);

	b43_fill_txstatus_report(dev, info, status);

	total_len = pack->skb->len + b43_txhdr_size(dev);
	total_len = roundup(total_len, 4);
	q->buffer_used -= total_len;
	q->free_packet_slots += 1;

	ieee80211_tx_status_irqsafe(dev->wl->hw, pack->skb);
	pack->skb = NULL;
	list_add(&pack->list, &q->packets_list);

	if (q->stopped) {
		ieee80211_wake_queue(dev->wl->hw, q->queue_prio);
		q->stopped = 0;
	}

	spin_unlock(&q->lock);
}

void b43_pio_get_tx_stats(struct b43_wldev *dev,
			  struct ieee80211_tx_queue_stats *stats)
{
	const int nr_queues = dev->wl->hw->queues;
	struct b43_pio_txqueue *q;
	unsigned long flags;
	int i;

	for (i = 0; i < nr_queues; i++) {
		q = select_queue_by_priority(dev, i);

		spin_lock_irqsave(&q->lock, flags);
		stats[i].len = B43_PIO_MAX_NR_TXPACKETS - q->free_packet_slots;
		stats[i].limit = B43_PIO_MAX_NR_TXPACKETS;
		stats[i].count = q->nr_tx_packets;
		spin_unlock_irqrestore(&q->lock, flags);
	}
}

/* Returns whether we should fetch another frame. */
static bool pio_rx_frame(struct b43_pio_rxqueue *q)
{
	struct b43_wldev *dev = q->dev;
	struct b43_rxhdr_fw4 rxhdr;
	u16 len;
	u32 macstat;
	unsigned int i, padding;
	struct sk_buff *skb;
	const char *err_msg = NULL;

	memset(&rxhdr, 0, sizeof(rxhdr));

	/* Check if we have data and wait for it to get ready. */
	if (q->rev >= 8) {
		u32 ctl;

		ctl = b43_piorx_read32(q, B43_PIO8_RXCTL);
		if (!(ctl & B43_PIO8_RXCTL_FRAMERDY))
			return 0;
		b43_piorx_write32(q, B43_PIO8_RXCTL,
				  B43_PIO8_RXCTL_FRAMERDY);
		for (i = 0; i < 10; i++) {
			ctl = b43_piorx_read32(q, B43_PIO8_RXCTL);
			if (ctl & B43_PIO8_RXCTL_DATARDY)
				goto data_ready;
			udelay(10);
		}
	} else {
		u16 ctl;

		ctl = b43_piorx_read16(q, B43_PIO_RXCTL);
		if (!(ctl & B43_PIO_RXCTL_FRAMERDY))
			return 0;
		b43_piorx_write16(q, B43_PIO_RXCTL,
				  B43_PIO_RXCTL_FRAMERDY);
		for (i = 0; i < 10; i++) {
			ctl = b43_piorx_read16(q, B43_PIO_RXCTL);
			if (ctl & B43_PIO_RXCTL_DATARDY)
				goto data_ready;
			udelay(10);
		}
	}
	b43dbg(q->dev->wl, "PIO RX timed out\n");
	return 1;
data_ready:

	/* Get the preamble (RX header) */
	if (q->rev >= 8) {
		ssb_block_read(dev->dev, &rxhdr, sizeof(rxhdr),
			       q->mmio_base + B43_PIO8_RXDATA,
			       sizeof(u32));
	} else {
		ssb_block_read(dev->dev, &rxhdr, sizeof(rxhdr),
			       q->mmio_base + B43_PIO_RXDATA,
			       sizeof(u16));
	}
	/* Sanity checks. */
	len = le16_to_cpu(rxhdr.frame_len);
	if (unlikely(len > 0x700)) {
		err_msg = "len > 0x700";
		goto rx_error;
	}
	if (unlikely(len == 0)) {
		err_msg = "len == 0";
		goto rx_error;
	}

	macstat = le32_to_cpu(rxhdr.mac_status);
	if (macstat & B43_RX_MAC_FCSERR) {
		if (!(q->dev->wl->filter_flags & FIF_FCSFAIL)) {
			/* Drop frames with failed FCS. */
			err_msg = "Frame FCS error";
			goto rx_error;
		}
	}

	/* We always pad 2 bytes, as that's what upstream code expects
	 * due to the RX-header being 30 bytes. In case the frame is
	 * unaligned, we pad another 2 bytes. */
	padding = (macstat & B43_RX_MAC_PADDING) ? 2 : 0;
	skb = dev_alloc_skb(len + padding + 2);
	if (unlikely(!skb)) {
		err_msg = "Out of memory";
		goto rx_error;
	}
	skb_reserve(skb, 2);
	skb_put(skb, len + padding);
	if (q->rev >= 8) {
		ssb_block_read(dev->dev, skb->data + padding, (len & ~3),
			       q->mmio_base + B43_PIO8_RXDATA,
			       sizeof(u32));
		if (len & 3) {
			u32 value;
			char *data;

			/* Read the last few bytes. */
			value = b43_piorx_read32(q, B43_PIO8_RXDATA);
			data = &(skb->data[len + padding - 1]);
			switch (len & 3) {
			case 3:
				*data = (value >> 16);
				data--;
			case 2:
				*data = (value >> 8);
				data--;
			case 1:
				*data = value;
			}
		}
	} else {
		ssb_block_read(dev->dev, skb->data + padding, (len & ~1),
			       q->mmio_base + B43_PIO_RXDATA,
			       sizeof(u16));
		if (len & 1) {
			u16 value;

			/* Read the last byte. */
			value = b43_piorx_read16(q, B43_PIO_RXDATA);
			skb->data[len + padding - 1] = value;
		}
	}

	b43_rx(q->dev, skb, &rxhdr);

	return 1;

rx_error:
	if (err_msg)
		b43dbg(q->dev->wl, "PIO RX error: %s\n", err_msg);
	b43_piorx_write16(q, B43_PIO_RXCTL, B43_PIO_RXCTL_DATARDY);
	return 1;
}

/* RX workqueue. We can sleep, yay! */
static void b43_pio_rx_work(struct work_struct *work)
{
	struct b43_pio_rxqueue *q = container_of(work, struct b43_pio_rxqueue,
						 rx_work);
	unsigned int budget = 50;
	bool stop;

	do {
		spin_lock_irq(&q->lock);
		stop = (pio_rx_frame(q) == 0);
		spin_unlock_irq(&q->lock);
		cond_resched();
		if (stop)
			break;
	} while (--budget);
}

/* Called with IRQs disabled. */
void b43_pio_rx(struct b43_pio_rxqueue *q)
{
	/* Due to latency issues we must run the RX path in
	 * a workqueue to be able to schedule between packets. */
	queue_work(q->dev->wl->hw->workqueue, &q->rx_work);
}

static void b43_pio_tx_suspend_queue(struct b43_pio_txqueue *q)
{
	unsigned long flags;

	spin_lock_irqsave(&q->lock, flags);
	if (q->rev >= 8) {
		b43_piotx_write32(q, B43_PIO8_TXCTL,
				  b43_piotx_read32(q, B43_PIO8_TXCTL)
				  | B43_PIO8_TXCTL_SUSPREQ);
	} else {
		b43_piotx_write16(q, B43_PIO_TXCTL,
				  b43_piotx_read16(q, B43_PIO_TXCTL)
				  | B43_PIO_TXCTL_SUSPREQ);
	}
	spin_unlock_irqrestore(&q->lock, flags);
}

static void b43_pio_tx_resume_queue(struct b43_pio_txqueue *q)
{
	unsigned long flags;

	spin_lock_irqsave(&q->lock, flags);
	if (q->rev >= 8) {
		b43_piotx_write32(q, B43_PIO8_TXCTL,
				  b43_piotx_read32(q, B43_PIO8_TXCTL)
				  & ~B43_PIO8_TXCTL_SUSPREQ);
	} else {
		b43_piotx_write16(q, B43_PIO_TXCTL,
				  b43_piotx_read16(q, B43_PIO_TXCTL)
				  & ~B43_PIO_TXCTL_SUSPREQ);
	}
	spin_unlock_irqrestore(&q->lock, flags);
}

void b43_pio_tx_suspend(struct b43_wldev *dev)
{
	b43_power_saving_ctl_bits(dev, B43_PS_AWAKE);
	b43_pio_tx_suspend_queue(dev->pio.tx_queue_AC_BK);
	b43_pio_tx_suspend_queue(dev->pio.tx_queue_AC_BE);
	b43_pio_tx_suspend_queue(dev->pio.tx_queue_AC_VI);
	b43_pio_tx_suspend_queue(dev->pio.tx_queue_AC_VO);
	b43_pio_tx_suspend_queue(dev->pio.tx_queue_mcast);
}

void b43_pio_tx_resume(struct b43_wldev *dev)
{
	b43_pio_tx_resume_queue(dev->pio.tx_queue_mcast);
	b43_pio_tx_resume_queue(dev->pio.tx_queue_AC_VO);
	b43_pio_tx_resume_queue(dev->pio.tx_queue_AC_VI);
	b43_pio_tx_resume_queue(dev->pio.tx_queue_AC_BE);
	b43_pio_tx_resume_queue(dev->pio.tx_queue_AC_BK);
	b43_power_saving_ctl_bits(dev, 0);
}
