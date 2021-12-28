#include "minic-stdlib.h"
int num(int a) {
    if (a == 0) {
        return 0;
    }
    return 1 + num(a-1);
}

void main() {
    print_i(num(12));
}