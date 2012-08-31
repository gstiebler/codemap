
class sInterno
{
	float soEsse;
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

int main() {
	sPri str1(3.0, 1);
	sPri *str2 = new sPri(4.0, 2);
	
	str2->h = 9099.0;
	str1.sint.soEsse = 17.95;
	
	float p = 8.0 * str1.sint.soEsse;
	
	float g = str1.a - str1.h;
	float k = str2->a * str2->h;
}