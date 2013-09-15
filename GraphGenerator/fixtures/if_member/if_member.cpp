
class ClassFixture
{
    int memberA;
    int memberB;
};

int main() 
{
    ClassFixture instance1, instance2;
    
    instance1.memberA = 10;
    instance2.memberA = 20;
    
    ClassFixture *pointerToClass;
    
    if(true)
        pointerToClass = &instance1;
    else
        pointerToClass = &instance2;
        
    int a = pointerToClass->memberA;
}
