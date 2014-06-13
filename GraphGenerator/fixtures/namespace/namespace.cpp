
namespace namespaceA
{

class ClassA
{
public:
    int _memberA;
};

int soma(int a, int b);

};


int namespaceA::soma(int a, int b)
{
    return a + b;
}
    
using namespace namespaceA;

void func1()
{
    ClassA var1;
    var1._memberA = 3;
    
    namespaceA::ClassA var2;
    var2._memberA = 5;
}

namespace namespaceB
{

class ClassB
{
public:
    int _memberB;
};

};

using namespaceB::ClassB;
    
int main() 
{
    func1();
    
    ClassB var3;
    var3._memberB = 8;
    
    int x = namespaceA::soma(10, 20);
}
