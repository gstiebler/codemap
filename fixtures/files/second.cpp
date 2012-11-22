
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