int f0(int x) {
  return x > 0 ? 1 : 0;
}

int f1(int x) {
  return x > 0? 1: 0; }

int main() {
  return f0(1) + f1(-1);
}
