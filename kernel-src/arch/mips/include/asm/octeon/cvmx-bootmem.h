/***********************license start***************
 * Author: Cavium Networks
 *
 * Contact: support@caviumnetworks.com
 * This file is part of the OCTEON SDK
 *
 * Copyright (c) 2003-2008 Cavium Networks
 *
 * This file is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 2, as
 * published by the Free Software Foundation.
 *
 * This file is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this file; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * or visit http://www.gnu.org/licenses/.
 *
 * This file may also be available under a different license from Cavium.
 * Contact Cavium Networks for more information
 ***********************license end**************************************/

/*
 * Simple allocate only memory allocator.  Used to allocate memory at
 * application start time.
 */

#ifndef __CVMX_BOOTMEM_H__
#define __CVMX_BOOTMEM_H__
/* Must be multiple of 8, changing breaks ABI */
#define CVMX_BOOTMEM_NAME_LEN 128

/* Can change without breaking ABI */
#define CVMX_BOOTMEM_NUM_NAMED_BLOCKS 64

/* minimum alignment of bootmem alloced blocks */
#define CVMX_BOOTMEM_ALIGNMENT_SIZE     (16ull)

/* Flags for cvmx_bootmem_phy_mem* functions */
/* Allocate from end of block instead of beginning */
#define CVMX_BOOTMEM_FLAG_END_ALLOC    (1 << 0)

/* Don't do any locking. */
#define CVMX_BOOTMEM_FLAG_NO_LOCKING   (1 << 1)

/* First bytes of each free physical block of memory contain this structure,
 * which is used to maintain the free memory list.  Since the bootloader is
 * only 32 bits, there is a union providing 64 and 32 bit versions.  The
 * application init code converts addresses to 64 bit addresses before the
 * application starts.
 */
struct cvmx_bootmem_block_header {
	/*
	 * Note: these are referenced from assembly routines in the
	 * bootloader, so this structure should not be changed
	 * without changing those routines as well.
	 */
	uint64_t next_block_addr;
	uint64_t size;

};

/*
 * Structure for named memory blocks.  Number of descriptors available
 * can be changed without affecting compatiblity, but name length
 * changes require a bump in the bootmem descriptor version Note: This
 * structure must be naturally 64 bit aligned, as a single memory
 * image will be used by both 32 and 64 bit programs.
 */
struct cvmx_bootmem_named_block_desc {
	/* Base address of named block */
	uint64_t base_addr;
	/*
	 * Size actually allocated for named block (may differ from
	 * requested).
	 */
	uint64_t size;
	/* name of named block */
	char name[CVMX_BOOTMEM_NAME_LEN];
};

/* Current descriptor versions */
/* CVMX bootmem descriptor major version */
#define CVMX_BOOTMEM_DESC_MAJ_VER   3

/* CVMX bootmem descriptor minor version */
#define CVMX_BOOTMEM_DESC_MIN_VER   0

/* First three members of cvmx_bootmem_desc_t are left in original
 * positions for backwards compatibility.
 */
struct cvmx_bootmem_desc {
	/* spinlock to control access to list */
	uint32_t lock;
	/* flags for indicating various conditions */
	uint32_t flags;
	uint64_t head_addr;

	/* Incremented when incompatible changes made */
	uint32_t major_version;

	/*
	 * Incremented changed when compatible changes made, reset to
	 * zero when major incremented.
	 */
	uint32_t minor_version;

	uint64_t app_data_addr;
	uint64_t app_data_size;

	/* number of elements in named blocks array */
	uint32_t named_block_num_blocks;

	/* length of name array in bootmem blocks */
	uint32_t named_block_name_len;
	/* address of named memory block descriptors */
	uint64_t named_block_array_addr;

};

/**
 * Initialize the boot alloc memory structures. This is
 * normally called inside of cvmx_user_app_init()
 *
 * @mem_desc_ptr:	Address of the free memory list
 */
extern int cvmx_bootmem_init(void *mem_desc_ptr);

/**
 * Allocate a block of memory from the free list that was passed
 * to the application by the bootloader.
 * This is an allocate-only algorithm, so freeing memory is not possible.
 *
 * @size:      Size in bytes of block to allocate
 * @alignment: Alignment required - must be power of 2
 *
 * Returns pointer to block of memory, NULL on error
 */
extern void *cvmx_bootmem_alloc(uint64_t size, uint64_t alignment);

/**
 * Allocate a block of memory from the free list that was
 * passed to the application by the bootloader at a specific
 * address. This is an allocate-only algorithm, so
 * freeing memory is not possible. Allocation will fail if
 * memory cannot be allocated at the specified address.
 *
 * @size:      Size in bytes of block to allocate
 * @address:   Physical address to allocate memory at.  If this memory is not
 *                  available, the allocation fails.
 * @alignment: Alignment required - must be power of 2
 * Returns pointer to block of memory, NULL on error
 */
extern void *cvmx_bootmem_alloc_address(uint64_t size, uint64_t address,
					uint64_t alignment);

/**
 * Allocate a block of memory from the free list that was
 * passed to the application by the bootloader within a specified
 * address range. This is an allocate-only algorithm, so
 * freeing memory is not possible. Allocation will fail if
 * memory cannot be allocated in the requested range.
 *
 * @size:      Size in bytes of block to allocate
 * @min_addr:  defines the minimum address of the range
 * @max_addr:  defines the maximum address of the range
 * @alignment: Alignment required - must be power of 2
 * Returns pointer to block of memory, NULL on error
 */
extern void *cvmx_bootmem_alloc_range(uint64_t size, uint64_t alignment,
				      uint64_t min_addr, uint64_t max_addr);

/**
 * Frees a previously allocated named bootmem block.
 *
 * @name:   name of block to free
 *
 * Returns 0 on failure,
 *         !0 on success
 */
extern int cvmx_bootmem_free_named(char *name);

/**
 * Finds a named bootmem block by name.
 *
 * @name:   name of block to free
 *
 * Returns pointer to named block descriptor on success
 *         0 on failure
 */
struct cvmx_bootmem_named_block_desc *cvmx_bootmem_find_named_block(char *name);

/**
 * Allocates a block of physical memory from the free list, at
 * (optional) requested address and alignment.
 *
 * @req_size: size of region to allocate.  All requests are rounded up
 *            to be a multiple CVMX_BOOTMEM_ALIGNMENT_SIZE bytes size
 *
 * @address_min: Minimum address that block can occupy.
 *
 * @address_max: Specifies the maximum address_min (inclusive) that
 *               the allocation can use.
 *
 * @alignment: Requested alignment of the block.  If this alignment
 *             cannot be met, the allocation fails.  This must be a
 *             power of 2.  (Note: Alignment of
 *             CVMX_BOOTMEM_ALIGNMENT_SIZE bytes is required, and
 *             internally enforced.  Requested alignments of less than
 *             CVMX_BOOTMEM_ALIGNMENT_SIZE are set to
 *             CVMX_BOOTMEM_ALIGNMENT_SIZE.)
 *
 * @flags:     Flags to control options for the allocation.
 *
 * Returns physical address of block allocated, or -1 on failure
 */
int64_t cvmx_bootmem_phy_alloc(uint64_t req_size, uint64_t address_min,
			       uint64_t address_max, uint64_t alignment,
			       uint32_t flags);

/**
 * Finds a named memory block by name.
 * Also used for finding an unused entry in the named block table.
 *
 * @name: Name of memory block to find.  If NULL pointer given, then
 *        finds unused descriptor, if available.
 *
 * @flags: Flags to control options for the allocation.
 *
 * Returns Pointer to memory block descriptor, NULL if not found.
 *         If NULL returned when name parameter is NULL, then no memory
 *         block descriptors are available.
 */
struct cvmx_bootmem_named_block_desc *
cvmx_bootmem_phy_named_block_find(char *name, uint32_t flags);

/**
 * Frees a named block.
 *
 * @name:   name of block to free
 * @flags:  flags for passing options
 *
 * Returns 0 on failure
 *         1 on success
 */
int cvmx_bootmem_phy_named_block_free(char *name, uint32_t flags);

/**
 * Frees a block to the bootmem allocator list.  This must
 * be used with care, as the size provided must match the size
 * of the block that was allocated, or the list will become
 * corrupted.
 *
 * IMPORTANT:  This is only intended to be used as part of named block
 * frees and initial population of the free memory list.
 *                                                      *
 *
 * @phy_addr: physical address of block
 * @size:     size of block in bytes.
 * @flags:    flags for passing options
 *
 * Returns 1 on success,
 *         0 on failure
 */
int __cvmx_bootmem_phy_free(uint64_t phy_addr, uint64_t size, uint32_t flags);

/**
 * Locks the bootmem allocator.  This is useful in certain situations
 * where multiple allocations must be made without being interrupted.
 * This should be used with the CVMX_BOOTMEM_FLAG_NO_LOCKING flag.
 *
 */
void cvmx_bootmem_lock(void);

/**
 * Unlocks the bootmem allocator.  This is useful in certain situations
 * where multiple allocations must be made without being interrupted.
 * This should be used with the CVMX_BOOTMEM_FLAG_NO_LOCKING flag.
 *
 */
void cvmx_bootmem_unlock(void);

#endif /*   __CVMX_BOOTMEM_H__ */
