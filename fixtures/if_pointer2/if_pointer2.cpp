
int main() {
	int *a;
	int b, c;
	b = 10;
	c = 20;

    if(true)
    {
        int *d = b;
        *d = 8;
        if(false)
        {
            a = &c;
            c = 25;
        }
        c += 10;
    }
}
