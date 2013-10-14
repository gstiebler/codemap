
class ClassA
{
public:
    int _memberA;
};

typedef ClassA ClassB;

int main() 
{
    ClassB var1;
    var1._memberA = 3;
}
