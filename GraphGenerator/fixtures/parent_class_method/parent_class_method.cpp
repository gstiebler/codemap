

class cPri
{
public:
	float _a;
	
	float soma(float x)
	{
		return x + _a;
	}
	
	static float subtracao(float y)
	{
		return y - 50;
	}
};

class cSeg : public cPri
{
public:

	float inc_a5(float z)
	{
        _a = 5;
        float w = cPri::soma( z );
        float k = subtracao( 20 );
        return w + k;
	}
   
};


int main() {
	cSeg seg;
	
    float m = seg.inc_a5( 30 );
}
