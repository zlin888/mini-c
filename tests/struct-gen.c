struct Book {
  int price;
  int page;
  int food;
  char* name;
};

void main() {
    struct Book book1;
    struct Book book2;

    book1.price = 1;
//    book2.price = 2;
    book2 = book1;
    book2.price = 2;
    print_i(book1.price);
    print_i(book2.price);
    book2 = book1;
    print_i(book1.price);
    print_i(book2.price);
    book1.page = 4;
    book2.page = 3;
    print_i(book1.page);
    print_i(book2.page);
    book1.page = 3;
    book2.page = 4;
    print_i(book1.page);
    print_i(book2.page);

}
