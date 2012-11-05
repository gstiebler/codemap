
class UniqueClass
{
public:
	float _floatMember;
	
	~UniqueClass() {
		_floatMember = 3;
	}
};

int main() 
{
	UniqueClass instance1;
	UniqueClass *pInstance = new UniqueClass;
	
	delete pInstance;
	int x = 5;
}
