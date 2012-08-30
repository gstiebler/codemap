
struct sInterno
{
	float soEsse;
};

struct sSeg
{
	int m1, n1;
	sInterno si;
	
	void setM1(int value);
};

void sSeg::setM1(int value)
{
	m1 = value;
}

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
	
	str2.seg.setM1(15);
	str2.seg.si.soEsse = 17.15;
	
	float r = str2.inc_a5(6.0);
	
	float p = r / str2.seg.m1 * str2.seg.si.soEsse;
}
