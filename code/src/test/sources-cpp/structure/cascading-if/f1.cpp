#include <iostream>
using namespace std;

int main() {
    int a = 1;
    int b = 0;
    int x = 0;

    int p2 = 1;
    int q2 = 0;
    int r2 = 0;

    // id=5 index 0: if ( E || E1 ) S
    if (a || b) {
        x = 1;
    }

    // id=5 index 1: if ( E ) S if ( E1 ) S1 (same S / S1)
    if (p2) {
        r2 = 1;
    }
    if (q2) {
        r2 = 1;
    }

    cout << x << r2 << "\n";
    return 0;
}
