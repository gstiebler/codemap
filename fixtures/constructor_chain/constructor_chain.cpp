
class sInterno
{
	float soEsse;
	
public:
	sInterno(float init, int dummy);
	void addSoEsse(float valuE);
};

sInterno::sInterno(float init, int dummy) : soEsse(10.0 + init)
{}

void sInterno::addSoEsse(float valuE)
{
	soEsse += valuE;
}

class sPri
{
	float a;
	float h;
	
	sInterno sint;
	sInterno earlyInit;
	
public:
	sPri(float add, int dummy) : 
		earlyInit(15.0, 1),
		sint(dummy, 81)
	{
		a = 23.4 + add;
		earlyInit.addSoEsse(90.0);
		h = 589.45 + earlyInit.soEsse;
	}
};

int main() {
	sPri str1(3.0, 1);
	sPri *str2 = new sPri(4.0, 2);
	
	str2->h = 9099.0;
	
	float p = 8.0 * str1.sint.soEsse;
	
	float g = str1.a - str1.h;
	float k = str2->a * str2->h;
}