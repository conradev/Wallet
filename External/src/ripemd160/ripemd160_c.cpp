#include "ripemd160.h"
#include <crypto/ripemd160.h>

void ripemd160_digest(uint8_t *input, size_t input_len, uint8_t *output, size_t output_len) {
	if (output_len < CRIPEMD160::OUTPUT_SIZE) {
		return;
	}

    CRIPEMD160().Write(input, input_len).Finalize(output);
}
