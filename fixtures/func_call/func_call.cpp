
int soma(int a, int b)
{
	int resultado;
	resultado = a + b;
	return resultado;
}

int soma(int a, int b, float fator)
{
	return (a + b) * fator;
}

int main() 
{
	int x;
	int a = 3;
	int b = 5;
	int c = soma(a, b);
	
	int d = soma(a, b, 0.3);
}
