#include <iostream>
using namespace std;

int main() {
    int a = 1;
    int b = 2;
    int v = 0;
    int a2 = 1;
    int b2 = 2;
    int v2 = 0;

    // id=4 index 0: if ( E && E1 ) S
    if (a > 0 && b > 0) {
        v = 1;
    }

    // id=4 index 1: if ( E ) { if ( E1 ) S }
    if (a2 > 0) {
        if (b2 > 0) {
            v2 = 1;
        }
    }

    cout << v << v2 << "\n";
    return 0;
}
