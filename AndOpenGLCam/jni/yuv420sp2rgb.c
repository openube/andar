#include <string.h>
#include <jni.h>
#include <yuv420sp2rgb.h>
#include <yuv420sp2rgbjni.h>

#ifndef max
#define max(a,b) ({typeof(a) _a = (a); typeof(b) _b = (b); _a > _b ? _a : _b; })
#define min(a,b) ({typeof(a) _a = (a); typeof(b) _b = (b); _a < _b ? _a : _b; })
#endif

#define CONVERT_TYPE_PPM 0
#define CONVERT_TYPE_RGB 1
#define CONVERT_TYPE_ARGB 2

#define TEXTURE_SIZE 256

/*
 * convert a yuv420 array to a rgb array
 */
JNIEXPORT void JNICALL Java_edu_dhbw_andopenglcam_CameraPreviewHandler_yuv420sp2rgb
  (JNIEnv* env, jobject object, jbyteArray pinArray, jint width, jint height, jint textureSize, jbyteArray poutArray) {
	jbyte *inArray; 
	jbyte *outArray; 
	inArray = (*env)->GetByteArrayElements(env, pinArray, JNI_FALSE);
	outArray = (*env)->GetByteArrayElements(env, poutArray, JNI_FALSE);
	//see http://java.sun.com/docs/books/jni/html/functions.html#100868
	//If isCopy is not NULL, then *isCopy is set to JNI_TRUE if a copy is made; if no copy is made, it is set to JNI_FALSE.	
	
       color_convert_common(inArray, inArray + width * height,
                 width, height, textureSize,
                 outArray, width * height * 3,
                 0, 0, rgb24_cb);

	//release arrays:
	(*env)->ReleaseByteArrayElements(env, pinArray, inArray, 0);
	(*env)->ReleaseByteArrayElements(env, poutArray, outArray, 0);
}

/*
   YUV 4:2:0 image with a plane of 8 bit Y samples followed by an interleaved
   U/V plane containing 8 bit 2x2 subsampled chroma samples.
   except the interleave order of U and V is reversed.

                        H V
   Y Sample Period      1 1
   U (Cb) Sample Period 2 2
   V (Cr) Sample Period 2 2
 */


/*
 size of a char:
 find . -name limits.h -exec grep CHAR_BIT {} \;
 */

const int bytes_per_pixel = 2;

static void color_convert_common(
    unsigned char *pY, unsigned char *pUV,
    int width, int height, int texture_size,
    unsigned char *buffer,
    int size, /* buffer size in bytes */
    int gray,
    int rotate,
    rgb_cb cb) 
{
	int i, j;
	int nR, nG, nB;
	int nY, nU, nV;
    rgb_context ctx;

    ctx.buffer = buffer;
    ctx.size = size; /* debug */
    ctx.width = width;
    ctx.height = height;
    ctx.rotate = rotate;
    ctx.texture_size = texture_size;

    if (gray) {
        for (i = 0; i < height; i++) {
            for (j = 0; j < width; j++) {
                nB = *(pY + i * width + j);
                ctx.i = i;
                ctx.j = j;
                cb(nB, nB, nB, &ctx);
            }
        }	
    } else {
        // YUV 4:2:0
        for (i = 0; i < height; i++) {
            for (j = 0; j < width; j++) {
                nY = *(pY + i * width + j);
                nV = *(pUV + (i/2) * width + bytes_per_pixel * (j/2));
                nU = *(pUV + (i/2) * width + bytes_per_pixel * (j/2) + 1);
            
                // Yuv Convert
                nY -= 16;
                nU -= 128;
                nV -= 128;
            
                if (nY < 0)
                    nY = 0;
            
                // nR = (int)(1.164 * nY + 2.018 * nU);
                // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
                // nB = (int)(1.164 * nY + 1.596 * nV);
            
                nB = (int)(1192 * nY + 2066 * nU);
                nG = (int)(1192 * nY - 833 * nV - 400 * nU);
                nR = (int)(1192 * nY + 1634 * nV);
            
                nR = min(262143, max(0, nR));
                nG = min(262143, max(0, nG));
                nB = min(262143, max(0, nB));
            
                nR >>= 10; nR &= 0xff;
                nG >>= 10; nG &= 0xff;
                nB >>= 10; nB &= 0xff;

                ctx.i = i;
                ctx.j = j;
                cb(nR, nG, nB, &ctx);
            }
        }
    }
}   

static void rgb16_cb(
    unsigned char r,
    unsigned char g,
    unsigned char b,
    rgb_context *ctx)
{
    unsigned short *rgb16 = (unsigned short *)ctx->buffer;
    *(rgb16 + ctx->i * ctx->width + ctx->j) = b | (g << 5) | (r << 11);
}

static void common_rgb_cb(
    unsigned char r,
    unsigned char g,
    unsigned char b,
    rgb_context *ctx,
    int alpha)
{
    unsigned char *out = ctx->buffer;
    int offset = 0;
    int bpp;
    int i = 0;
    switch(ctx->rotate) {
    case 0: /* no rotation */
        offset = ctx->i * ctx->width + ctx->j;
	//offset = ctx->i * ctx->texture_size + ctx->j;
        break;
    case 1: /* 90 degrees */
        offset = ctx->height * (ctx->j + 1) - ctx->i;
        break;
    case 2: /* 180 degrees */
        offset = (ctx->height - 1 - ctx->i) * ctx->width + ctx->j;
        break;
    case 3: /* 270 degrees */
        offset = (ctx->width - 1 - ctx->j) * ctx->height + ctx->i;
        break;
    /*default:
        FAILIF(1, "Unexpected roation value %d!\n", ctx->rotate);*/
    }

    bpp = 3 + !!alpha;
    offset *= bpp;
   /* FAILIF(offset < 0, "point (%d, %d) generates a negative offset.\n", ctx->i, ctx->j);
    FAILIF(offset + bpp > ctx->size, "point (%d, %d) at offset %d exceeds the size %d of the buffer.\n",
           ctx->i, ctx->j,
           offset,
           ctx->size);*/

    out += offset;

    if (alpha) out[i++] = 0xff;
    out[i++] = r;
    out[i++] = g;
    out[i] = b;
}

static void rgb24_cb(
    unsigned char r,
    unsigned char g,
    unsigned char b,
    rgb_context *ctx)
{
    return common_rgb_cb(r,g,b,ctx,0);
}

static void argb_cb(
    unsigned char r,
    unsigned char g,
    unsigned char b,
    rgb_context *ctx)
{
    return common_rgb_cb(r,g,b,ctx,1);
}
