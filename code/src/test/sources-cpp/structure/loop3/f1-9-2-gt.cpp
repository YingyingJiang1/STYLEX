#include <iostream>
using namespace std;

int main() {
  int sum = 0;

  for (int i = 0; i < 3; ) {
    sum += i;
    i++;
  }

  int k = 0;
  for (; k < 3; ) {
    sum += k;
    k++;
  }

  int m = 0;
  while (m < 3) {
    sum += m;
    m++;
  }

  cout << sum << "\n";
  return 0;
}
