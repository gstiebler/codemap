
int soma(int a, int b);

int main() 
{
	int a = 3;
	int b = 5;
	int c = 8;
	c = soma(a, b);
	a = 15;
	b = 18;
	int d = c;
}

int soma(int a, int b)
{
	return a + b;
}
