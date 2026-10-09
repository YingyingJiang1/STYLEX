#include <iostream>
using namespace std;

int main() {
    int i = 0;
    int sum0 = 0;
    while ( i < 3) {
        sum0 += i;
        ++i;
    }

    int j = 0;
    int sum1 = 0;
    while (j < 3) {
        sum1 += j;
        ++j;
    }

    cout << sum0 + sum1 << "\n";
    return 0;
}
