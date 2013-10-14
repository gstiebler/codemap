
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
    
    _Tp soma(_Tp a, _Tp b)
    {
        return a + b;
    }
};

template <>
class vector<float> {

public:
    void push_back(float value)
    {
        return;
    }
    
    float get_value()
    {
        return 20.0f;
    }

    float soma(float a, float b)
    {
        return (a + b) * 2;
    }
};
    
int main() 
{
	vector<int> myVector;
    myVector.push_back(5);
    
    int a = myVector.get_value();
    
    vector<float> fVector;
    float x = fVector.soma(3.0f, 12.0f);
}
