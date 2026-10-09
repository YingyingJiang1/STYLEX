#include <iostream>
using namespace std;

int main() {
    int a0 = 1;
    int x0 = 0;
    int y0 = 2;
    if ( a0) { x0 = 3; } else { x0 = y0; } int a1 = 1;
    int x1 = 0;
    if ( a1 ) { x1 = 3 ; } else { x1 = 2; } int a2 = 1;
    int x2 = 0;
    int w2 = 2;
    if (a2) {
        x2 = 3;
    } else {
        x2 = w2;
    }

    cout << x0 + x1 + x2 << "\n";
    return 0;
}
