#include "minic-stdlib.h"
int search(int *a, int target, int l, int r) {
    int i;
    int m;
    int f;
    if (l + 1 >= r) {
        if (a[l] == target) {
            return target;
        } else {
            return -1;
        }
    }
    i = l;
    m = (l + r) / 2;
    if (a[m] == target) {
        return target;
    } else {
        if (target < a[m] ) {
            return search(a, target, l, m);
        } else {
            return search(a, target, m, r);
        }
    }
    return -1;
}

void main() {
    int a[5];
    int ans;
    a[0] = 1;
    a[1] = 2;
    a[2] = 3;
    a[3] = 4;
    a[4] = 5;
    print_i(search(a, 1, 0, 5));
    print_i(search(a, 2, 0, 5));
    print_i(search(a, 3, 0, 5));
    print_i(search(a, 4, 0, 5));
    print_i(search(a, 5, 0, 5));
    print_i(search(a, 11, 0, 5));
    print_i(9999);
}