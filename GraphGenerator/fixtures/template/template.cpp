
template<typename _Tp>
class vector
{
private:
    _Tp _internalValue;

public:
    void push_back(_Tp value)
    {
        _internalValue = value;
    }
    
    _Tp get_value()
    {
        return _internalValue;
    }
};
    
int main() 
{
	vector<int> myVector;
    myVector.push_back(5);
    
    int a = myVector.get_value();
}
