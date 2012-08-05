
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
	sInterno sint;
};

int main() {
	sPri str1, str2;
	
	str1.a = 23.4;
	str1.h = 589.45;
	
	str2.a = 45.98;
	str2.h = 9099.0;
	
	str2.seg.m1 = 15;
	str2.seg.si.soEsse = 17.15;
	
	float r = 8;
	float p = r / str2.seg.m1 * str2.seg.si.soEsse;
	
	float g = str1.a - str1.h;
	float k = str2.a * str2.h;
}