struct Author {
  int page1;
  char names[10];
  char c;
  int page2;
  char k;
};

int array[7];
int i;
char c;
char ccc;
struct Author author;
char cc;

void main() {
   int aaa[3];
   int j;
   int* ip;
   struct Author jack;
   char *s;
   s = (char*) "hifff";
   jack.page1 = 33;
   jack.page2 = 44;
   author.page1 = 55;
   author.page2 = 66;
   author = jack;
   j = 5555;
   i = 2;
   array[0] = 72;
   array[1] = 12;
   aaa[0] = 88;
   aaa[1] = 98;
   aaa[2] = 198;
   print_i(3);
   print_i(i);
   print_i(j);
   print_i(array[0]);
   print_i(array[1]);
   print_i(aaa[0]);
   print_i(aaa[1]);
   print_i(aaa[2]);
   print_i(jack.page1);
   print_i(jack.page2);
   print_i(author.page1);
   print_i(author.page2);
   print_s(s);
   print_s((char*)"\nhello\n");
   print_s((char*)"\nworld\n");
   i = i + 1;
   print_i(i);
   while(i < 5) {
      print_i(i);
      i = i + 1;
   }
   if (j == 5555) {
      print_s((char*)"good\n");
   } else {
      print_s((char*)"bad\n");
   }
   if (j != 5555) {
      print_s((char*)"good\n");
   } else {
      print_s((char*)"bad\n");
   }
   if (j == 5556 && i == 5) {
      print_s((char*)"good\n");
   } else {
      print_s((char*)"bad\n");
   }
 }