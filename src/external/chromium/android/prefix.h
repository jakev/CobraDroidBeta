#ifndef ANDROID_CHROMIUM_PREFIX_H
#define ANDROID_CHROMIUM_PREFIX_H

// C++ specific changes
#ifdef __cplusplus
// chromium refers to stl functions without std::
#include <algorithm>
using std::find;
using std::reverse;
using std::search;

// Our pwrite has buf declared void*.
ssize_t pwrite(int fd, const void *buf, size_t count, off_t offset) {
    return pwrite(fd, const_cast<void*>(buf), count, offset);
}

// Called by command_line.cc to shorten the process name. Not needed for
// network stack.
inline int prctl(int option, ...) { return 0; }

namespace std {
// our new does not trigger oom
inline void set_new_handler(void (*p)()) {}
}

// Chromium expects size_t to be a signed int on linux but Android defines it
// as unsigned.
inline size_t abs(size_t x) { return x; }
#endif

// Needed by base_paths.cc for close() function.
#include <unistd.h>
// Need to define assert before logging.h undefines it.
#include <assert.h>
// logging.cc needs pthread_mutex_t
#include <pthread.h>
// needed for isalpha
#include <ctype.h>
// needed for sockaddr_in
#include <netinet/in.h>

// Implemented in bionic but not exposed.
extern char* mkdtemp(char* path);
extern time_t timegm(struct tm* const tmp);

// This will probably need a real implementation.
#define F_ULOCK 0
#define F_LOCK 1
inline int lockf(int fd, int cmd, off_t len) { return -1; }

// We have posix monotonic clocks but don't define this...
#define _POSIX_MONOTONIC_CLOCK 1

// Disable langinfo in icu
#define U_GAVE_NL_LANGINFO_CODESET 0

#endif
