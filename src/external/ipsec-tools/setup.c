/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/ip.h>
#include <netdb.h>

#include "config.h"
#include "libpfkey.h"
#include "var.h"
#include "isakmp_var.h"
#include "isakmp.h"
#include "vmbuf.h"
#include "oakley.h"
#include "ipsec_doi.h"
#include "algorithm.h"
#include "vendorid.h"
#include "proposal.h"
#include "sainfo.h"
#include "localconf.h"
#include "remoteconf.h"
#include "sockmisc.h"
#include "grabmyaddr.h"
#include "plog.h"
#include "admin.h"
#include "privsep.h"

static struct myaddrs myaddrs[2];
static struct etypes main_mode = { .type = ISAKMP_ETYPE_IDENT };
static struct localconf localconf;
static struct remoteconf remoteconf;
static struct sainfo sainfo;
static char *pre_shared_key;

struct localconf *lcconf = &localconf;
char *script_names[SCRIPT_MAX + 1];

static void set_default()
{
    localconf.myaddrs = &myaddrs[0];
    localconf.port_isakmp = PORT_ISAKMP;
    localconf.port_isakmp_natt = PORT_ISAKMP_NATT;
    localconf.default_af = AF_INET;
    localconf.pad_random = LC_DEFAULT_PAD_RANDOM;
    localconf.pad_randomlen = LC_DEFAULT_PAD_RANDOM;
    localconf.pad_strict = LC_DEFAULT_PAD_STRICT;
    localconf.pad_excltail = LC_DEFAULT_PAD_EXCLTAIL;
    localconf.retry_counter = 10;
    localconf.retry_interval = 3;
    localconf.count_persend = LC_DEFAULT_COUNT_PERSEND;
    localconf.secret_size = LC_DEFAULT_SECRETSIZE;
    localconf.retry_checkph1 = LC_DEFAULT_RETRY_CHECKPH1;
    localconf.wait_ph2complete = LC_DEFAULT_WAIT_PH2COMPLETE;
    localconf.natt_ka_interval = LC_DEFAULT_NATT_KA_INTERVAL;
    localconf.pathinfo[LC_PATHTYPE_CERT] = "/";

    remoteconf.etypes = &main_mode;
    remoteconf.doitype = IPSEC_DOI;
    remoteconf.sittype = IPSECDOI_SIT_IDENTITY_ONLY;
    remoteconf.idvtype = IDTYPE_ADDRESS;
    remoteconf.nonce_size = DEFAULT_NONCE_SIZE;

    remoteconf.ike_frag = TRUE;
    remoteconf.esp_frag = IP_MAXPACKET;
    remoteconf.ini_contact = TRUE;
    remoteconf.pcheck_level = PROP_CHECK_OBEY;
    remoteconf.verify_identifier = FALSE;
    remoteconf.verify_cert = TRUE;
    remoteconf.getcert_method = ISAKMP_GETCERT_PAYLOAD;
    remoteconf.certtype = ISAKMP_CERT_X509SIGN;
    remoteconf.getcacert_method = ISAKMP_GETCERT_LOCALFILE;
    remoteconf.cacerttype = ISAKMP_CERT_X509SIGN;
    remoteconf.send_cert = TRUE;
    remoteconf.send_cr = TRUE;
    remoteconf.gen_policy = TRUE;
    remoteconf.retry_counter = LC_DEFAULT_RETRY_COUNTER;
    remoteconf.retry_interval = LC_DEFAULT_RETRY_INTERVAL;
    remoteconf.nat_traversal = TRUE;
    remoteconf.rsa_private = genlist_init();
    remoteconf.rsa_public = genlist_init();
    remoteconf.dpd = TRUE;
    remoteconf.dpd_interval = 0;
    remoteconf.dpd_retry = 5;
    remoteconf.dpd_maxfails = 5;

    sainfo.lifetime = IPSECDOI_ATTR_SA_LD_SEC_DEFAULT;
    sainfo.lifebyte = IPSECDOI_ATTR_SA_LD_KB_MAX;
}

static void set_address(char *server, char *port)
{
    struct addrinfo hints = {
        .ai_flags = AI_NUMERICSERV,
#ifndef INET6
        .ai_family = AF_INET,
#else
        .ai_family = AF_UNSPEC,
#endif
        .ai_socktype = SOCK_DGRAM,
    };
    struct addrinfo *r;

    if (getaddrinfo(server, port, &hints, &r) != 0) {
        do_plog(LLV_ERROR, "Cannot resolve server address\n");
        exit(1);
    }
    if (r->ai_next) {
        do_plog(LLV_WARNING, "Multiple server address found\n");
    }
    remoteconf.remote = dupsaddr(r->ai_addr);
    freeaddrinfo(r);

    myaddrs[0].addr = getlocaladdr(remoteconf.remote);
    if (!myaddrs[0].addr) {
        do_plog(LLV_ERROR, "Cannot get local address\n");
        exit(1);
    }
}

static void add_proposal(int auth, int hash, int encryption, int length)
{
    struct isakmpsa *p = calloc(1, sizeof(struct isakmpsa));
    p->prop_no = 1;
    p->lifetime = OAKLEY_ATTR_SA_LD_SEC_DEFAULT;
    p->enctype = encryption;
    p->encklen = length;
    p->authmethod = auth;
    p->hashtype = hash;
    p->dh_group = OAKLEY_ATTR_GRP_DESC_MODP1024;
    p->vendorid = VENDORID_UNKNOWN;
    p->rmconf = &remoteconf;

    if (!remoteconf.proposal) {
      p->trns_no = 1;
      remoteconf.proposal = p;
    } else {
        struct isakmpsa *q = remoteconf.proposal;
        while (q->next) {
            q = q->next;
        }
        p->trns_no = q->trns_no + 1;
        q->next = p;
    }
}

static void add_sainfo_algorithm(int class, int algorithm, int length)
{
    struct sainfoalg *p = calloc(1, sizeof(struct sainfoalg));
    p->alg = algorithm;
    p->encklen = length;

    if (!sainfo.algs[class]) {
        sainfo.algs[class] = p;
    } else {
        struct sainfoalg *q = sainfo.algs[class];
        while (q->next) {
            q = q->next;
        }
        q->next = p;
    }
}

/* flush; spdflush; */
static void flush()
{
    int key = pfkey_open();
    if (key != -1) {
        pfkey_send_flush(key, SADB_SATYPE_UNSPEC);
        pfkey_send_spdflush(key);
        pfkey_close(key);
    }
}

/* flush; spdflush;
 * spdadd local remote udp -P out ipsec esp/transport//require; */
static void spdadd(struct sockaddr *local, struct sockaddr *remote)
{
    struct __attribute__((packed)) {
        struct sadb_x_policy p;
        struct sadb_x_ipsecrequest q;
    } policy;
    int mask = (local->sa_family == AF_INET) ? sizeof(struct in_addr) * 8 :
               sizeof(struct in6_addr) * 8;
    int key = pfkey_open();
    if (key == -1) {
        do_plog(LLV_ERROR, "Cannot create KEY socket\n");
        exit(1);
    }

    memset(&policy, 0, sizeof(policy));
    policy.p.sadb_x_policy_len = PFKEY_UNIT64(sizeof(policy));
    policy.p.sadb_x_policy_exttype = SADB_X_EXT_POLICY;
    policy.p.sadb_x_policy_type = IPSEC_POLICY_IPSEC;
    policy.p.sadb_x_policy_dir = IPSEC_DIR_OUTBOUND;
#ifdef HAVE_PFKEY_POLICY_PRIORITY
    policy.p.sadb_x_policy_priority = PRIORITY_DEFAULT;
#endif
    policy.q.sadb_x_ipsecrequest_len = sizeof(struct sadb_x_ipsecrequest);
    policy.q.sadb_x_ipsecrequest_proto = IPPROTO_ESP;
    policy.q.sadb_x_ipsecrequest_mode = IPSEC_MODE_TRANSPORT;
    policy.q.sadb_x_ipsecrequest_level = IPSEC_LEVEL_REQUIRE;

    if (pfkey_send_flush(key, SADB_SATYPE_UNSPEC) <= 0 ||
        pfkey_send_spdflush(key) <= 0 ||
        pfkey_send_spdadd(key, local, mask, remote, mask, IPPROTO_UDP,
                          (caddr_t)&policy, sizeof(policy), 0) <= 0) {
        do_plog(LLV_ERROR, "Cannot initialize SA and SPD\n");
        exit(1);
    }
    pfkey_close(key);
    atexit(flush);
}

void setup(int argc, char **argv)
{
    int auth;
    if (argc != 4 && argc != 6) {
        printf("Usage: %s server port pre-shared-key\n"
               "       %s server port my-private-key my-cert ca-cert\n",
               argv[0], argv[0]);
        exit(0);
    }
    set_default();

    /* Set local address and remote address. */
    set_address(argv[1], argv[2]);

    /* Initialize SA and SPD. */
    spdadd(myaddrs[0].addr, remoteconf.remote);

    /* Set local port and remote port. */
    set_port(myaddrs[0].addr, localconf.port_isakmp);
    set_port(remoteconf.remote, localconf.port_isakmp);
#ifdef ENABLE_NATT
    myaddrs[0].next = &myaddrs[1];
    myaddrs[1].addr = dupsaddr(myaddrs[0].addr);
    set_port(myaddrs[1].addr, localconf.port_isakmp_natt);
    myaddrs[1].udp_encap = 1;
#endif

    /* Set authentication method. */
    if (argc == 4) {
        pre_shared_key = argv[3];
        auth = OAKLEY_ATTR_AUTH_METHOD_PSKEY;
    } else {
        remoteconf.idvtype = IDTYPE_ASN1DN;
        remoteconf.myprivfile = argv[3];
        remoteconf.mycertfile = argv[4];
        remoteconf.cacertfile = argv[5];
        auth = OAKLEY_ATTR_AUTH_METHOD_RSASIG;
    }

    /* Create proposals. */
    add_proposal(auth, OAKLEY_ATTR_HASH_ALG_SHA, OAKLEY_ATTR_ENC_ALG_3DES, 0);
    add_proposal(auth, OAKLEY_ATTR_HASH_ALG_MD5, OAKLEY_ATTR_ENC_ALG_3DES, 0);
    add_proposal(auth, OAKLEY_ATTR_HASH_ALG_SHA, OAKLEY_ATTR_ENC_ALG_DES, 0);
    add_proposal(auth, OAKLEY_ATTR_HASH_ALG_MD5, OAKLEY_ATTR_ENC_ALG_DES, 0);
    add_proposal(auth, OAKLEY_ATTR_HASH_ALG_SHA, OAKLEY_ATTR_ENC_ALG_AES, 128);
    add_proposal(auth, OAKLEY_ATTR_HASH_ALG_MD5, OAKLEY_ATTR_ENC_ALG_AES, 128);

    /* Create sainfo algorithms. */
    add_sainfo_algorithm(algclass_ipsec_auth, IPSECDOI_ATTR_AUTH_HMAC_SHA1, 0);
    add_sainfo_algorithm(algclass_ipsec_auth, IPSECDOI_ATTR_AUTH_HMAC_MD5, 0);
    add_sainfo_algorithm(algclass_ipsec_enc, IPSECDOI_ESP_3DES, 0);
    add_sainfo_algorithm(algclass_ipsec_enc, IPSECDOI_ESP_DES, 0);
    add_sainfo_algorithm(algclass_ipsec_enc, IPSECDOI_ESP_AES, 128);
}

/* localconf.h */

vchar_t *getpskbyaddr(struct sockaddr *addr)
{
    return privsep_getpsk(pre_shared_key, strlen(pre_shared_key));
}

vchar_t *getpskbyname(vchar_t *name)
{
    return NULL;
}

void getpathname(char *path, int length, int type, const char *name)
{
    strncpy(path, name, length);
}

/* remoteconf.h */

struct remoteconf *getrmconf(struct sockaddr *addr)
{
    return cmpsaddrwop(addr, remoteconf.remote) ? NULL : &remoteconf;
}

struct isakmpsa *dupisakmpsa(struct isakmpsa *sa)
{
    struct isakmpsa *p = NULL;
    if (sa && (p = malloc(sizeof(struct isakmpsa)))) {
        *p = *sa;
        p->next = NULL;
        if (sa->dhgrp) {
            oakley_setdhgroup(sa->dh_group, &p->dhgrp);
        }
    }
    return p;
}

void delisakmpsa(struct isakmpsa *sa)
{
    while (sa) {
        struct isakmpsa *p = sa->next;
        if (sa->dhgrp) {
            oakley_dhgrp_free(sa->dhgrp);
        }
        free(sa);
        sa = p;
    }
}

struct etypes *check_etypeok(struct remoteconf *rmconf, uint8_t etype)
{
    struct etypes *p = rmconf->etypes;
    while (p && etype != p->type) {
        p = p->next;
    }
    return p;
}

struct remoteconf *foreachrmconf(rmconf_func_t function, void *data)
{
    return (*function)(&remoteconf, data);
}

/* sainfo.h */

struct sainfo *getsainfo(const vchar_t *src, const vchar_t *dst,
                         const vchar_t *peer, int remoteid)
{
    return &sainfo;
}

const char *sainfo2str(const struct sainfo *si)
{
    return "*";
}
