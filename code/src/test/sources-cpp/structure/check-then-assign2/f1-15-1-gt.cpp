#include <iostream>
using namespace std;

int main() {
    int a0 = 1;
    int x0 = 0;
    int y0 = 2;
    x0 = a0? 3: y0; int a1 = 1;
    int x1 = 0;
    x1 = a1 ? 3 : 2;

    int a2 = 1;
    int x2 = 0;
    int w2 = 2;
    x2 = a2? 3: w2; cout << x0 + x1 + x2 << "\n";
    return 0;
}
