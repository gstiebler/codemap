
int main() {
	int *a;
	int b, c;
	b = 10;
	c = 20;
    a = &c;

    if(true)
    {
        a = &b;
        *a = 5;
        int d1 = b;
        if(false)
        {
            a = &c;
            a* = 32;
            int e2 = *a;
        }
    }
}
