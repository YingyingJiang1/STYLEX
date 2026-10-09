int main() {
  int i = 0;
  while ( true ) {
    i++;
    if (i > 2) {
      break;
    }
  }
  int j = 0;
  while (true) {
    j++;
    if (j > 2) {
      break;
    }
  }
  return i + j;
}
