
class sPri;

float globalFunc(sPri *param);

class sPri
{
public:
	float _a;
	float _b;
	
	void func1();
};

float globalFunc(sPri *param)
{
	return param->_a + 5;
}

void sPri::func1()
{
	this->_a = 3;
	_b = globalFunc(this);
}

int main() {
	sPri str1;
	str1.func1();
	float c = str1._b;
}