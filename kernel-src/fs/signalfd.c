/*
 *  fs/signalfd.c
 *
 *  Copyright (C) 2003  Linus Torvalds
 *
 *  Mon Mar 5, 2007: Davide Libenzi <davidel@xmailserver.org>
 *      Changed ->read() to return a siginfo strcture instead of signal number.
 *      Fixed locking in ->poll().
 *      Added sighand-detach notification.
 *      Added fd re-use in sys_signalfd() syscall.
 *      Now using anonymous inode source.
 *      Thanks to Oleg Nesterov for useful code review and suggestions.
 *      More comments and suggestions from Arnd Bergmann.
 *  Sat May 19, 2007: Davi E. M. Arnaut <davi@haxent.com.br>
 *      Retrieve multiple signals with one read() call
 *  Sun Jul 15, 2007: Davide Libenzi <davidel@xmailserver.org>
 *      Attach to the sighand only during read() and poll().
 */

#include <linux/file.h>
#include <linux/poll.h>
#include <linux/init.h>
#include <linux/fs.h>
#include <linux/sched.h>
#include <linux/kernel.h>
#include <linux/signal.h>
#include <linux/list.h>
#include <linux/anon_inodes.h>
#include <linux/signalfd.h>
#include <linux/syscalls.h>

struct signalfd_ctx {
	sigset_t sigmask;
};

static int signalfd_release(struct inode *inode, struct file *file)
{
	kfree(file->private_data);
	return 0;
}

static unsigned int signalfd_poll(struct file *file, poll_table *wait)
{
	struct signalfd_ctx *ctx = file->private_data;
	unsigned int events = 0;

	poll_wait(file, &current->sighand->signalfd_wqh, wait);

	spin_lock_irq(&current->sighand->siglock);
	if (next_signal(&current->pending, &ctx->sigmask) ||
	    next_signal(&current->signal->shared_pending,
			&ctx->sigmask))
		events |= POLLIN;
	spin_unlock_irq(&current->sighand->siglock);

	return events;
}

/*
 * Copied from copy_siginfo_to_user() in kernel/signal.c
 */
static int signalfd_copyinfo(struct signalfd_siginfo __user *uinfo,
			     siginfo_t const *kinfo)
{
	long err;

	BUILD_BUG_ON(sizeof(struct signalfd_siginfo) != 128);

	/*
	 * Unused members should be zero ...
	 */
	err = __clear_user(uinfo, sizeof(*uinfo));

	/*
	 * If you change siginfo_t structure, please be sure
	 * this code is fixed accordingly.
	 */
	err |= __put_user(kinfo->si_signo, &uinfo->ssi_signo);
	err |= __put_user(kinfo->si_errno, &uinfo->ssi_errno);
	err |= __put_user((short) kinfo->si_code, &uinfo->ssi_code);
	switch (kinfo->si_code & __SI_MASK) {
	case __SI_KILL:
		err |= __put_user(kinfo->si_pid, &uinfo->ssi_pid);
		err |= __put_user(kinfo->si_uid, &uinfo->ssi_uid);
		break;
	case __SI_TIMER:
		 err |= __put_user(kinfo->si_tid, &uinfo->ssi_tid);
		 err |= __put_user(kinfo->si_overrun, &uinfo->ssi_overrun);
		 err |= __put_user((long) kinfo->si_ptr, &uinfo->ssi_ptr);
		break;
	case __SI_POLL:
		err |= __put_user(kinfo->si_band, &uinfo->ssi_band);
		err |= __put_user(kinfo->si_fd, &uinfo->ssi_fd);
		break;
	case __SI_FAULT:
		err |= __put_user((long) kinfo->si_addr, &uinfo->ssi_addr);
#ifdef __ARCH_SI_TRAPNO
		err |= __put_user(kinfo->si_trapno, &uinfo->ssi_trapno);
#endif
		break;
	case __SI_CHLD:
		err |= __put_user(kinfo->si_pid, &uinfo->ssi_pid);
		err |= __put_user(kinfo->si_uid, &uinfo->ssi_uid);
		err |= __put_user(kinfo->si_status, &uinfo->ssi_status);
		err |= __put_user(kinfo->si_utime, &uinfo->ssi_utime);
		err |= __put_user(kinfo->si_stime, &uinfo->ssi_stime);
		break;
	case __SI_RT: /* This is not generated by the kernel as of now. */
	case __SI_MESGQ: /* But this is */
		err |= __put_user(kinfo->si_pid, &uinfo->ssi_pid);
		err |= __put_user(kinfo->si_uid, &uinfo->ssi_uid);
		err |= __put_user((long) kinfo->si_ptr, &uinfo->ssi_ptr);
		break;
	default:
		/*
		 * This case catches also the signals queued by sigqueue().
		 */
		err |= __put_user(kinfo->si_pid, &uinfo->ssi_pid);
		err |= __put_user(kinfo->si_uid, &uinfo->ssi_uid);
		err |= __put_user((long) kinfo->si_ptr, &uinfo->ssi_ptr);
		err |= __put_user(kinfo->si_int, &uinfo->ssi_int);
		break;
	}

	return err ? -EFAULT: sizeof(*uinfo);
}

static ssize_t signalfd_dequeue(struct signalfd_ctx *ctx, siginfo_t *info,
				int nonblock)
{
	ssize_t ret;
	DECLARE_WAITQUEUE(wait, current);

	spin_lock_irq(&current->sighand->siglock);
	ret = dequeue_signal(current, &ctx->sigmask, info);
	switch (ret) {
	case 0:
		if (!nonblock)
			break;
		ret = -EAGAIN;
	default:
		spin_unlock_irq(&current->sighand->siglock);
		return ret;
	}

	add_wait_queue(&current->sighand->signalfd_wqh, &wait);
	for (;;) {
		set_current_state(TASK_INTERRUPTIBLE);
		ret = dequeue_signal(current, &ctx->sigmask, info);
		if (ret != 0)
			break;
		if (signal_pending(current)) {
			ret = -ERESTARTSYS;
			break;
		}
		spin_unlock_irq(&current->sighand->siglock);
		schedule();
		spin_lock_irq(&current->sighand->siglock);
	}
	spin_unlock_irq(&current->sighand->siglock);

	remove_wait_queue(&current->sighand->signalfd_wqh, &wait);
	__set_current_state(TASK_RUNNING);

	return ret;
}

/*
 * Returns a multiple of the size of a "struct signalfd_siginfo", or a negative
 * error code. The "count" parameter must be at least the size of a
 * "struct signalfd_siginfo".
 */
static ssize_t signalfd_read(struct file *file, char __user *buf, size_t count,
			     loff_t *ppos)
{
	struct signalfd_ctx *ctx = file->private_data;
	struct signalfd_siginfo __user *siginfo;
	int nonblock = file->f_flags & O_NONBLOCK;
	ssize_t ret, total = 0;
	siginfo_t info;

	count /= sizeof(struct signalfd_siginfo);
	if (!count)
		return -EINVAL;

	siginfo = (struct signalfd_siginfo __user *) buf;
	do {
		ret = signalfd_dequeue(ctx, &info, nonblock);
		if (unlikely(ret <= 0))
			break;
		ret = signalfd_copyinfo(siginfo, &info);
		if (ret < 0)
			break;
		siginfo++;
		total += ret;
		nonblock = 1;
	} while (--count);

	return total ? total: ret;
}

static const struct file_operations signalfd_fops = {
	.release	= signalfd_release,
	.poll		= signalfd_poll,
	.read		= signalfd_read,
};

SYSCALL_DEFINE4(signalfd4, int, ufd, sigset_t __user *, user_mask,
		size_t, sizemask, int, flags)
{
	sigset_t sigmask;
	struct signalfd_ctx *ctx;

	/* Check the SFD_* constants for consistency.  */
	BUILD_BUG_ON(SFD_CLOEXEC != O_CLOEXEC);
	BUILD_BUG_ON(SFD_NONBLOCK != O_NONBLOCK);

	if (flags & ~(SFD_CLOEXEC | SFD_NONBLOCK))
		return -EINVAL;

	if (sizemask != sizeof(sigset_t) ||
	    copy_from_user(&sigmask, user_mask, sizeof(sigmask)))
		return -EINVAL;
	sigdelsetmask(&sigmask, sigmask(SIGKILL) | sigmask(SIGSTOP));
	signotset(&sigmask);

	if (ufd == -1) {
		ctx = kmalloc(sizeof(*ctx), GFP_KERNEL);
		if (!ctx)
			return -ENOMEM;

		ctx->sigmask = sigmask;

		/*
		 * When we call this, the initialization must be complete, since
		 * anon_inode_getfd() will install the fd.
		 */
		ufd = anon_inode_getfd("[signalfd]", &signalfd_fops, ctx,
				       flags & (O_CLOEXEC | O_NONBLOCK));
		if (ufd < 0)
			kfree(ctx);
	} else {
		struct file *file = fget(ufd);
		if (!file)
			return -EBADF;
		ctx = file->private_data;
		if (file->f_op != &signalfd_fops) {
			fput(file);
			return -EINVAL;
		}
		spin_lock_irq(&current->sighand->siglock);
		ctx->sigmask = sigmask;
		spin_unlock_irq(&current->sighand->siglock);

		wake_up(&current->sighand->signalfd_wqh);
		fput(file);
	}

	return ufd;
}

SYSCALL_DEFINE3(signalfd, int, ufd, sigset_t __user *, user_mask,
		size_t, sizemask)
{
	return sys_signalfd4(ufd, user_mask, sizemask, 0);
}
