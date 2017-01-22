package main;

//Basically every class that needs a finalizer
public interface Destructable {
	//Destroy or dereference everything
	public void destroy();
	//Only self, use if children may be needed elsewhere
	public void destroySelf();
}
