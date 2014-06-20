
//#define __SIZE_TYPE__ long unsigned int
//typedef __SIZE_TYPE__ size_t;

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
    
    int d = 5;
    int h = sizeof(d);
}
