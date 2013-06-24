#include <stdio.h> 

// ugly hack because we don't have fscanf

int fscanf(FILE* stream, const char* format, int* value)
{
    int c;
    int r = 0;
    do {
        c = fgetc(stream);
        if (c>='0' && c<='9') {
            r = r*10 + (c-'0');
            continue;
        }
        break;
    } while (1);

    *value = r;

    // gahhhh
    return 1;
}
