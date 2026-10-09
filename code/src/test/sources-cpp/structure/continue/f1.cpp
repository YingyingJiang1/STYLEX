#include <iostream>
using namespace std;

int main() {
    int s0 = 0;
    for (int i = 0; i < 5; ++i) {
        if (i % 2 == 0) {
            continue;
        }
        s0 += i;
    }

    int s1 = 0;
    for (int i = 0; i < 5; ++i) {
        if (i % 2 == 0) {
            continue;
        } else {
            s1 += i;
        }
    }

    int s2 = 0;
    for (int i = 0; i < 5; ++i) {
        if (i % 2 != 0) {
            s2 += i;
        }
    }

    cout << s0 + s1 + s2 << "\n";
    return 0;
}
