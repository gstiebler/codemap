
int soma(int a, int b)
{
	int resultado;
	resultado = a + b;
	return resultado;
}

int func1(int x, int y)
{
	int resultado = x + 2;
	resultado -= soma(3, y - x);
	return resultado;
}

int main() {
	int a = 3;
	int b = 8;
	int l = func1(a, b);
}
