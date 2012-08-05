
struct sPri
{
	float a;
	float h;
};

int main() {
	sPri str1;
	str1.h = 19.3;

	float m;
	float g = 12.3, k = 15.6;
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
}
