
class sPri
{
public:
	float _a;
	float _b;
	
	sPri() : _a(0.0), _b(1.0) {}
	
	void operator+(sPri &other)
	{
		_a += other._a;
	}
	
	void operator-(sPri &other);
};

void sPri::operator-(sPri &other)
{
	_a -= other._a;
}

int main() {
	sPri strIn, strOut;
	strIn._a = 10;
	strOut += strIn;
	float c = strOut._a;
	strOut -= strIn;
	float d = strOut._a;
}