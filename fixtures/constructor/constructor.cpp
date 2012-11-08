
class sInterno
{
public:
	float soEsse;
	
	sInterno() 
	{
		soEsse = 11.1;
	}
};

class sPri
{
	float a;
	float h;
	
	sInterno sint;
	
public:
	sPri(float add, int dummy)
	{
		a = 23.4 + add;
		h = 589.45 + sint.soEsse;
	}
};

class sSeg
{
	sInterno interno;
};

int main() {
	float a(3.0);
	sPri str1(a, 1);
	float q = str1.sint.soEsse;
	sPri *str2 = new sPri(4.0, 2);
	
	str2->h = 9099.0;
	str1.sint.soEsse = 17.95;
	
	float p = 8.0 * str1.sint.soEsse;
	
	float g = str1.a - str1.h;
	float k = str2->a * str2->h;
	
	sInterno s();
	//float x = s.soEsse;
	
	//sInterno s2;
	//float y = s2.soEsse;
	
	//sSeg seg;
	//float z = seg.interno.soEsse;
}