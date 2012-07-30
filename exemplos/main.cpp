
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

struct sInterno
{
	float soEsse;
};

struct sSeg
{
	int m1, n1;
	sInterno si;
};

struct sPri
{
	float a;
	float h;
	
	sSeg seg;
	
	float inc_a5(float w)
	{
		float inc;
		inc = 5.0;
		a -= inc;
		h = h + w + 3.0;
		a *= h;
		seg.m1 += 16;
		seg.si.soEsse += 18.20;
		return 7 * h;
	}
};

int main() {
	sPri str1, str2;
	
	str1.a = 23.4;
	str1.h = 589.45;
	
	str2.a = 45.98;
	str2.h = 9099.0;
	
	str2.seg.m1 = 15;
	str2.seg.si.soEsse = 17.15;
	
	float r = str2.inc_a5(6.0);
	
	float p = r / str2.seg.m1 * str2.seg.si.soEsse;
	
	float g = str1.a - str1.h;
	float k = str2.a * str2.h;
	
	int l = func1(20, 22);
	
	float m;
	
	{
		m = g / k;
	}

	int z, e, x = 0;
	z = 30;
	
	if(m > 5)
	{
		z = 40;
		x = 11;
		str1.h = 400.0;
	}
	else
	{
		z = 50;
		x = 12;
	}
	
	int y = 3;
	if(m >= 8)
		x += y;
	int d = 9;
	d /= x * 5 + y * m;
	e = soma(x, 5);
	bool value = false;
	int f = soma(d, e) + 4;
	
	int q = 3;
	int s = 53;
	for (int i = 0; i < 4; i++)
	{
		s = q;
		q += s - e;
		d = q;
	}
}
