
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
		a -= 5.0;
		h += 3.0;
		seg.setM1(16);
		seg.si.soEsse += 18.20;
	}
};

int main() {
	sPri str2;
	
	str2.a = 45.98;
	str2.h = 9099.0;
	
	str2.seg.setM1(15);
	float b = str2.seg.m1;
	str2.seg.si.soEsse = 17.15;
	
	str2.inc_a5(6.0);
	
	float p = str2.seg.m1 * str2.seg.si.soEsse;
}
