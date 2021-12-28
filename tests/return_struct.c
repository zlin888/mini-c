struct Book {
  int price;
  int page;
  int shop;
  char* name;
};

struct Book a() {
    struct Book book99;
    book99.price = 1283;
    book99.page = 777;
    book99.name = (char*) "happy";
    book99.shop = 444;
    return book99;
}

//void b(struct Book *p) {
//    (*p).price = 38;
//    (*p).page= 58;
//    (*p).shop= 48;
//    (*p).name= (char*)"hihihihi";
//}

void main() {
//    struct Book book1;
//    b(&book1);
       struct Book book1;
      book1 = a();
    print_i(book1.price);
    print_i(book1.page);
    print_s(book1.name);
    book1.price = 77;
    print_i(book1.price);
}
