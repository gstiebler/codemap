
struct sInterno
{
	float soEsse;
};

struct sPri
{
	sInterno sIntA;
	sInterno sIntB;
	
	void func1()
	{
		sIntA.soEsse = 20.0;
		sIntB.soEsse = 30.0;
	}
};

int main() {
	sPri str1;
	str1.func1();
	
	float x = str1.sIntA.soEsse;
	float y = str1.sIntB.soEsse;
}
