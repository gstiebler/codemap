
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

class ClassTest
{
	static int staticFunc(int staticParam)
	{
		return staticParam + 7;
	}
};

int main() 
{
	int x;
	int a = 3;
	int b = 5;
	int c = soma(a, b);
	
	int d = soma(a, b, 0.3);
	
	int e = ClassTest::staticFunc(8);
}
