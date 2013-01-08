
int main() {
	int *a;
	int b, c;
	b = 10;
	c = 20;
		
	int m = 6;
	if(m > 5)
	{
		a = &b;
	}
	else
	{
		a = &c;
	}
	
	int d = *a;
	*a = 7;
	int e = b;
	
	int x = 8;
	int y = 16;
	int *f = &x;
	if(m > 10)
		f = &y;
	
	z = *f;
}
