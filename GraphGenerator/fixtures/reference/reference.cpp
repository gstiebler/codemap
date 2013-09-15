
void soma_sub(int a, int b, int &soma, int &sub)
{
	soma = a + b;
	sub = a - b;
}

int main() 
{
		int a = 3;
		int b = 5;
		int c, d;
		soma_sub(a, b, c, d);
		int e = c;
		int f = d;
		
		int k = e + f;
}
