int main() {
  int i = 0;
  for (;;) {
    i++;
    if (i > 2) {
      break;
    }
  }
  int j = 0;
  for ( ; ; ) {
    j++;
    if (j > 2) {
      break;
    }
  }
  return i + j;
}
