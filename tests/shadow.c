int x;

void main(){
   x = 1;
   if (1) 
   {
        int x;
        x = 2;
        if (1)
        {
            int x;
            x = 3;
            print_i(x);
            print_c('\n');
        }
		print_i(x);
        print_c('\n');
   }
   print_i(x);
   print_c('\n');
}