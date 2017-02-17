package components;

import javax.swing.JComponent;

/*
 * Interfaces can't specify the superclass of their implementations,
 * and shouldn't need to anyways
 * This allows for extending any component as long as it can
 * cast to the specified class
 */
public interface ComponentType<T extends JComponent> {
	public T asComponent();
}
