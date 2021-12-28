#include "INCLUDE"
#include ""

// structdecls
struct Author {
  char names[10];
};

struct Book {
  int price;
  char titles[10];
  struct Author author;
  struct Author authors[10];
};

// vardecls
int price;
char titles[10];

// fundecls
int add(int a, int b) {
  return a + b;
}

void empty_func() {
}

void main() {
  int i;
  int array[10];
  int *p;
  char *s;
  struct Book nicebook;
  // block
  while (price) {
    price;
  }
  
  if (price) price;
  if (price) price; else price;
  
  price = price;
  price;
  
  {}
  {int j;}
  {}

  (price);
  12;
  -price;
  +price;
  'h';
  '\t';
  '\\';
  "string\t\\";
  "";
  price > price;
  price >= price;
  price && price;
  add(price, price);
  empty_func();
  
  array[5];
  nicebook.price;
  nicebook.titles;
  nicebook.titles[1];
  print_s((char*) "HI");
  s = (char*) "Hi"; // not sure about this

  *p;
  &p;
  &*p;
  *&p;
  
  sizeof(int);
  sizeof(struct Book);
  
  (struct Book) nicebook;
  (int*) *p;
  (int) price;
  
  
  return;
} 
