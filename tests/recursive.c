int nat(int n) {
    if (n == 0) {
        return 0;
    } else {
        return 1 + nat(n - 1);
    }
}

void main() {
    print_i(nat(12));
}