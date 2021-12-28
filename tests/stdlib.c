char global_c;
void main() {
   // PRINT_C
   char c;
   int i;
   global_c = 'm';
   c = '\n';
   print_c(global_c);
   print_c(c);
   c = 'n';
   print_c(c);
   i = read_i();
   print_i(i);
   c = read_c();
   print_c(c);
}