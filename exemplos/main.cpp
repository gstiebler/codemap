
int soma(int a, int b)
{
	int resultado;
	resultado = a + b;
	return resultado;
}

struct sPri
{
	float a;
	float h;
	
	void inc_a5(float w)
	{
		float inc;
		inc = 5.0;
		a += inc;
		h = h + w + 3.0;
		a += h;
	}
};

int main() {
	sPri str1, str2;
	
	str1.a = 23.4;
	str1.h = 589.45;
	
	str2.a = 45.98;
	str2.h = 9099.0;
	
	str2.inc_a5(6.0);
	
	float g = str1.a + str1.h;
	float k = str2.a + str2.h;
	
	float m = g + k;

	int z, e, x = 0;
	z = 30;
	int y = 3;
	x += y;
	int d;
	d = x * 5 + y * m;
	e = soma(x, 5);
	bool value = false;
	int f = d + e + 4;
	
	for (int i = 0; i < 4; i++)
	{
		int x;
	}
}
