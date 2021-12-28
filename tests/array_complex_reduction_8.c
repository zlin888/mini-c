#include "minic-stdlib.h"
int a() {
  int b[15];
  int c;
  int d;
  int e;
  int f;
  int g;
  int h;
  int i;
  int j;
  int k;
  int l[13];
  int m;
  int n;
  int o;
  int p;
  int q;
  int r;
  b[0] = 4;
  b[1] = 32;
  b[2] = 247;
  b[3] = 212;
  b[4] = 5;
  b[5] = 35;
  b[6] = 6;
  b[7] = 1;
  b[8] = 134;
  b[9] = 87;
  b[10] = 149;
  b[11] = 42;
  b[12] = 27;
  b[13] = 15;
  b[14] = 4;
  c = 4;
  d = 32;
  e = 247;
  f = 212;
  g = 5;
  h = 35;
  i = 6;
  j = 1;
  k = 134;
  m = 0;
  while (m < 13) {
    l[m] = b[m];
    m = m + 1;
  }
  n = 4 + 4;
  o = 32 + 32;
  p = 247 - 247;
  q = 0;
  r = 0;
  while (q < 248) {
    int s;
    if (q < 158) {
      s = -(-(b[q / 15] - 4 - (b[q % 15] + 3)));
    } else {
      int t;
      int u;
      int v;
      if ((q - 158) / 3 < 15) {
        t = b[(q - 158) / 3];
      } else {
        t = b[(q - 158) / 3 - 15];
      }
      if ((q - 158) / 3 < 9) {
        int w;
        if ((q - 158) / 3 == 0)
          w = c;
        else if ((q - 158) / 3 == 1)
          w = d;
        else if ((q - 158) / 3 == 2)
          w = e;
        else if ((q - 158) / 3 == 3)
          w = f;
        else if ((q - 158) / 3 == 4)
          w = g;
        else if ((q - 158) / 3 == 5)
          w = h;
        else if ((q - 158) / 3 == 6)
          w = i;
        else if ((q - 158) / 3 == 7)
          w = j;
        else
          w = k;
        u = w;
      } else {
        u = b[((q - 158) / 3 - 9) % 15];
      }
      if ((q - 158) % 3 == 0)
        v = n;
      else if ((q - 158) % 3 == 1)
        v = o;
      else
        v = p;
      s = t + b[(q - 158) / 3 / 15] * b[(q - 158) / 3 % 15] + u - v;
    }
    r = s + r;
    q = q + 1;
  }
  return r;
}
void main() { print_i(a()); }
