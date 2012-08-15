
int main() 
{
		int a;
		int *b;
		int c;
		int d;
		a = 3;
		b = &d;
		d = 5;
		c = a + *b;
		*b = 10;
		int e = a + d;
		b = &c;
		int *f = b;
		int *g = &c;
		*g = 15;
		e += a + *f;
}
