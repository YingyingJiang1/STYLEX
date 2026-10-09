#include <iostream>
using namespace std;

int main() {
  int sum0 = 0;
  for (int i = 0; i < 3; i++) {
    sum0 += i;
  }

  int sum1 = 0;
  int j = 0;
  for (; j < 3; j++) {
    sum1 += j;
  }

  int sum2 = 0;
  int n = 0;
  while (n < 3) {
    sum2 += n;
    n++;
  }

  cout << sum0 + sum1 + sum2 << "\n";
  return 0;
}
