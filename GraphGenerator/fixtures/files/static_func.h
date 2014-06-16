
class ClassTest
{
public:
	int _member;

	static int staticFunc(int staticParam);
	
	static int staticFuncSub(int staticParam)
	{
		return staticParam - 8;
	}
	
	int somaCT(int x, int y)
	{
		return x + y;
	}
};
