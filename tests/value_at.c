
void main() {
    int i;
    int j;
    int *p;
    j = 1;
    i = *&j;
    print_i(i);
    p = &j;
    print_i(*p);
    print_i(sizeof(int));
    print_i(sizeof(char));
}