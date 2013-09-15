
class BaseClassA
{
public:
	int _baseMemberA;
};

class BaseClassB
{
public:
	int _baseMemberB;
};

class DerivedClass : public BaseClassA, public BaseClassB
{
public:
	int _unused;
};

int main() 
{
	DerivedClass derived;
	derived._baseMemberA = 5;
	derived._baseMemberB = 10;
	int a = derived._baseMemberA + derived._baseMemberB;
}
