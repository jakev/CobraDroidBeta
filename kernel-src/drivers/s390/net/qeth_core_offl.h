/*
 *  drivers/s390/net/qeth_core_offl.h
 *
 *    Copyright IBM Corp. 2007
 *    Author(s): Thomas Spatzier <tspat@de.ibm.com>,
 *		 Frank Blaschka <frank.blaschka@de.ibm.com>
 */

#ifndef __QETH_CORE_OFFL_H__
#define __QETH_CORE_OFFL_H__

struct qeth_eddp_element {
	u32 flags;
	u32 length;
	void *addr;
};

struct qeth_eddp_context {
	atomic_t refcnt;
	enum qeth_large_send_types type;
	int num_pages;			    /* # of allocated pages */
	u8 **pages;			    /* pointers to pages */
	int offset;			    /* offset in ctx during creation */
	int num_elements;		    /* # of required 'SBALEs' */
	struct qeth_eddp_element *elements; /* array of 'SBALEs' */
	int elements_per_skb;		    /* # of 'SBALEs' per skb **/
};

struct qeth_eddp_context_reference {
	struct list_head list;
	struct qeth_eddp_context *ctx;
};

struct qeth_eddp_data {
	struct qeth_hdr qh;
	struct ethhdr mac;
	__be16 vlan[2];
	union {
		struct {
			struct iphdr h;
			u8 options[40];
		} ip4;
		struct {
			struct ipv6hdr h;
		} ip6;
	} nh;
	u8 nhl;
	void *nh_in_ctx;	/* address of nh within the ctx */
	union {
		struct {
			struct tcphdr h;
			u8 options[40];
		} tcp;
	} th;
	u8 thl;
	void *th_in_ctx;	/* address of th within the ctx */
	struct sk_buff *skb;
	int skb_offset;
	int frag;
	int frag_offset;
} __attribute__ ((packed));

extern struct qeth_eddp_context *qeth_eddp_create_context(struct qeth_card *,
		 struct sk_buff *, struct qeth_hdr *, unsigned char);
extern void qeth_eddp_put_context(struct qeth_eddp_context *);
extern int qeth_eddp_fill_buffer(struct qeth_qdio_out_q *,
		struct qeth_eddp_context *, int);
extern void qeth_eddp_buf_release_contexts(struct qeth_qdio_out_buffer *);
extern int qeth_eddp_check_buffers_for_context(struct qeth_qdio_out_q *,
		struct qeth_eddp_context *);

void qeth_tso_fill_header(struct qeth_card *, struct qeth_hdr *,
		struct sk_buff *);
void qeth_tx_csum(struct sk_buff *skb);

#endif /* __QETH_CORE_EDDP_H__ */
