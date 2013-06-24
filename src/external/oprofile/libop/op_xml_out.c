/**
 * @file op_xml_out.c
 * C utility routines for writing XML
 *
 * @remark Copyright 2008 OProfile authors
 * @remark Read the file COPYING
 *
 * @author Dave Nomura
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "op_xml_out.h"

char const * xml_tag_map[] = {
	"NONE",
	"id",
	"profile",
		"processor",
		"cputype",
		"title",
		"schemaversion",
		"mhz",
	"setup",
	"timersetup",
		"rtcinterrupts",
	"eventsetup",
		"eventname",
		"unitmask",
		"setupcount",
		"separatedcpus",
	"options",
		"session", "debuginfo", "details", "excludedependent",
		"excludesymbols", "imagepath", "includesymbols", "merge",
	"classes",
	"class",
		"cpu",
		"event",
		"mask",
	"process",
		"pid",
	"thread",
		"tid",
	"binary",
	"module",
		"name",
	"callers",
	"callees",
	"symbol",
		"idref",
		"self",
		"detaillo",
		"detailhi",
	"symboltable",
	"symboldata",
		"startingaddr",
		"file",
		"line",
		"codelength",
	"summarydata",
	"sampledata",
	"count",
	"detailtable",
	"symboldetails",
	"detaildata",
		"vmaoffset",
	"bytestable",
	"bytes",
	"help_events",
	"header",
		"title",
		"doc",
	"event",
		"event_name",
		"group",
		"desc",
		"counter_mask",
		"min_count",
	"unit_masks",
		"default",
	"unit_mask",
		"mask",
		"desc"
};

#define MAX_BUF_LEN 2048
char const * xml_tag_name(tag_t tag)
{
	return xml_tag_map[tag];
}


void open_xml_element(tag_t tag, int with_attrs, char * buffer)
{
	char const * tag_name = xml_tag_name(tag);
	unsigned int const max_len = strlen(tag_name) + 3;
	char tmp_buf[MAX_BUF_LEN];

	if (max_len >= sizeof(tmp_buf))
		fprintf(stderr,"Warning: open_xml_element: buffer overflow %d\n", max_len);

	if (snprintf(tmp_buf, sizeof(tmp_buf), "<%s%s", tag_name,
		(with_attrs ? " " : ">\n")) < 0) {
		fprintf(stderr,"open_xml_element: snprintf failed\n");
		exit(EXIT_FAILURE);
	}
	strncat(buffer, tmp_buf, sizeof(tmp_buf));
}


void close_xml_element(tag_t tag, int has_nested, char * buffer)
{
	char const * tag_name = xml_tag_name(tag);
	unsigned int const max_len = strlen(tag_name) + 3;
	char tmp_buf[MAX_BUF_LEN];

	if (max_len >= sizeof(tmp_buf))
		fprintf(stderr,"Warning: close_xml_element: buffer overflow %d\n", max_len);

	if (tag == NONE) {
		if (snprintf(tmp_buf, sizeof(tmp_buf), "%s\n", (has_nested ? ">" : "/>")) < 0) {
			fprintf(stderr, "close_xml_element: snprintf failed\n");
			exit(EXIT_FAILURE);
		}
	} else {
		if (snprintf(tmp_buf, sizeof(tmp_buf), "</%s>\n", tag_name) < 0) {
			fprintf(stderr, "close_xml_element: snprintf failed\n");
			exit(EXIT_FAILURE);
		}
	}
	strncat(buffer, tmp_buf, sizeof(tmp_buf));
}


void init_xml_int_attr(tag_t attr, int value, char * buffer)
{
	char const * attr_name = xml_tag_name(attr);
	char tmp_buf[MAX_BUF_LEN];
	unsigned int const max_len = strlen(attr_name) + 50;

	if (max_len >= sizeof(tmp_buf)) {
		fprintf(stderr,
			"Warning: init_xml_int_attr: buffer overflow %d\n", max_len);
	}


	if (snprintf(tmp_buf, sizeof(tmp_buf), " %s=\"%d\"", attr_name, value) < 0) {
		fprintf(stderr,"init_xml_int_attr: snprintf failed\n");
		exit(EXIT_FAILURE);
	}
	strncat(buffer, tmp_buf, sizeof(tmp_buf));
}


void init_xml_dbl_attr(tag_t attr, double value, char * buffer)
{
	char const * attr_name = xml_tag_name(attr);
	unsigned int const max_len = strlen(attr_name) + 50;
	char tmp_buf[MAX_BUF_LEN];

	if (max_len >= sizeof(tmp_buf))
		fprintf(stderr, "Warning: init_xml_dbl_attr: buffer overflow %d\n", max_len);

	if (snprintf(tmp_buf, sizeof(tmp_buf), " %s=\"%.2f\"", attr_name, value) < 0) {
		fprintf(stderr, "init_xml_dbl_attr: snprintf failed\n");
		exit(EXIT_FAILURE);
	}
	strncat(buffer, tmp_buf, sizeof(tmp_buf));
}


static char * xml_quote(char const * str, char * quote_buf)
{
	int i;
	int pos = 0;
	int len = strlen(str);

	
	quote_buf[pos++] = '"';

	for (i = 0; i < len; i++) {
		if (pos >= MAX_BUF_LEN - 10) {
			fprintf(stderr,"quote_str: buffer overflow %d\n", pos);
			exit(EXIT_FAILURE);
		}

		switch(str[i]) {
		case '&':
			strncpy(quote_buf + pos, "&amp;", 5);
			pos += 5;
			break;
		case '<':
			strncpy(quote_buf + pos, "&lt;", 4);
			pos += 4;
			break;
		case '>':
			strncpy(quote_buf + pos, "&gt;", 4);
			pos += 4;
			break;
		case '"':
			strncpy(quote_buf + pos, "&quot;", 6);
			pos += 6;
			break;
		default:
			quote_buf[pos++] = str[i];
			break;
		}
	}

	quote_buf[pos++] = '"';
	quote_buf[pos++] = '\0';
	return quote_buf;
}


void init_xml_str_attr(tag_t attr, char const * str, char * buffer)
{
	char tmp_buf[MAX_BUF_LEN];
	char quote_buf[MAX_BUF_LEN];
	char const * attr_name = xml_tag_name(attr);
	char const * quote_str = xml_quote(str, quote_buf);
	const unsigned int max_len = strlen(attr_name) + strlen(quote_str) + 10;

	if (max_len >= sizeof(tmp_buf))
		fprintf(stderr, "Warning: init_xml_str_attr: buffer overflow %d\n", max_len);

	if (snprintf(tmp_buf, sizeof(tmp_buf), " %s=""%s""", attr_name, quote_str) < 0) {
		fprintf(stderr,"init_xml_str_attr: snprintf failed\n");
		exit(EXIT_FAILURE);
	}
	strncat(buffer, tmp_buf, sizeof(tmp_buf));
}
