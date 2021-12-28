// this file is used to test comments
// this is a single line comment
/* this is a multiple line comments */
/* multiple
   line
   comments */

int main() {
    char i; // should be int (type error)
    i = /*try to nested*/ 0 * 1 /**/;
    return 0;
}
