
class ParentClass
{
    public:
    int _intAParentClass;
    
    ParentClass( int a )
    {
        _intAParentClass = a;
    }
};



class DerivedClass : public ParentClass
{
public:
    DerivedClass( int aDerived );
};



DerivedClass::DerivedClass( int aDerived ) : ParentClass( aDerived )
{
    
}



void main()
{
    ParentClass *classInst = new DerivedClass( 5 );
    
    int b = classInst->_intAParentClass;
}