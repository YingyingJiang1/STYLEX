int main() {
  int sum0 = 0;
  int i0 = 0;
  while (i0 < 5) {
    i0++;
    if (i0 % 2 == 0) {
      sum0 += i0;
      continue;
    }
    sum0 += 1;
  }

  int sum1 = 0;
  int i1 = 0;
  while (i1 < 5) {{
    i1++;}
    if ( i1 % 2 == 0) { sum1 += i1;
      continue ; }  sum1 += 1;
    }

  int sum2 = 0;
  int i2 = 0;
  while (i2 < 5) {{
    i2++;}
    if ( i2 % 2 == 0) { sum2 += i2;
    continue ; }  sum2 += 1;
    }

  return sum0 + sum1 + sum2;
}
