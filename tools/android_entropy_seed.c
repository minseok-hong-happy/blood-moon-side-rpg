#include <errno.h>
#include <fcntl.h>
#include <linux/random.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/ioctl.h>
#include <unistd.h>


static int fill_buffer(int file_descriptor, unsigned char *buffer, size_t size) {
    size_t offset = 0;
    while (offset < size) {
        ssize_t read_count = read(file_descriptor, buffer + offset, size - offset);
        if (read_count < 0) {
            if (errno == EINTR) {
                continue;
            }
            return -1;
        }
        if (read_count == 0) {
            errno = EIO;
            return -1;
        }
        offset += (size_t)read_count;
    }
    return 0;
}


int main(void) {
    const size_t byte_count = 512;
    struct rand_pool_info *pool = calloc(1, sizeof(*pool) + byte_count);
    if (pool == NULL) {
        perror("calloc");
        return 1;
    }

    int urandom = open("/dev/urandom", O_RDONLY | O_CLOEXEC);
    if (urandom < 0 ||
            fill_buffer(urandom, (unsigned char *)pool->buf, byte_count) != 0) {
        perror("read /dev/urandom");
        free(pool);
        return 1;
    }
    close(urandom);

    pool->entropy_count = (int)(byte_count * 8);
    pool->buf_size = (int)byte_count;
    int random_device = open("/dev/random", O_WRONLY | O_CLOEXEC);
    if (random_device < 0 || ioctl(random_device, RNDADDENTROPY, pool) != 0) {
        perror("RNDADDENTROPY");
        memset(pool->buf, 0, byte_count);
        free(pool);
        return 1;
    }
    close(random_device);

    memset(pool->buf, 0, byte_count);
    free(pool);
    puts("ANDROID_EMULATOR_ENTROPY_READY");
    return 0;
}
