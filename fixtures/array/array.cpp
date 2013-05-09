
int main() 
{
    int indexA = 5;
	int array[10];
	array[indexA] = 8;
	int receivedA = array[indexA];
	
	int *array_p = new int [10];
	array_p[indexA] = 8;
	int b = array_p[indexA];
}
