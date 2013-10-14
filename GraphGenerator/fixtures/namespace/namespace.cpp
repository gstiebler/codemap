
namespace namespaceTest
{

class ClassA
{
public:
    int _memberA;
};

};
    
using namespace namespaceTest;
    
int main() 
{
    ClassA var1;
    var1._memberA = 3;
    
    namespaceTest::ClassA var2;
    var2._memberA = 5;
}
