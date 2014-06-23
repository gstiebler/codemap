
#include "static_func.h"

int ClassTest::staticFunc(int staticParam)
{
	return staticParam + 7;
}

int ClassTest::staticOnCpp()
{
    return staticFunc(42);
}
