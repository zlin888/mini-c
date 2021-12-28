int b() {
    int c;
    if ( 1 == 1) {
        return 12;
    } else {
        return 11;
    }
}

int a() {
    int i;
    i = b();
    return i+b();
}

void main() {
    int i;
    i = a();
    print_i(i);
}
