
class sInterno
{
public:
	float soEsse;
	
	sInterno(int dummy, float init);
	void addSoEsse(float valuE);
};

sInterno::sInterno(int dummy, float init) : soEsse(10.0 + init)
{}

void sInterno::addSoEsse(float valuE)
{
	soEsse += valuE;
}

class sPri
{
public:
	float a;
	float h;
	
	sInterno sint;
	sInterno earlyInit;
	sPri(float add, int dummy) : 
		earlyInit(1, 15.0),
		sint(81, dummy)
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