void main() {
    int i;
    int* p;

    i  = 0;
    *p = i;
    p  = &i;

    i+2=3; // i+2 is not an lvalue
    &3;    // 3 is not an lvalue
}