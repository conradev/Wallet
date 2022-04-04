#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

static const size_t RIPEMD160_DIGEST_LENGTH = 20;

void ripemd160_digest(uint8_t *input, size_t input_len, uint8_t *output, size_t output_len);

#ifdef __cplusplus
}
#endif
