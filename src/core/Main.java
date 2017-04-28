package core;

/**
 * <i>The</i> main class.
 * 
 * @author EPICI
 * @version 1.0
 */
public class Main {
	
	public static Session session;

	/**
	 * <i>The</i> main method. Uses &quot;/&quot; for empty/null values.
	 * <br>
	 * Because technical reasons, if a file is opened with the
	 * application, the file location is the first argument.
	 * To avoid this, use &quot;/&quot; or one of the standard
	 * argument specifiers.
	 * <br>
	 * Supported arguments:
	 * <ul>
	 * <li>&quot;-open &lt;location&gt;&quot; to open a project file</li>
	 * </ul>
	 * 
	 * @param args program arguments
	 */
	public static void main(String[] args) {
		session = new Session();
		int i=1;
		if(args.length>0){
			String s = args[0];
			switch(s.charAt(0)){
			case '/':break;
			case '-':i--;break;
			default:{
				open(s);
				break;
			}
			}
		}
		for(;i<args.length;){
			String first = args[i++];
			switch(first){
			case "-open":{
				open(args[i++]);
				break;
			}
			}
		}
		init();
	}
	
	/**
	 * Called at the end of main. The rest of the initialization process
	 * is delegated to this method.
	 */
	public static void init(){
		if(session.composition==null){
			session.newComposition();
		}
		session.composition.currentSession=session;
		//TODO
	}
	
	/**
	 * Open a project file
	 * 
	 * @param filename the file location
	 */
	public static void open(String filename){
		//TODO
	}

}
