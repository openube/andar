typedef struct rgb_context {
    unsigned char *buffer;
    int width;
    int height;
    int rotate;
    int i;
    int j;
    int size; /* for debugging */
    int texture_size;
} rgb_context;

typedef void (*rgb_cb)(
    unsigned char r,
    unsigned char g,
    unsigned char b,
    rgb_context *ctx);

static void color_convert_common(
    unsigned char *pY, unsigned char *pUV,
    int width, int height, int texture_size,
    unsigned char *buffer,
    int size, /* buffer size in bytes */
    int gray,
    int rotate,
    rgb_cb cb);


static void rgb16_cb(
    unsigned char r,
    unsigned char g,
    unsigned char b,
    rgb_context *ctx);

static void common_rgb_cb(
    unsigned char r,
    unsigned char g,
    unsigned char b,
    rgb_context *ctx,
    int alpha);

static void rgb24_cb(
    unsigned char r,
    unsigned char g,
    unsigned char b,
    rgb_context *ctx);

